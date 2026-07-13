# Nova Radar – Android IP Scanner

A powerful Android IP scanner with real-time TCP/TLS verification. Scan Cloudflare IP ranges, test latency, and export results with Nova Proxy suffix or plain IP:Port format.

## Features

- **Two-Phase Scanning**: Quick TCP connect + deep TLS handshake verification
- **Multiple IP Sources**: Cloudflare, Akamai, Vercel CIDR ranges
- **Dual Output**: Nova Proxy suffix (`#Nova-id`) or plain `ip:port`
- **Real-time Results**: Live probe feed, latency sorting, operator detection
- **Modern UI**: Material 3 with dark/light theme, Persian/English support
- **Import IP**: Manual paste or auto-generate by operator (MCI, MTN, ICT)
- **Cloudflare Worker Deployment**: Deploy VLESS proxy directly from the app
- **Speed Test**: Per-IP latency and bandwidth testing

## Download

[Latest Release](https://github.com/TheCanniball/NovaRadar/releases)

## Build

```bash
cd "nova radar android"
./gradlew assembleRelease
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository + Room
- **Scanner**: Raw sockets (TCP) + SSLContext (TLS)
- **Min SDK**: 24 (Android 7.0)
- **Package**: `com.novascanner.network`

## License

MIT
