#!/usr/bin/env python3
from __future__ import annotations

import argparse
import asyncio
import logging

from ftswarm_client import AsyncFtSwarmClient, FtSwarmClient, discover_ports


def arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Connect to an ftSwarm and print button subscription updates.",
    )
    parser.add_argument(
        "--port",
        help="Serial device, for example /dev/ttyUSB0; auto-detected by default.",
    )
    parser.add_argument(
        "--io",
        default="S1",
        help="Button IO name to subscribe to (default: S1).",
    )
    parser.add_argument(
        "--async",
        dest="use_async",
        action="store_true",
        help="Exercise the asyncio client instead of the synchronous client.",
    )
    parser.add_argument(
        "--debug",
        action="store_true",
        help="Print all serial communication.",
    )
    parser.add_argument(
        "--list-ports",
        action="store_true",
        help="List detected ftSwarm serial devices and exit.",
    )
    return parser.parse_args()


def configure_logging(debug: bool) -> None:
    logging.basicConfig(
        level=logging.DEBUG if debug else logging.INFO,
        format="%(asctime)s %(levelname)-8s %(name)s: %(message)s",
    )


def run_sync(port: str | None, io_name: str) -> None:
    with FtSwarmClient.open(port) as client:
        print(f"Connected; subscribing to button {io_name}. Press Ctrl-C to stop.")
        button = client.button(io_name)
        for value in button.value.changes():
            print(f"{io_name}: {value}", flush=True)


async def run_async(port: str | None, io_name: str) -> None:
    async with await AsyncFtSwarmClient.open(port) as client:
        print(f"Connected; subscribing to button {io_name}. Press Ctrl-C to stop.")
        button = await client.button(io_name)
        async for value in button.value.changes():
            print(f"{io_name}: {value}", flush=True)


def main() -> None:
    options = arguments()
    configure_logging(options.debug)

    if options.list_ports:
        ports = discover_ports()
        print("\n".join(ports) if ports else "No ftSwarm devices found.")
        return

    try:
        if options.use_async:
            asyncio.run(run_async(options.port, options.io))
        else:
            run_sync(options.port, options.io)
    except KeyboardInterrupt:
        print("\nDisconnected.")


if __name__ == "__main__":
    main()
