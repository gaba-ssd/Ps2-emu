package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EmulatorRepository
import com.example.data.StorageScannerService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StorageScannerServiceTest {

    @Test
    fun `scanDeviceStorage locates files with ps2 extensions`() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            
            // Create simulated ROMs in app files dir
            val romDir = File(context.filesDir, "roms").apply { mkdirs() }
            val testIso = File(romDir, "Shadow_of_the_Colossus.iso").apply {
                writeBytes(ByteArray(1024))
            }
            val testElf = File(romDir, "uLaunchELF_v4.42.elf").apply {
                writeBytes(ByteArray(512))
            }

            val result = StorageScannerService.scanDeviceStorage(context)
            assertNotNull(result)
            assertTrue(result.foundGames.isNotEmpty())
            
            val isoGame = result.foundGames.find { it.title.contains("Shadow of the Colossus", ignoreCase = true) }
            assertNotNull("Should find Shadow of the Colossus", isoGame)
            assertEquals("ISO", isoGame?.discFormat)

            val elfGame = result.foundGames.find { it.title.contains("uLaunchELF", ignoreCase = true) }
            assertNotNull("Should find uLaunchELF", elfGame)
            assertEquals("ELF", elfGame?.discFormat)

            // Add to repository
            val addedCount = EmulatorRepository.addScannedGames(result.foundGames)
            assertTrue(addedCount >= 2)

            // Clean up
            testIso.delete()
            testElf.delete()
        }
    }

    @Test
    fun `manual custom rom addition works`() {
        val newRom = EmulatorRepository.addCustomRom("Tekken_5.cso", "CSO", 1200f)
        assertEquals("Tekken 5", newRom.title)
        assertEquals("CSO", newRom.discFormat)
        assertTrue(EmulatorRepository.gameLibrary.value.any { it.id == newRom.id })
    }
}
