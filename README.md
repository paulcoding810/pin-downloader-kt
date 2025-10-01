![Test](https://github.com/paulcoding810/pin-downloader-kt/actions/workflows/test.yml/badge.svg?branch=main)
![Build](https://github.com/paulcoding810/pin-downloader-kt/actions/workflows/dev.yml/badge.svg?branch=main)
![GitHub release](https://img.shields.io/github/v/release/paulcoding810/pin-downloader-kt?include_prereleases)

# PinDownloader

PinDownloader is an Android application that allows users to download images and videos from Pinterest and Pixiv.

## Features

- Download images and videos from Pinterest
- Download artwork from Pixiv
- Support for multiple media types (images, videos, GIFs)
- Clean and simple interface

## Supported Platforms

- Pinterest (pinterest.com)
- Pixiv (pixiv.net)

## URL Patterns Supported

### Pinterest
- `https://www.pinterest.com/pin/{PIN_ID}`
- `https://www.pinterest.com/board/{BOARD_ID}`

### Pixiv
- `https://www.pixiv.net/artworks/{ARTWORK_ID}`
- `https://www.pixiv.net/en/artworks/{ARTWORK_ID}`
- `https://www.pixiv.net/i/{ARTWORK_ID}`

## Technical Details

Built with:
- Kotlin
- Ktor Client for network requests
- KSoup for HTML parsing
- Kotlinx Serialization for JSON handling

## How It Works

1. The app extracts the media ID from the provided URL
2. Makes API calls to retrieve media information
3. Parses the response to extract download URLs
4. Downloads the media to the device

## Error Handling

The app handles various error cases including:
- No internet connection
- Invalid URLs
- Failed downloads
- Premium content restrictions
- Parser errors

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[MIT](LICENSE)

## Disclaimer

This app is for educational purposes only. Please respect the terms of service and copyright policies of Pinterest and Pixiv when using this application.
