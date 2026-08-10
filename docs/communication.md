# ftSwarm communication specification

This is the wire-level contract implemented by the Kotlin client. It is written
for authors of compatible cores in other languages. The generator/runtime
boundary above this protocol is specified in [core.md](core.md).

Unless a section is explicitly labeled as recommended hardening, the rules
below describe current Kotlin behavior. Commands and examples use visible
`\r`/`\n` notation; those tokens stand for bytes `0D` and `0A`.

## 1. Physical serial connection

Open the selected serial device with:

- baud rate: **115200**;
- read mode: non-blocking; and
- byte encoding: UTF-8 for outgoing text (the protocol grammar itself is ASCII).

The Kotlin implementation leaves data bits, stop bits, parity, and flow control
at the serial library/platform defaults. A portable implementation should use
the conventional **8 data bits, no parity, 1 stop bit, no flow control (8N1)**
unless its platform already supplies those defaults.

Port discovery is by exact USB vendor/product pairs generated from
`vendor-ids.yaml`. Discovery is a convenience, not part of the on-wire
protocol; callers may select a port explicitly.

## 2. CLI initialization handshake

No ordinary command may be sent until this handshake completes.

1. Start the reader and serialized writer loops.
2. Write an empty CRLF line: `\r\n`.
3. Wait until the serial driver's outbound queue is empty.
4. Write `startCLI\r\n`.
5. Wait up to **5 seconds** for the three consecutive bytes `@@@` at the start
   of the current input line.
6. Mark the transport initialized. Subsequent command submissions may proceed.

The marker does **not** need a line ending. In a normal firmware response such
as `@@@ ftSwarmOS CLI started\n`, initialization completes immediately after the
third `@`; the remainder is later framed as an ordinary diagnostic line.

Before initialization, complete lines other than the marker are diagnostic
output. They must not complete pending commands or create subscriptions. A
line-framing implementation may also accept a complete pre-initialization line
starting with `@@@`, matching the Kotlin fallback.

If the handshake times out or any serial operation fails, fail construction and
close the port. The initial empty line is not an input-buffer flush: stale input
may still arrive and is ignored/logged until initialization.

## 3. Byte and line framing

Outgoing CLI commands are UTF-8 text terminated by exactly `\r\n`.

```text
wireCommand = command with every trailing CR or LF removed
            + "\r\n"
```

This normalization applies to `startCLI` and ordinary commands. Embedded CR,
LF, NUL, or other control characters are not escaped by the Kotlin serializer;
generated callers must not put them in port names, command names, or strings.

Incoming data is processed byte by byte:

- LF (`0A`) ends a line;
- one CR immediately before that LF is removed;
- all other bytes are appended to the current line;
- empty lines are valid diagnostic lines and otherwise ignored; and
- partial lines persist across reads of any size.

The implementation maps each incoming byte directly to a character while
framing. All recognized prefixes and generated payloads are ASCII, so compatible
cores should treat the wire as ASCII/UTF-8 and reject or preserve unexpected
bytes without losing framing.

## 4. Incoming line classes

After initialization, classify complete lines in this order:

| Line form | Meaning | Action |
| --- | --- | --- |
| `R: <value>` | successful command reply | complete oldest pending command with `<value>` |
| optional whitespace + `^...` | CLI command error | complete oldest pending command with error text after the caret and following whitespace |
| optional whitespace + `[ERROR]...` | logged/device error treated as command error | complete oldest pending command; preserve the trimmed `[ERROR]...` text |
| `S: <port> <entry>` | subscription update | parse and broadcast independently of commands |
| anything else | device diagnostic output | log/ignore |

Prefix matching is case-sensitive. `R: ` and `S: ` both include one literal
space. Payload text is otherwise preserved.

The Kotlin error classifier is equivalent to:

```regex
^\s*(?:\^|\[ERROR]).*
```

For a caret error it trims leading whitespace, removes exactly one leading
caret, then trims whitespace again. For `[ERROR]`, it trims leading whitespace
but leaves the marker in the error message.

Malformed subscription lines are logged and dropped. Unknown lines never
consume a pending command.

## 5. Typed serialization and parsing

### 5.1 Command grammar

Generated commands have no whitespace inserted by the serializer:

```text
command       = port "." method "(" [parameter *("," parameter)] ")"
parameter     = integer | float | boolean | quoted-string | color-integer
```

Examples:

```text
S1.getValue()
M1.setSpeed(-120)
S2.setIOType(1,0)
M2.setBlink(1000,1,50,0,16711680,65280,255)
```

`port` and `method` are inserted verbatim. The Kotlin core performs no lexical
validation, escaping, or case conversion on them. Generators should only pass
trusted schema command names and caller-supplied device port identifiers.

### 5.2 Command parameters

| Logical type | Wire encoding | Kotlin-compatible constraints |
| --- | --- | --- |
| integer | base-10 text | signed 32-bit value |
| float | host single-precision decimal text | Kotlin `Float.toString()` semantics; may use a decimal point or exponent |
| boolean | `1` for true, `0` for false | never `true`/`false` on wire |
| string | `"` + value + `"` | a value containing `"` is rejected; no other escaping is performed |
| color | base-10 packed integer | `red << 16 \| green << 8 \| blue`; Kotlin does not range-check channels |

Generated code currently uses the schema types `boolean`, `int`, `float`, and
`string`. `color` exists in the Kotlin core as a convenience encoding but is not
a schema `ApiType`. `joystick` and `ok` cannot be parameters.

For wire safety, language ports should reject CR, LF, and NUL in string values
in addition to the Kotlin core's quote rejection. Escaping quotes or line breaks
would not be compatible because the CLI serializer defines no escape syntax.

### 5.3 Successful reply parsers

The `R: ` prefix is removed before applying the selected parser.

| Parser | Accepted payload | Typed result |
| --- | --- | --- |
| `ok` | first space-delimited token is an integer and the whole payload contains case-insensitive `" ok"` | acknowledgement sequence integer |
| `int` | exactly a host-parseable signed 32-bit decimal integer | integer |
| `boolean` | exactly `1` or `0` | Boolean |
| `string` | any text | remove one pair of surrounding quotes if both are present; otherwise preserve text |
| `float` | host-parseable single-precision float | float |
| `joystick` | exactly two float tokens separated by a space | `{ lr, fb }` |

A normal acknowledgement is `R: <sequence> ok`, for example `R: 42 ok`.
The Kotlin `ok` parser is intentionally permissive about the suffix: after
confirming that `" ok"` occurs anywhere ignoring case, it parses only the token
before the first space. Compatible firmware-facing implementations should emit
and tests should prefer the canonical form even if a client accepts more.

Integer and float parsing failures are command parse errors. The Kotlin
joystick parser can throw while converting malformed tokens instead of wrapping
that conversion in its result type; another language should report the same
condition as a normal parse failure rather than crash its reader loop.

### 5.4 Subscription framing and parsers

The raw subscription grammar is:

```regex
^S: (\S+) (.*)$
```

The first capture is the exact port key. The second capture is the entry; it may
be empty and preserves all remaining spaces. A raw update is broadcast, then
each object filters by exact port.

Plain subscription parsers use the same integer, float, Boolean, and joystick
rules as successful replies. The subscription string parser preserves the entry
verbatim and does **not** remove quotes. Parser failures are supposed to be
logged and dropped. The current Kotlin joystick parser can throw on malformed
numeric tokens; this is a known implementation limitation, and new cores should
convert it to an ordinary parse failure so the stream remains live.

A designated bitset update encodes a field name followed by its value:

```text
S: <port> <designator> <typed-value>
```

The designated parser splits the entry at the first literal space, preserves
the remainder as the inner value, and applies the entry-specific parser after
the generated object selects the matching designator. Example:

```text
S: M1 running 1
S: M1 distance -240
```

Unknown designators simply do not match a generated state route. A malformed
entry or typed value should be logged and dropped so later updates remain live.
As above, a conversion thrown by the current Kotlin composite parsers may
instead terminate that particular collector; do not copy that limitation.

## 6. Correlation, pipelining, and subscriptions

The protocol has no command ID. Correlation is strict FIFO:

1. Serialize all physical writes through one writer.
2. Before writing an ordinary command, append its completion handle to the
   pending FIFO. Registering first prevents a fast reply from overtaking it.
3. The next `R: ` or recognized error line completes and removes the oldest
   pending handle.
4. `S: ` and diagnostic lines never alter the pending FIFO.

Multiple callers may have commands in flight. Their reply/error lines must
arrive in command order. A pipelined stream returns results in original command
order as well.

There is no per-command timeout in the Kotlin core. A missing reply leaves that
command pending and, because correlation is positional, blocks trustworthy
interpretation of subsequent replies. A language may add a timeout, but it must
then fail/reset the entire transport rather than remove only the timed-out FIFO
entry and risk shifting every later response.

Cancellation of one caller must likewise not remove its pending correlation
slot. Discard or detach that caller's eventual result while retaining the slot,
or close/reset the connection.

An `R: ` or error line with no pending command is unexpected and is dropped
with a warning.

## 7. Subscription delivery semantics

Raw subscriptions form a hot broadcast stream shared by all generated objects.
They are not replayed by the transport. Generated state objects provide replay
of the current typed value, initialized by an explicit query before the device
subscription is enabled.

The Kotlin transport has finite burst buffering (128 extra entries). If a live
consumer cannot keep up and a non-blocking publish fails, it drops that update
and logs a warning. Equivalent implementations may use backpressure or a
different bounded capacity, but must ensure subscription handling never blocks
the serial reader long enough to break command processing. Document any
different delivery guarantee in the language client.

See [core.md §6.1](core.md#61-factory-initialization-algorithm) for the required
snapshot/subscribe order and bitset mask calculation.

## 8. Failure and shutdown behavior

Treat these as fatal transport failures:

- the port reports that it is unavailable;
- a read or write fails;
- a write makes no forward progress;
- initialization fails or times out; or
- the platform connection is otherwise lost.

On fatal failure:

1. fail initialization if it is still pending;
2. fail all queued pending command handles with the same cause;
3. stop reader and writer work; and
4. close the serial port.

Explicit close is idempotent. It closes command queues, fails/cancels pending
commands, stops background work, and closes the port. Commands submitted after
close must fail rather than write.

Malformed typed command results fail that command but do not inherently close
the transport. Malformed subscription updates and diagnostics are logged and
dropped.

## 9. State-machine reference

```text
CLOSED/NEW
    |
    | open 115200 8N1 non-blocking; start reader/writer
    v
INITIALIZING
    | TX "\r\n"; drain TX; TX "startCLI\r\n"
    | RX "@@@" within 5 s
    v
READY
    | command: enqueue waiter, then write command + CRLF
    | R/error: complete oldest waiter
    | S: broadcast subscription
    | other: diagnostic
    |
    | close or fatal I/O error
    v
CLOSED/FAILED
```

## 10. Conformance tests

A compatible core should test at least:

- `@@@` initializes without LF and across arbitrary read boundaries;
- CRLF and LF input lines both frame correctly, with one terminal CR removed;
- outgoing commands have exactly one CRLF even when input already ends in CR/LF;
- stale `R: ` lines before initialization do not complete commands;
- success, caret error, `[ERROR]`, subscription, and diagnostic lines classify
  correctly when interleaved;
- concurrent command results remain FIFO;
- a cancelled waiter does not shift FIFO correlation;
- Boolean, string, numeric, acknowledgement, joystick, and designated parsers
  accept canonical values and reject malformed values;
- malformed subscriptions do not terminate later delivery;
- initialization timeout and I/O failure close the port and fail waiters; and
- close is idempotent.
