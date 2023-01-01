# ServerConnector

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/okocraft/ServerConnector)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/okocraft/ServerConnector/maven.yml?branch=master)
![GitHub](https://img.shields.io/github/license/okocraft/ServerConnector)

## Features

- サーバーごとの接続権限に基づくアクセス制限 (`bungeecord.server.<name>`)
- `/<server-name>` での接続試行コマンド
- 多言語化可能なサーバー参加/移動/退出/その他各メッセージ
- サーバーから kick された際のサーバー転送機能
- スナップショットサーバーへのダイレクト接続 (BungeeCord の対応状況に依存)

## Permissions

- `bungeecord.server.<server-name>` - `<server-name>` に接続する権限
- `serverconnector.slashserver.<server-name>` - `/<server-name>` の使用権限
- `serverconnector.slashserver.other-players` - `/<server-name> <player-name>` で他人を他のサーバーに移動させる権限
