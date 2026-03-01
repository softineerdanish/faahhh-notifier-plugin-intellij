package com.gtxtreme.panic.ui

import com.gtxtreme.panic.FaahStateService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JComponent

class RedemptionOverlayComponent(private val project: Project) : JComponent() {
    private val logger = thisLogger()
    
    private var isVisible = false
    private var startTime = 0L
    private val displayDurationMs = 5000
    private var missionPassedImage: BufferedImage? = null

    init {
        isOpaque = false
        logger.info("[REDEMPTION COMPONENT] Initialized")
        loadMissionPassedImage()
    }

    private fun loadMissionPassedImage() {
        try {
            // Try to load mission_passed.png from resources
            val resourceStream = this::class.java.classLoader.getResourceAsStream("images/mission_passed.png")
            if (resourceStream != null) {
                missionPassedImage = ImageIO.read(resourceStream)
                logger.info("[REDEMPTION COMPONENT] Loaded mission_passed.png: ${missionPassedImage?.width}x${missionPassedImage?.height}")
            } else {
                logger.warn("[REDEMPTION COMPONENT] mission_passed.png not found in resources")
            }
        } catch (e: Exception) {
            logger.error("[REDEMPTION COMPONENT] Failed to load mission_passed image: ${e.message}", e)
        }
    }

    fun displayRedemption() {
        isVisible = true
        startTime = System.currentTimeMillis()
        logger.info("[REDEMPTION COMPONENT] Display started")
    }

    fun isRedemptionActive(): Boolean {
        if (!isVisible) return false
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed > displayDurationMs) {
            isVisible = false
            logger.info("[REDEMPTION COMPONENT] Display expired (${elapsed}ms > ${displayDurationMs}ms)")
            return false
        }
        return true
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        
        if (!isRedemptionActive()) {
            return
        }

        val g2d = g as Graphics2D

        // Full-screen semi-transparent white overlay (very light)
        // Works for both dark and light themes - creates subtle focus effect
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f)
        g2d.color = Color(255, 255, 255)  // White overlay
        g2d.fillRect(0, 0, width, height)

        // Draw the mission passed image at top-center
        if (missionPassedImage != null) {
            val img = missionPassedImage!!
            val imgWidth = img.width
            val imgHeight = img.height
            
            // Position at top-center (20% from top of screen)
            val x = (width - imgWidth) / 2
            val y = (height * 0.2).toInt()
            
            // Draw image with full opacity
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
            g2d.drawImage(img, x, y, null)
            
            logger.info("[REDEMPTION COMPONENT] Drew image at ($x, $y), image size: ${imgWidth}x${imgHeight}")
        } else {
            logger.warn("[REDEMPTION COMPONENT] No image loaded, skipping paint")
        }
    }
}
