package com.demo.mobilebankingassistant.data

enum class ChatRole {
    USER, ASSISTANT, SYSTEM
}

enum class ChatAction {
    CHANGE_PIN, CHANGE_ADDRESS, BLOCK_CREDIT_CARD, UNBLOCK_CREDIT_CARD
}

data class AssistantChatMessage(
    val role: ChatRole,
    val text: String,
    val routeLabel: String? = null,
    val action: ChatAction? = null,
    val chart: List<SpendingChartSlice> = emptyList()
)
