package com.gtxtreme.panic.startup

import com.gtxtreme.panic.listeners.SimpleBuildListener
import com.gtxtreme.panic.ui.PanicOverlayManager
import com.gtxtreme.panic.ui.RedemptionManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class PanicPluginStartup : ProjectActivity {
    private val logger = thisLogger()

    override suspend fun execute(project: Project) {
        logger.info("🚀 Initializing Panic Plugin")
        
        // Initialize UI managers for panic and redemption states
        PanicOverlayManager(project)
        RedemptionManager(project)

        logger.info("✅ ExecutionListener registered via plugin.xml")
        
        // Try to subscribe to BuildProgressListener via message bus
        subscribeToBuildEvents(project)
        
        logger.info("🎯 Panic Plugin initialized - Ready to scream!")
    }

    private fun subscribeToBuildEvents(project: Project) {
        try {
            logger.info("ℹ️ Attempting to subscribe to build events...")
            
            // Get BuildProgressListener class
            val buildListenerClass = Class.forName("com.intellij.build.BuildProgressListener")
            logger.info("✅ BuildProgressListener class found")
            
            // Try to get TOPIC field
            val topicField = buildListenerClass.getDeclaredField("TOPIC")
            topicField.isAccessible = true
            logger.info("✅ TOPIC field found and made accessible")
            
            val topic = topicField.get(null)
            logger.info("✅ TOPIC value obtained: ${topic?.javaClass?.name}")
            
            // Subscribe
            val buildConnection = project.messageBus.connect()
            val buildListener = SimpleBuildListener(project)
            
            // Use the generic subscribe method
            val subscribeMethod = buildConnection::class.java.getDeclaredMethod(
                "subscribe",
                Class.forName("com.intellij.util.messages.Topic"),
                Any::class.java
            )
            subscribeMethod.isAccessible = true
            subscribeMethod.invoke(buildConnection, topic, buildListener)
            
            logger.info("✅ SimpleBuildListener subscribed successfully!")
        } catch (e: NoSuchFieldException) {
            logger.warn("⚠️ TOPIC field not found: ${e.message}")
        } catch (e: ClassNotFoundException) {
            logger.warn("⚠️ BuildProgressListener class not found: ${e.message}")
        } catch (e: Exception) {
            logger.warn("⚠️ Error subscribing to build events: ${e.message}")
        }
    }
}
