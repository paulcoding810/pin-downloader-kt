package com.paulcoding.pindownloader

sealed class AppException(messageRes: String) : Exception(messageRes) {
    class NetworkError : AppException("Network Error Occurred")
    class DownloadError(url: String) : AppException("Failed to download image: $url")
    class InvalidUrlError(url: String) : AppException("Invalid URL: $url")
    class PinNotFoundError(url: String) : AppException("Pin not found: $url")
    class ParseIdError(url: String) : AppException("Cannot parse Id for $url")
    class ParseJsonError(url: String) : AppException("Cannot parse JSON for $url")
    class MessageError : AppException("Cannot parse url from message")
    class UnknownError : AppException("Something went wrong")
}
