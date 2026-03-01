package com.gtxtreme.panic.listeners

import com.gtxtreme.panic.FaahStateService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

/**
 * Build failure listener using reflection to avoid version-specific imports.
 * Works with multiple versions of IntelliJ that may have BuildManagerListener
 * in different locations (com.intellij.compiler.server or com.intellij.build, etc.)
 */
class BuildFailureListener(private val project: Project) {
    private val logger = thisLogger()
    private val stateService = FaahStateService.getInstance(project)

    fun onBuildFinished(isErrors: Boolean) {
        logger.debug("Build finished: isErrors=$isErrors")

        if (isErrors) {
            logger.info("Build failed")
            stateService.enterPanic()
        } else {
            val wasInPanic = stateService.exitPanic()
            if (wasInPanic) {
                logger.info("Build succeeded after previous failure - triggering redemption")
            }
        }
    }
}
