package com.paulcoding.pindownloader.extractor.pinterest

import com.paulcoding.pindownloader.AppException
import com.paulcoding.pindownloader.di.appModule
import com.paulcoding.pindownloader.di.networkModule
import com.paulcoding.pindownloader.extractor.PinSource
import com.paulcoding.pindownloader.extractor.pixiv.PixivExtractor
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
class PixivExtractorTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var extractor: PixivExtractor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        startKoin {
            modules(appModule)
            modules(networkModule)
        }

        extractor = PixivExtractor()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `extracting image content`() = runTest {
        val link = "https://www.pixiv.net/en/artworks/80148063"
        val expectedUrl =
            "https://i.pximg.net/img-original/img/2020/03/16/00/01/06/80148063_p0.png"

        val pinData = extractor.extract(link)

        assertEquals(pinData.source, PinSource.PIXIV)
        assertEquals(pinData.image, expectedUrl)
        assertEquals(pinData.video, null)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `should throw exception`() = runTest {
        val pixivUrl = "https://www.pixiv.net/en/artworks/1/"

        assertThrows(AppException.ParseJsonError::class.java) {
            runBlocking {
                extractor.extract(pixivUrl)
            }
        }
    }
}
