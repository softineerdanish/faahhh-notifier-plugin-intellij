package com.gtxtreme.panic.ui

import com.gtxtreme.panic.FaahStateService
import com.gtxtreme.panic.PanicStateChangeEvent
import com.gtxtreme.panic.PanicStateListener
import com.gtxtreme.panic.audio.AudioPlayer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import javax.swing.Timer

class PanicOverlayManager(private val project: Project) : PanicStateListener {
    private val logger = thisLogger()
    private val audioPlayer = AudioPlayer()
    private var overlayComponent: PanicOverlayComponent? = null
    private var animationTimer: Timer? = null
    private val projectConnection = project.messageBus.connect()
    private val appConnection = ApplicationManager.getApplication().messageBus.connect()

    init {
        projectConnection.subscribe(FaahStateService.PANIC_STATE_TOPIC, this)
        appConnection.subscribe(FaahStateService.PANIC_STATE_TOPIC, this)
    }

    override fun onPanicStateChanged(event: PanicStateChangeEvent) {
        logger.info("[PANIC OVERLAY] State changed: isInPanic=${event.isInPanic}, panicLevel=${event.panicLevel}")
        if (event.isInPanic) {
            startPanicMode()
        } else {
            stopPanicMode()
        }
    }

    private fun startPanicMode() {
        logger.info("[PANIC OVERLAY] Starting panic mode")
        audioPlayer.playScream()

        if (overlayComponent == null) {
            try {
                val ideFrame = WindowManager.getInstance().getIdeFrame(project)
                logger.info("[PANIC OVERLAY] Got IDE frame: ${ideFrame != null}, type: ${ideFrame?.javaClass?.simpleName}")
                
                val window = ideFrame?.component
                logger.info("[PANIC OVERLAY] Window component: ${window != null}, type: ${window?.javaClass?.simpleName}")
                
                // IdeRootPane is the root pane itself, not a container
                val rootPane = when {
                    window is javax.swing.JRootPane -> window
                    window is javax.swing.RootPaneContainer -> (window as javax.swing.RootPaneContainer).rootPane
                    window?.javaClass?.simpleName == "IdeRootPane" -> {
                        // IntelliJ's custom root pane
                        window as javax.swing.JRootPane
                    }
                    else -> null
                }
                
                logger.info("[PANIC OVERLAY] RootPane found: ${rootPane != null}, type: ${rootPane?.javaClass?.simpleName}")
                
                if (rootPane != null) {
                    val layeredPane = rootPane.layeredPane
                    logger.info("[PANIC OVERLAY] LayeredPane: ${layeredPane != null}, size: ${layeredPane.width}x${layeredPane.height}")
                    
                    overlayComponent = PanicOverlayComponent(project).apply {
                        setBounds(0, 0, layeredPane.width, layeredPane.height)
                    }
                    logger.info("[PANIC OVERLAY] Created overlay component with bounds: ${overlayComponent?.bounds}")

                    layeredPane.add(overlayComponent, javax.swing.JLayeredPane.POPUP_LAYER)
                    layeredPane.setComponentZOrder(overlayComponent, 0)
                    logger.info("[PANIC OVERLAY] Added overlay to layered pane, z-order set to 0")
                    
                    overlayComponent?.startPulsing()
                    logger.info("[PANIC OVERLAY] Started pulsing animation")
                } else {
                    logger.warn("[PANIC OVERLAY] Could not find root pane")
                }
            } catch (e: Exception) {
                logger.error("[PANIC OVERLAY] Failed to create overlay: ${e.message}", e)
                return
            }
        } else {
            logger.info("[PANIC OVERLAY] Reusing existing overlay component")
            overlayComponent?.startPulsing()
        }

        if (animationTimer == null) {
            animationTimer = Timer(50) {
                overlayComponent?.updatePhase()
                overlayComponent?.repaint()
            }.apply {
                start()
            }
            logger.info("[PANIC OVERLAY] Animation timer started")
        }
    }

    private fun stopPanicMode() {
        logger.info("[PANIC OVERLAY] Stopping panic mode")
        animationTimer?.stop()
        animationTimer = null

        overlayComponent?.stopPulsing()
        if (overlayComponent != null) {
            overlayComponent?.let {
                try {
                    val window = WindowManager.getInstance().getIdeFrame(project)?.component as? javax.swing.JFrame
                    window?.rootPane?.layeredPane?.remove(it)
                    window?.rootPane?.layeredPane?.repaint()
                    logger.info("[PANIC OVERLAY] Removed overlay from layered pane")
                } catch (e: Exception) {
                    logger.error("[PANIC OVERLAY] Failed to remove overlay", e)
                }
            }
        }
    }

    fun dispose() {
        stopPanicMode()
    }
}
