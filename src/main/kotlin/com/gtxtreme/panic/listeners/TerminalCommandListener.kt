package com.gtxtreme.panic.listeners

import com.gtxtreme.panic.FaahStateService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

class TerminalCommandListener(
    private val project: Project,
    private val scope: CoroutineScope
) {
    private val logger = thisLogger()
    private val stateService = FaahStateService.getInstance(project)

    fun attachToShellIntegration(shellIntegration: Any) {
        try {
            logger.info("🔗 Attaching to shell integration: ${shellIntegration.javaClass.simpleName}")
            
            // Official API: TerminalShellIntegration.addCommandExecutionListener(listener)
            val addListenerMethod = shellIntegration.javaClass.getMethod(
                "addCommandExecutionListener",
                Class.forName("kotlin.jvm.functions.Function1")
            )
            
            // Create a lambda listener for command execution
            val listener: (Any) -> Unit = { commandBlock ->
                logger.info("📝 Command block received: ${commandBlock.javaClass.simpleName}")
                handleCommandExecuted(commandBlock)
            }
            
            addListenerMethod.invoke(shellIntegration, listener)
            logger.info("✅ Successfully attached command execution listener")
        } catch (e: Exception) {
            logger.error("❌ Failed to attach command listener: ${e.message}", e)
        }
    }

    private fun handleCommandExecuted(commandBlock: Any) {
        try {
            logger.info("🎯 Processing command block...")
            
            // Official API: TerminalCommandBlock.getExitCode()
            val exitCodeMethod = commandBlock.javaClass.getMethod("getExitCode")
            val exitCode = exitCodeMethod.invoke(commandBlock) as? Int
            
            logger.info("📊 Command exit code: $exitCode")

            if (exitCode != null && exitCode != 0) {
                logger.error("🔴 TERMINAL COMMAND FAILED with exit code: $exitCode - ENTERING PANIC!")
                stateService.enterPanic()
            } else if (exitCode == 0) {
                logger.info("✅ TERMINAL COMMAND SUCCEEDED with exit code: 0")
                val wasInPanic = stateService.exitPanic()
                if (wasInPanic) {
                    logger.info("🟢 WAS IN PANIC - TRIGGERING REDEMPTION!")
                }
            }
        } catch (e: Exception) {
            logger.error("❌ Error processing command block: ${e.message}", e)
        }
    }
}
