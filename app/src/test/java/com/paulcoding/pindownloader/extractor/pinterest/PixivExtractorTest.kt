package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PixivExtractorTest :
    BehaviorSpec({
        val extractor = PixivExtractor()

        given("Pixiv Extractor") {
            `when`("extracting image content") {
                val link = "https://www.pixiv.net/en/artworks/80148063"
                val expectedUrl =
                    "https://i.pximg.net/img-original/img/2020/03/16/00/01/06/80148063_p0.png"
                then("should return original url") {
                    val result = extractor.extract(link)

                    result.isSuccess shouldBe true

                    result.getOrNull()?.let {
                        it.source shouldBe PinSource.PIXIV
                        it.image shouldBe expectedUrl
                    }
                }
            }
        }
    })
