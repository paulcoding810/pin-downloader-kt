package com.paulcoding.pindownloader.helper

import com.paulcoding.pindownloader.BuildConfig

fun log(
    message: Any?,
    tag: String? = "pinDownloader",
) {
    if (BuildConfig.DEBUG) {
        val border = "*".repeat(150)
        println("\n")
        println(border)
        print("\t")
        println("$tag:")
        print("\t")
        println(message)
        println(border)
        println("\n")
    }
}

fun <T> T.alsoLog(tag: String? = "pinDownloader"): T {
    log(this, tag)
    return this
}
