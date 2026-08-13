# ftSwarm Python client

The package provides matching blocking and asyncio APIs generated from the same
YAML definitions as the Kotlin client.

## Testbench

From the repository root, run:

```shell
./ftswarm-python-client/testbench
```

It auto-detects an ftSwarm, subscribes to button `S1`, and prints changes until
you press Ctrl-C. Useful options:

```shell
./ftswarm-python-client/testbench --debug
./ftswarm-python-client/testbench --async
./ftswarm-python-client/testbench --io S2 --port /dev/ttyUSB0
./ftswarm-python-client/testbench --list-ports
```

```python
from ftswarm_client import FtSwarmClient

with FtSwarmClient.open() as client:
    button = client.button("S1")
    print(button.value.value)
```

```python
from ftswarm_client import AsyncFtSwarmClient

async with await AsyncFtSwarmClient.open() as client:
    button = await client.button("S1")
    async for value in button.value.changes():
        print(value)
```

Regenerate the Python object API after changing `api/`:

```shell
./kotlin task :ftswarm-kotlin-client:generatePython@codegen
```
