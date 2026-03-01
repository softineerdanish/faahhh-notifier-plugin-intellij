package com.gtxtreme.panic.listeners

import com.gtxtreme.panic.FaahStateService
import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

class ExecutionFailureListener() : ExecutionListener {
    private val logger = thisLogger()

    init {
        logger.info("✅ [INIT] ExecutionFailureListener instantiated (application-level)")
    }

    override fun processStartScheduled(executorId: String, environment: ExecutionEnvironment) {
        logger.info("[EXECUTION] Process scheduled: $executorId - ${environment.runProfile?.name}")
    }

    override fun processStarting(executorId: String, environment: ExecutionEnvironment) {
        logger.info("[EXECUTION] Process starting: $executorId - ${environment.runProfile?.name}")
    }

    override fun processStarted(executorId: String, environment: ExecutionEnvironment, handler: ProcessHandler) {
        val runProfileName = environment.runProfile?.name ?: "Unknown"
        logger.info("[EXECUTION] Process started: $executorId - $runProfileName")
        logger.info("[EXECUTION] ✅ BUILD SUCCEEDED (processStarted called) - Exiting panic mode")
        
        try {
            val project = environment.project
            val stateService = FaahStateService.getInstance(project)
            stateService.exitPanic()
            logger.info("[EXECUTION] ✅ Redemption mode triggered!")
        } catch (e: Exception) {
            logger.error("[EXECUTION] Error triggering redemption: ${e.message}", e)
        }
    }

    override fun processNotStarted(executorId: String, environment: ExecutionEnvironment, cause: Throwable?) {
        val runProfileName = environment.runProfile?.name ?: "Unknown"
        logger.info("[EXECUTION] Process NOT started: $executorId - $runProfileName")
        logger.info("[EXECUTION] Cause: ${cause?.message}")
        
        // If process didn't start for ANY reason during a run attempt, it's likely a build failure
        // Treat this as a build failure and enter panic mode
        logger.info("[EXECUTION] 🔴 BUILD FAILED (processNotStarted called)")
        try {
            val project = environment.project
            val stateService = FaahStateService.getInstance(project)
            stateService.enterPanic()
            logger.info("[EXECUTION] ✅ Panic mode triggered!")
        } catch (e: Exception) {
            logger.error("[EXECUTION] Error triggering panic: ${e.message}", e)
        }
    }

    override fun processTerminated(
        executorId: String,
        environment: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int
    ) {
        val runProfileName = environment.runProfile?.name ?: "Unknown"
        val project = environment.project
        
        logger.info("[EXECUTION] ====================================")
        logger.info("[EXECUTION] Process terminated: $executorId")
        logger.info("[EXECUTION] Run profile: $runProfileName")
        logger.info("[EXECUTION] Project: ${project?.name}")
        logger.info("[EXECUTION] Exit code: $exitCode")
        logger.info("[EXECUTION] ====================================")
        
        try {
            val stateService = FaahStateService.getInstance(project)
            when {
                exitCode != 0 -> {
                    logger.info("[EXECUTION] 🔴 BUILD FAILED (exit code: $exitCode)")
                    logger.info("[EXECUTION] Entering PANIC MODE now!")
                    stateService.enterPanic()
                }
                else -> {
                    logger.info("[EXECUTION] ✅ BUILD SUCCEEDED (exit code: $exitCode)")
                    logger.info("[EXECUTION] Exiting panic mode")
                    stateService.exitPanic()
                }
            }
        } catch (e: Exception) {
            logger.error("[EXECUTION] Error getting FaahStateService: ${e.message}", e)
        }
    }
}
