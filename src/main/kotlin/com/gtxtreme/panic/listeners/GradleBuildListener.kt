package com.gtxtreme.panic.listeners

import com.gtxtreme.panic.FaahStateService
import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FailureResult
import com.intellij.build.events.FinishBuildEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

class GradleBuildListener(val project: Project) : BuildProgressListener {
    private val logger = thisLogger()
    private val stateService: FaahStateService? = try {
        FaahStateService.getInstance(project)
    } catch (e: Exception) {
        logger.warn("⚠️ Failed to get FaahStateService: ${e.message}")
        null
    }

    init {
        logger.info("✅ [GRADLE LISTENER] GradleBuildListener instantiated for project: ${project.name}")
    }

    override fun onEvent(buildId: Any, event: BuildEvent) {
        logger.info("[GRADLE LISTENER] onEvent called: buildId=$buildId, eventClass=${event::class.simpleName}")
        
        // Listen for build finish events
        if (event is FinishBuildEvent && stateService != null) {
            logger.info("[GRADLE BUILD] Build finished: ${event.message}")
            
            // Check if build has failure result
            val hasFailed = event.result is FailureResult
            
            when {
                hasFailed -> {
                    logger.info("[GRADLE BUILD] 🔴 BUILD FAILED - Entering panic mode")
                    stateService.enterPanic()
                }
                !hasFailed -> {
                    logger.info("[GRADLE BUILD] ✅ BUILD SUCCEEDED")
                    stateService.exitPanic()
                }
            }
        }
    }
}
