package com.paulcoding.pindownloader.helper

fun log(
    message: Any?,
    tag: String? = "pinDownloader",
) {
    val border = "*".repeat(150)
    println("\n")
    println(border)
    print("\t")
    println("$tag:")
    print("\t")
    println(message)
    println(border)
}

fun <T> T.alsoLog(tag: String? = "PaulCoding"): T {
    log(this, tag)
    return this
}
