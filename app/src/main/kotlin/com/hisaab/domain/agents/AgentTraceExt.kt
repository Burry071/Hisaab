package com.hisaab.domain.agents

import com.hisaab.domain.model.AgentName
import com.hisaab.domain.model.AgentTaskStatus
import com.hisaab.domain.model.AgentTrace
import com.hisaab.domain.model.AgentTraceStep

/**
 * Convenience extension so every agent can call `trace.step(...)` instead of
 * constructing an [AgentTraceStep] manually each time.
 */
internal fun AgentTrace.step(
    agent:      AgentName,
    task:       String,
    detail:     String      = "",
    toolCall:   String?     = null,
    toolResult: String?     = null,
    status:     AgentTaskStatus = AgentTaskStatus.RUNNING,
) = addStep(
    AgentTraceStep(
        agentName   = agent,
        taskName    = task,
        detail      = detail,
        status      = status,
        timestampMs = System.currentTimeMillis(),
        toolCall    = toolCall,
        toolResult  = toolResult,
    )
)
