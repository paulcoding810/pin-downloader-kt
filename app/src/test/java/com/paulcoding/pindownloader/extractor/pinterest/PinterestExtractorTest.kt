package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.di.appModule
import com.paulcoding.pindownloader.di.networkModule
import com.paulcoding.pindownloader.extractor.PinSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PinterestExtractorTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var extractor: PinterestExtractor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(appModule)
            modules(networkModule)
        }

        extractor = PinterestExtractor()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `extracting image content`() = runTest {

        val pinUrl = "https://www.pinterest.com/pin/70298444178786767/"
        val expectedVideoUrl = null
        val expectedImageUrl =
            "https://i.pinimg.com/originals/17/8e/bf/178ebf8c048a58243707fed29787298e.jpg"

        val pinData = extractor.extract(pinUrl)

        assertEquals(pinData.source, PinSource.PINTEREST)
        assertEquals(pinData.video, expectedVideoUrl)
        assertEquals(pinData.image, expectedImageUrl)

        testDispatcher.scheduler.advanceUntilIdle()
    }


    @Test
    fun `should return video and image`() = runTest {
        val pinUrl = "https://www.pinterest.com/pin/58687601384243020/"
        val expectedVideoUrl =
            "https://v1.pinimg.com/videos/mc/720p/53/19/62/5319622e973fd1f673569bd89a4b608b.mp4"
        val expectedImageUrl =
            "https://i.pinimg.com/originals/0f/c5/dc/0fc5dc1c9001045680ddf55bdb3fe9db.jpg"

        extractor.extract(pinUrl).run {
            assertEquals(PinSource.PINTEREST, source)
            assertEquals(expectedImageUrl, image)
            assertEquals(expectedVideoUrl, video)
        }
    }

    @Test
    fun `should return video from story_pin_data`() = runTest {
        val pinUrl = "https://www.pinterest.com/pin/297096906690237209/"
        val expectedVideoUrl =
            "https://v1.pinimg.com/videos/iht/expMp4/62/ce/32/62ce32b6898266fe292aedd1d35ba1a8_720w.mp4"
        val expectedImageUrl =
            "https://i.pinimg.com/originals/94/6f/a1/946fa14cf72a33ff6810936d0dcdfeb7.jpg"

        extractor.extract(pinUrl).run {
            assertEquals(PinSource.PINTEREST, source)
            assertEquals(expectedImageUrl, image)
            assertEquals(expectedVideoUrl, video)
        }
    }

    @Test
    fun `should throw exception`() = runTest {
        val pinUrl = "https://www.pinterest.com/pin/123456789/"

        assertThrows(AppException.ParseJsonError::class.java) {
            runBlocking {
                extractor.extract(pinUrl)
            }
        }
    }
}
