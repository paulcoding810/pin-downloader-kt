package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.extractor.PinSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class PinterestExtractorTest : BehaviorSpec({

    val extractor = PinterestExtractor()

    given("Pinterest extractor") {
        `when`("extracting image content") {
            val pinUrl = "https://www.pinterest.com/pin/70298444178786767/"
            val expectedVideoUrl = null
            val expectedImageUrl =
                "https://i.pinimg.com/originals/17/8e/bf/178ebf8c048a58243707fed29787298e.jpg"

            then("should return successful result with video data") {
                val pinData = extractor.extract(pinUrl)

                pinData.source shouldBe PinSource.PINTEREST
                pinData.video shouldBe expectedVideoUrl
                pinData.image shouldBe expectedImageUrl
            }
        }

        `when`("extracting gif content") {
            val pinUrl = "https://www.pinterest.com/pin/249598004342746159/"
            val expectedImageUrl =
                "https://i.pinimg.com/originals/4b/7f/97/4b7f9782977aea6e8a59dc9d6dae0317.gif"
            val expectedThumb =
                "https://i.pinimg.com/236x/4b/7f/97/4b7f9782977aea6e8a59dc9d6dae0317.jpg"
            then("should return successful result with gif data") {
                val pinData = extractor.extract(pinUrl)

                pinData.image shouldBe expectedImageUrl
                pinData.thumbnail shouldBe expectedThumb
            }
        }

        `when`("extracting video and image") {
            val pinUrl = "https://www.pinterest.com/pin/58687601384243020/"
            val expectedVideoUrl =
                "https://v1.pinimg.com/videos/mc/720p/53/19/62/5319622e973fd1f673569bd89a4b608b.mp4"
            val expectedImageUrl =
                "https://i.pinimg.com/originals/0f/c5/dc/0fc5dc1c9001045680ddf55bdb3fe9db.jpg"

            then("should return video and image") {
                val pinData = extractor.extract(pinUrl)

                pinData.image shouldBe expectedImageUrl
                pinData.video shouldBe expectedVideoUrl
            }
        }

        `when`("extracting video content from story_pin_data") {
            val pinUrl = "https://www.pinterest.com/pin/1081989879216075566/"
            val expectedVideoUrl =
                "https://v1.pinimg.com/videos/mc/720p/fd/25/05/fd2505ae6a2ce068866cb085e488fcef.mp4"
            val expectedImageUrl =
                "https://i.pinimg.com/originals/09/66/67/09666718ec1ce7a0d500bd5dc46c2cf9.jpg"

            then("should return video from story_pin_data") {
                extractor.extract(pinUrl).run {
                    image shouldBe expectedImageUrl
                    video shouldBe expectedVideoUrl
                }
            }
        }
//        `when`("extracting video but there's no mp4") {
//            val pinUrl = "https://www.pinterest.com/pin/74239093852167971/"
//            then("should return m3u8 instead") {
//                val result = extractor.extract(pinUrl)
//
//                result.isSuccess shouldBe true
//                result.getOrNull()?.let {
////                   TODO: implement m3u8 loader
////                    it.video shouldBe "https://v1.pinimg.com/vi…cf7438ee05b86a6ef21.m3u8"
//                    it.video shouldBe null
//                    it.image shouldBe "https://i.pinimg.com/originals/da/95/7c/da957c1ca43887bbe7360f9b2237aef5.jpg"
//                }
//            }
//        }

        `when`("no media is present") {
            val pinUrl = "https://www.pinterest.com/pin/123456789/"

            then("should throw exception") {
                shouldThrow<AppException.ParseJsonError> {
                    extractor.extract(pinUrl)
                }
            }
        }
    }
})
