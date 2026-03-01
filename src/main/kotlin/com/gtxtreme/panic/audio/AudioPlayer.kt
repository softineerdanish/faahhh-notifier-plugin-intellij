package com.gtxtreme.panic.audio

import com.intellij.openapi.diagnostic.thisLogger
import com.gtxtreme.panic.FaahStateService
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class AudioPlayer {
    private val logger = thisLogger()

    fun playScream() {
        Thread {
            try {
                playBundledAudio("audio/scream.mp3")
            } catch (e: Exception) {
                logger.error("Error playing scream audio", e)
            }
        }.start()
    }

    fun playMissionPassed() {
        logger.info("[AUDIO] playMissionPassed() called")
        Thread {
            try {
                logger.info("[AUDIO] Starting mission_passed playback in thread")
                playBundledAudio("audio/mission_passed.mp3")
                logger.info("[AUDIO] mission_passed playback completed")
            } catch (e: Exception) {
                logger.error("[AUDIO] Error playing mission passed audio", e)
            }
        }.start()
    }

    private fun playBundledAudio(resourcePath: String) {
        logger.info("[AUDIO] playBundledAudio called for: $resourcePath")
        val classloaders = listOf(
            AudioPlayer::class.java.classLoader,
            Thread.currentThread().contextClassLoader,
            FaahStateService::class.java.classLoader
        )
        
        val inputStream = classloaders
            .mapNotNull { it.getResourceAsStream(resourcePath) }
            .firstOrNull()
        
        logger.info("[AUDIO] Found resource stream: ${inputStream != null}")
        
        if (inputStream == null) {
            logger.info("[AUDIO] Resource not found in classpath, trying Downloads")
            tryPlayFromDownloads(resourcePath)
            return
        }

        var tempFile: File? = null
        try {
            val tempFilePath = Files.createTempFile("panic-audio-", ".mp3")
            tempFile = tempFilePath.toFile()
            logger.info("[AUDIO] Created temp file: ${tempFile.absolutePath}")
            
            Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING)
            logger.info("[AUDIO] Copied resource to temp file")
            playAudioFile(tempFile)
        } catch (e: Exception) {
            logger.error("[AUDIO] Error during playback: ${e.message}", e)
            if (tempFile != null) {
                logger.info("[AUDIO] Trying afplay as fallback")
                tryPlayWithAFPlay(tempFile.absolutePath)
            }
        } finally {
            inputStream.close()
            if (tempFile != null) {
                Thread {
                    Thread.sleep(3000)
                    try {
                        tempFile.delete()
                    } catch (e: Exception) {
                        // Ignore cleanup errors
                    }
                }.start()
            }
        }
    }

    private fun playAudioFile(audioFile: File) {
        val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
        audioInputStream.use { stream ->
            val format = stream.format
            val info = javax.sound.sampled.DataLine.Info(javax.sound.sampled.Clip::class.java, format)
            val clip = AudioSystem.getLine(info) as javax.sound.sampled.Clip

            clip.use { c ->
                c.open(stream)
                c.start()
                while (c.isRunning) {
                    Thread.sleep(10)
                }
            }
        }
    }

    private fun tryPlayWithAFPlay(filePath: String) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("afplay", filePath))
            Thread {
                process.waitFor()
            }.start()
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    private fun tryPlayFromDownloads(resourcePath: String) {
        try {
            val fileName = resourcePath.substringAfterLast("/")
            val downloadsFile = File(System.getProperty("user.home"), "Downloads/$fileName")
            
            if (downloadsFile.exists()) {
                val process = Runtime.getRuntime().exec(arrayOf("afplay", downloadsFile.absolutePath))
                Thread {
                    process.waitFor()
                }.start()
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
}
