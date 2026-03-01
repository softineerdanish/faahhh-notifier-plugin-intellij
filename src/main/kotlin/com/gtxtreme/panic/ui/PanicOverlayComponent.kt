package com.gtxtreme.panic.ui

import com.gtxtreme.panic.FaahStateService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import kotlin.math.sin

class PanicOverlayComponent(private val project: Project) : JComponent() {
    private val logger = thisLogger()
    
    companion object {
        private const val ANIMATION_FREQUENCY = 2.0 * Math.PI / 120.0
        private const val BASE_ALPHA = 80
        private const val ALPHA_AMPLITUDE = 40
    }

    private var phase = 0.0
    private var isVisible = false

    init {
        isOpaque = false
        logger.info("[PANIC COMPONENT] Initialized")
    }

    fun startPulsing() {
        if (isVisible) return
        isVisible = true
        phase = 0.0
        logger.info("[PANIC COMPONENT] Pulsing started")
    }

    fun stopPulsing() {
        isVisible = false
        phase = 0.0
        logger.info("[PANIC COMPONENT] Pulsing stopped")
    }

    fun updatePhase() {
        phase += ANIMATION_FREQUENCY
        if (phase > 2.0 * Math.PI) {
            phase -= 2.0 * Math.PI
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        
        if (!isVisible) {
            return
        }

        val stateService = FaahStateService.getInstance(project)
        if (!stateService.isInPanic()) {
            stopPulsing()
            return
        }

        val g2d = g as Graphics2D
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)

        val alpha = (BASE_ALPHA + ALPHA_AMPLITUDE * sin(phase)).toInt().coerceIn(40, 200)
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)

        g2d.color = Color(255, 0, 0)
        g2d.fillRect(0, 0, width, height)

        g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
    }
}
