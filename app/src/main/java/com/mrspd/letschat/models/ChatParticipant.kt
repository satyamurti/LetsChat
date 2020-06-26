package com.mrspd.letschat.models


data class ChatParticipant(
    var particpant: User? = null,
    var lastMessage: String? = null,
    var lastMessageDate: Map<String, Double>? = null,
    var isLoggedUser: Boolean? = null,
    var lastMessageType: Double? = null
)
