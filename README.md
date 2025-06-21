# ServerConnector

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/ServerConnector)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/ServerConnector/maven.yml?branch=master)
![GitHub](https://img.shields.io/github/license/okocraft/ServerConnector)

## Features

- Access controls for connecting server based on the permissions (default: `serverconnector.connect.%server_name%`)
- Add commands `/<server-name>` that try to connect to the server
- Server join/switch/quit messages
- Fallback server when the player is kicked

## Requirements

- Java 17
- Velocity 3.4.0+
- (Optional) LuckPerms - For detecting the players who are connected for the first time

## Permissions

- `serverconnector.connect.%server_name%` - the permission to connect to the server, configurable in `config.yml`
- `serverconnector.slashserver.<server-name>` - the permission to use `/<server-name>`
- `serverconnector.slashserver.other-players` - the permission to connect other player to the server using `/<server-name> <player-name>`

## License

This project is under the Apache License version 2.0. Please see [LICENSE](LICENSE) for more info.

Copyright © 2021-2025, OKOCRAFT and Siroshun09
