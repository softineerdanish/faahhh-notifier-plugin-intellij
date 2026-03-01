package com.gtxtreme.panic.actions

import com.gtxtreme.panic.FaahStateService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger

/**
 * Test action for Panic Mode
 * 
 * REAL-WORLD BEHAVIOR:
 * In production, each build failure triggers panic mode and plays the scream.
 * Multiple failures in a row will each play the scream independently.
 * To stop the panic, you need a successful build (exit panic).
 */
class TestPanicAction : AnAction("Test Panic Mode") {
    private val logger = thisLogger()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val stateService = FaahStateService.getInstance(project)
        
        logger.info("🎬 TEST ACTION: Triggering panic mode")
        // Each call triggers the scream (mimics multiple build failures)
        stateService.enterPanic()
    }
}

class TestRedemptionAction : AnAction("Test Redemption Mode") {
    private val logger = thisLogger()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val stateService = FaahStateService.getInstance(project)
        
        logger.info("🎬 TEST ACTION: Triggering redemption mode manually")
        stateService.exitPanic()
    }
}
