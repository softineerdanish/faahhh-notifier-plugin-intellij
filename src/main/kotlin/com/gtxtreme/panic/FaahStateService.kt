package com.gtxtreme.panic

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

data class PanicStateChangeEvent(val isInPanic: Boolean, val panicLevel: Int)

interface PanicStateListener {
    fun onPanicStateChanged(event: PanicStateChangeEvent)
}

@Service(Service.Level.PROJECT)
@State(name = "FaahState", storages = [Storage("faah_state.xml")])
class FaahStateService(private val project: Project) : PersistentStateComponent<FaahStateService.State> {

    data class State(
        var panicLevel: Int = 0,
        var lastFailureTimestamp: Long = 0L
    )

    private var myState = State()
    private val logger = thisLogger()

    companion object {
        val PANIC_STATE_TOPIC = Topic("PanicStateChanged", PanicStateListener::class.java)

        fun getInstance(project: Project): FaahStateService {
            return project.getService(FaahStateService::class.java)
        }
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    fun isInPanic(): Boolean = myState.panicLevel > 0

    fun getPanicLevel(): Int = myState.panicLevel

    fun enterPanic() {
        myState.panicLevel++
        myState.lastFailureTimestamp = System.currentTimeMillis()
        notifyStateChanged()
    }

    fun exitPanic(): Boolean {
        val wasInPanic = isInPanic()
        if (wasInPanic) {
            myState.panicLevel = 0
            notifyStateChanged()
        }
        return wasInPanic
    }

    private fun notifyStateChanged() {
        val event = PanicStateChangeEvent(isInPanic = isInPanic(), panicLevel = myState.panicLevel)
        project.messageBus.syncPublisher(PANIC_STATE_TOPIC).onPanicStateChanged(event)
        ApplicationManager.getApplication().messageBus.syncPublisher(PANIC_STATE_TOPIC).onPanicStateChanged(event)
    }
}
