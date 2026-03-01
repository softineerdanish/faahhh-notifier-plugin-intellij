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

class RedemptionManager(private val project: Project) : PanicStateListener {
    private val logger = thisLogger()
    private val audioPlayer = AudioPlayer()
    private var overlayComponent: RedemptionOverlayComponent? = null
    private var repaintTimer: Timer? = null
    private val projectConnection = project.messageBus.connect()
    private val appConnection = ApplicationManager.getApplication().messageBus.connect()

    init {
        projectConnection.subscribe(FaahStateService.PANIC_STATE_TOPIC, this)
        appConnection.subscribe(FaahStateService.PANIC_STATE_TOPIC, this)
    }

    override fun onPanicStateChanged(event: PanicStateChangeEvent) {
        logger.info("[REDEMPTION OVERLAY] State changed: isInPanic=${event.isInPanic}")
        if (!event.isInPanic) {
            showRedemption()
        }
    }

    private fun showRedemption() {
        logger.info("[REDEMPTION OVERLAY] Starting redemption mode")
        audioPlayer.playMissionPassed()

        if (overlayComponent == null) {
            try {
                val ideFrame = WindowManager.getInstance().getIdeFrame(project)
                logger.info("[REDEMPTION OVERLAY] Got IDE frame: ${ideFrame != null}, type: ${ideFrame?.javaClass?.simpleName}")
                
                val window = ideFrame?.component
                logger.info("[REDEMPTION OVERLAY] Window component: ${window != null}, type: ${window?.javaClass?.simpleName}")
                
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
                
                logger.info("[REDEMPTION OVERLAY] RootPane found: ${rootPane != null}, type: ${rootPane?.javaClass?.simpleName}")
                
                if (rootPane != null) {
                    val layeredPane = rootPane.layeredPane
                    logger.info("[REDEMPTION OVERLAY] LayeredPane: ${layeredPane != null}, size: ${layeredPane.width}x${layeredPane.height}")
                    
                    overlayComponent = RedemptionOverlayComponent(project).apply {
                        setBounds(0, 0, layeredPane.width, layeredPane.height)
                    }
                    logger.info("[REDEMPTION OVERLAY] Created overlay component with bounds: ${overlayComponent?.bounds}")

                    layeredPane.add(overlayComponent, javax.swing.JLayeredPane.POPUP_LAYER)
                    layeredPane.setComponentZOrder(overlayComponent, 0)
                    logger.info("[REDEMPTION OVERLAY] Added overlay to layered pane, z-order set to 0")
                    
                    overlayComponent?.displayRedemption()
                    logger.info("[REDEMPTION OVERLAY] Display redemption called")
                } else {
                    logger.warn("[REDEMPTION OVERLAY] Could not find root pane")
                }
            } catch (e: Exception) {
                logger.error("[REDEMPTION OVERLAY] Failed to create overlay: ${e.message}", e)
                return
            }
        } else {
            logger.info("[REDEMPTION OVERLAY] Reusing existing overlay component")
            overlayComponent?.displayRedemption()
        }

        if (repaintTimer == null) {
            repaintTimer = Timer(50) {
                if (overlayComponent?.isRedemptionActive() == false) {
                    repaintTimer?.stop()
                    repaintTimer = null
                    cleanupOverlay()
                    logger.info("[REDEMPTION OVERLAY] Redemption timeout - cleaning up")
                } else {
                    overlayComponent?.repaint()
                }
            }.apply {
                start()
            }
            logger.info("[REDEMPTION OVERLAY] Repaint timer started")
        }
    }

    private fun cleanupOverlay() {
        try {
            val window = WindowManager.getInstance().getIdeFrame(project)?.component as? javax.swing.JFrame
            if (window != null && overlayComponent != null) {
                window.rootPane.layeredPane.remove(overlayComponent)
                window.rootPane.layeredPane.repaint()
                logger.info("[REDEMPTION OVERLAY] Removed overlay from layered pane")
            }
        } catch (e: Exception) {
            logger.error("[REDEMPTION OVERLAY] Failed to clean up overlay", e)
        }
    }

    fun dispose() {
        repaintTimer?.stop()
        repaintTimer = null
        cleanupOverlay()
    }
}
