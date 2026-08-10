# Core implementation contract

This document specifies the smallest language-runtime API that an ftSwarm code
generator needs. It is language-neutral, but it records the behavior of the
current Kotlin core. The byte and line protocol beneath this API is specified in
[communication.md](communication.md).

The core is deliberately small. Generated code should describe IO objects and
call this core; it should not open serial ports, frame lines, correlate replies,
or duplicate parsers.

## 1. Layering

A language implementation has four layers:

1. **Transport** owns the connection, initialization, line framing, reply
   correlation, and the broadcast stream of raw subscriptions.
2. **Transaction context** serializes typed commands, maps transport errors to
   language errors, parses successful values, and parses subscriptions.
3. **Generated IO objects** bind a port name to a transaction context, execute
   their initialization recipe, expose typed commands, and maintain observable
   subscription state.
4. **Client** owns the transport and root context and creates generated objects.

Only layers 3 and the small generated constant tables vary with the IO
definitions. Layers 1, 2, and 4 are the reusable core.

## 2. Minimum public and generator-facing API

Names may follow the target language's conventions. The semantics are required.

```text
interface Transport : Closeable {
    async command(serializedCommand: String) -> RawCommandResult
    subscriptionsFor(port: String) -> AsyncStream<RawSubscription>
}

union RawCommandResult =
    Success(value: String)
    Error(message: String)

record RawSubscription(port: String, entry: String)

record Command(port: String, method: String, parameters: CommandParameter[]) {
    serialize() -> String
}

union CommandParameter = Int | Float | Boolean | String | Color

interface ReturnParser<T> {
    parse(value: String) -> Result<T>
}

interface SubscriptionParser<T> {
    parse(value: String) -> Result<T>
}

interface TransactionContext {
    async command<T>(request: CommandRequest<T>) -> Result<T>
    subscriptions<T>(port: String, parser: SubscriptionParser<T>)
        -> AsyncStream<T>
}

record CommandRequest<T>(command: Command, parser: ReturnParser<T>)

class Client : Closeable {
    generatorVisible rootContext: TransactionContext
}
```

The Kotlin transport also exposes a pipelined `commands(stream)` operation. It
is useful, but generated code does not need it; a single asynchronous `command`
operation that permits concurrent callers is the minimum.

The core also needs an observable, replaying state primitive for subscriptions:

```text
interface State<T> {
    currentValue: T
    updates: AsyncStream<T>       // new observers also see the current value
}
```

Use the target ecosystem's normal equivalent (`StateFlow`, behavior subject,
async property, event emitter plus cached value, and so on). Generated object
construction must start collection eagerly, not on the first user observer.

### 2.1 Lifetime/scope requirement

Each generated object needs a child lifetime owned by the client. The child
lifetime runs its subscription collectors. Closing the client must:

- cancel the client and all object collectors;
- fail or cancel outstanding transport work;
- close the physical connection; and
- be idempotent.

Kotlin calls this child a `LocalFtSwarmTransactionContext`; it delegates command
and subscription operations to the root context and adds a named coroutine
scope. A target language may instead store a cancellation token/task group on
the generated object. A `child(name)` method is therefore convenient, not a
required public API shape.

## 3. Core value model

The generator-visible type universe is fixed:

| Schema type | Command parameter | Command result | Subscription value |
| --- | --- | --- | --- |
| `boolean` | yes, encoded as `1`/`0` | Boolean | Boolean |
| `int` | yes | signed 32-bit integer | signed 32-bit integer |
| `float` | yes | single-precision float | single-precision float |
| `string` | yes | string | string |
| `joystick` | no | `{ lr: float, fb: float }` | `{ lr: float, fb: float }` |
| `ok` | no | `{ sequence: int }` or unit at generated API boundary | invalid |

The Kotlin core wraps successful results in `Ok`, `IntValue`, `BooleanValue`,
`StringValue`, `FloatValue`, and `JoystickValue`. Other languages do not need
these wrapper objects if their `CommandRequest<T>` can return `T` directly.
They must retain the same parser selection and failure behavior.

The core must provide these parser instances:

- return parsers: `ok`, `int`, `boolean`, `string`, `float`, `joystick`;
- subscription parsers: `int`, `boolean`, `string`, `float`, `joystick`; and
- a `designated(inner)` subscription parser producing
  `{ designator: string, value: T }`.

Exact encodings and parser rules are in
[communication.md §5](communication.md#5-typed-serialization-and-parsing).

## 4. Transaction-context behavior

For `command(request)`:

1. Serialize the request's `Command`.
2. Await `transport.command(serialized)`.
3. On `RawCommandResult.Error`, return/throw the language's ftSwarm command
   error containing the device message. Kotlin formats it as
   `FtSwarm Error: <message>`.
4. On `Success(value)`, run the request's return parser.
5. Propagate parse failure distinctly from a device command error where the
   language permits it.

For `subscriptions(port, parser)`:

1. Subscribe to the transport's broadcast stream filtered by exact port name.
2. Parse only the `entry` field.
3. Emit successfully parsed values.
4. Log/report and drop malformed values; one bad update must not terminate the
   subscription stream.

Port comparison is exact and case-sensitive.

The parser contract requires malformed input to be returned as a failed
`Result`, not thrown from `parse`. The current Kotlin joystick and designated
parser implementations contain conversions that can throw for some malformed
numeric input; that is an implementation limitation, not behavior a new core
should reproduce.

## 5. Client creation and discovery

The serial-backed client factory performs these steps atomically from the
caller's perspective:

1. Open the chosen serial port using the settings in the communication spec.
2. Start the transport reader and writer.
3. Complete the CLI initialization handshake.
4. Create the root transaction context.
5. Return the client only after initialization succeeds.

If opening or initialization fails, close the serial port before returning the
error. The Kotlin client attaches its lifetime to the calling asynchronous
scope, but target languages may use explicit ownership.

Automatic discovery is optional for the generator but part of Kotlin parity.
Read `vendor-ids.yaml`, enumerate serial ports, and retain exact VID/PID
matches. `discoverPorts` returns all matches in platform enumeration order;
`getFtSwarmPort` returns the first or fails if there is none. Do not bake the
current numeric IDs into a generator: generate them from the API data.

## 6. Generated object contract

For every normalized `IoDefinition`, generate:

- one user-visible object type;
- one client factory named from `definition.name`;
- one method per `members` entry;
- constructor/factory parameters for all `set_io_type` parameters;
- state properties for subscription entries; and
- for bitset subscriptions, a target-language enum or equivalent containing
  every non-`always` entry and its mask.

The object stores its exact port string and its context/lifetime. A marker
interface such as Kotlin's empty `FtSwarmProtocolObject` is optional; generated
behavior must not depend on it.

### 6.1 Factory initialization algorithm

Factory creation is asynchronous and fail-fast. It must not return a partially
initialized object.

```text
createObject(client, port, initArguments, requestedSubscriptions):
    cx = child lifetime/context for this object

    for each set_io_type step, in definition order:
        await cx.command(
            port.setIOType(IO_TYPE_ID, step parameters...),
            parser = ok
        ).orThrow()

    if subscription is value:
        initial = await call(initialValue)
        state = eagerState(
            cx.subscriptions(port, parser),
            initial
        )
        await cx.command(port.subscribe(), ok).orThrow()

    if subscription is bitset:
        updates = cx.subscriptions(port, designated(string))
        for each entry, in entry order:
            initial = await call(entry.initialValue)
            state[entry] = eagerState(
                updates where designator == entry.name
                    mapped through entry.parser,
                initial
            )

        mask = OR(mask of every `always` entry)
             | OR(mask of selected `default`/`requested` entries)
        await cx.command(port.subscribe(mask), ok).orThrow()

    return object(port, cx, state...)
```

Important ordering constraints:

- Execute all `set_io_type` operations before any snapshot or subscription.
  The current generator does this even if the YAML `subscribe` marker appears
  earlier in the `init` list.
- Query initial values before sending `subscribe`.
- Start each state collector eagerly as it is created.
- For bitsets, query and create states in declaration order, then subscribe
  once with the combined mask.
- Await and require success for every initialization command.
- Create state for every bitset entry. Unselected `default`/`requested` entries
  remain at their initial snapshot unless the device happens to emit them.

If a target state primitive cannot install the collector atomically with its
initial value, ensure the collector is active before the `subscribe` command is
sent so the first device update cannot be missed.

### 6.2 Generated member methods

Each generated member:

1. serializes `port.command(parameters...)` using the schema's command name and
   ordered parameters;
2. selects the parser from `returnType`;
3. awaits the transaction context; and
4. throws/returns failure on transport, device, or parse errors.

An `ok` member returns the target language's unit/void value after validating
the acknowledgement. Other members return the unwrapped typed value. Preserve
schema parameter order and expose scalar defaults at the public method boundary.

### 6.3 State-property names

For a value subscription, derive its public state name from the initial-value
command:

- `getValue` becomes `value`;
- `isRunning` becomes `running`;
- another command name is unchanged.

The prefix is removed only when at least one character follows it, and the first
remaining character is lowercased. Bitset state properties use `entry.name`
directly.

## 7. Generator input model

The current collector reads this normalized model:

```text
IoDefinition {
    name: String
    init: InitStep[] = []
    subscription: SubscriptionDefinition? = null
    members: Map<String, FunctionDefinition> = {}
}

InitStep =
    set_io_type { type: String, parameters: ParameterDefinition[] = [] }
    subscribe

SubscriptionDefinition =
    value {
        parser: ApiType
        initialValue: FunctionDefinition
    }
    bitset BitsetEntry[]

BitsetEntry {
    name: String
    enabled: always | default | requested
    mask: Int
    parser: ApiType
    initialValue: FunctionDefinition
}

FunctionDefinition {
    command: String
    parameters: ParameterDefinition[] = []
    returnType: ApiType
}

ParameterDefinition {
    name: String
    type: ApiType
    defaultValue: scalar? = null
}
```

YAML uses tagged forms `!<set_io_type>`, `!<subscribe>`, `!<value>`, and
`!<bitset>`. Anchors and aliases are permitted.

### 7.1 Files and deterministic collection

Given an API definition directory:

- `vendor-ids.yaml` is a list of `{ vid, pid }` records;
- `iotypes.yaml` is an ordered list of constant names; the zero-based list
  index is the numeric IO type sent to `setIOType`;
- `templates/*.yaml` contains reusable partial mappings;
- `inputs/*.yaml` and `outputs/*.yaml` contain concrete IO definitions.

Sort files lexically within each directory. Collect all inputs first, followed
by all outputs. Output order should therefore be deterministic.

### 7.2 Template resolution

A concrete definition may contain `extends: template-name`.

- Resolution is one level only; templates may not themselves use `extends`.
- Recursively merge mapping values.
- Recursively merge equally tagged nodes.
- A concrete scalar, sequence, differently tagged node, or other non-map value
  replaces the inherited value.
- A concrete YAML `null` deletes the inherited key.
- Remove `extends` before decoding the normalized definition.

This means member maps can be augmented or individual members replaced, while
parameter and init lists replace the template's entire list.

### 7.3 Required validation

Reject definitions that violate any of these rules before generating files:

- IO names are unique across inputs and outputs.
- A definition with a subscription has exactly one `subscribe` init marker; a
  definition without a subscription has none.
- Member names, command names, and parameter names are non-blank.
- Command parameters cannot have type `ok` or `joystick`.
- Scalar defaults parse as their declared `boolean`, `int`, or `float` type;
  string defaults are accepted verbatim.
- A value subscription parser is not `ok` and equals its initial-value return
  type.
- A bitset has at least one entry.
- Bitset entry names and masks are unique within that subscription.
- Every bitset mask is positive and has exactly one bit set.
- Every bitset parser is not `ok` and equals its initial-value return type.

A production generator should additionally reject target-language identifier
collisions, duplicate parameter names, missing IO-type constants, out-of-range
numeric values, and non-scalar defaults. The current Kotlin collector does not
perform those extra checks, so adding them is compatible error hardening rather
than a wire behavior change.

## 8. Generated constants and defaults

Generate IO-type constants by enumerating `iotypes.yaml` from zero. A
`set_io_type.type` string names one of those constants and its numeric value is
the first argument of `setIOType`.

Initialization factory parameters are the concatenation of parameters from all
`set_io_type` steps, preserving step and parameter order. Method parameters
preserve schema order. Render defaults as native literals; escape schema string
defaults for the target source language without changing their runtime value.

`always` bitset entries cannot be deselected. `default` entries appear in the
factory's default selection; `requested` entries do not. Both `default` and
`requested` must be user-selectable when an explicit selection is supplied.

## 9. Reference implementation checklist

A new language core is generator-ready when it can demonstrate all of these:

- initialize a serial connection and reject an initialization timeout;
- execute concurrent commands and correlate mixed success/error replies FIFO;
- receive subscriptions interleaved with replies without disturbing FIFO;
- round-trip every parameter and return type;
- create a non-subscribing IO object and run its `setIOType` recipe;
- create a value-subscription object with an initial snapshot;
- create a bitset-subscription object with `always`, `default`, and `requested`
  masks and designated update routing;
- drop a malformed subscription update without killing later updates;
- cancel outstanding work and close the port idempotently; and
- generate deterministic outputs from templates, inputs, outputs, IO types, and
  vendor IDs.
