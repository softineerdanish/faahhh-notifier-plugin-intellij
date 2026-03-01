package com.gtxtreme.panic.listeners

import com.gtxtreme.panic.FaahStateService
import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.events.SuccessResult
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

/**
 * Listens to Gradle build events via the BuildProgressListener interface.
 * This is instantiated by the IDE framework and receives all build events.
 */
class SimpleBuildListener(val project: Project) : BuildProgressListener {
    private val logger = thisLogger()
    private val stateService: FaahStateService? = try {
        FaahStateService.getInstance(project)
    } catch (e: Exception) {
        logger.warn("⚠️ Failed to get FaahStateService: ${e.message}")
        null
    }

    init {
        logger.info("✅ [BUILD LISTENER] SimpleBuildListener instantiated for project: ${project.name}")
    }

    override fun onEvent(buildId: Any, event: BuildEvent) {
        logger.info("[BUILD LISTENER] Event: buildId=$buildId, eventType=${event::class.simpleName}, message=${event.message}")
        
        if (event is FinishBuildEvent) {
            logger.info("[BUILD LISTENER] Build finished event received")
            
            val hasFailed = when (event.result) {
                is FailureResult -> {
                    logger.info("[BUILD LISTENER] Result is FailureResult")
                    true
                }
                is SuccessResult -> {
                    logger.info("[BUILD LISTENER] Result is SuccessResult")
                    false
                }
                else -> {
                    logger.info("[BUILD LISTENER] Result type: ${event.result::class.simpleName}")
                    false
                }
            }
            
            if (stateService != null) {
                when {
                    hasFailed -> {
                        logger.info("[BUILD LISTENER] 🔴 BUILD FAILED - Entering panic mode")
                        stateService.enterPanic()
                    }
                    else -> {
                        logger.info("[BUILD LISTENER] ✅ BUILD SUCCEEDED")
                        stateService.exitPanic()
                    }
                }
            }
        }
    }
}
