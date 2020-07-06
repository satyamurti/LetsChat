package com.mrspd.letschat.models


data class GroupName(
    var group_name: String? = null,
    var description: String? = null,
    var imageurl: String? = null,
    var chat_members_in_group: List<String>? = null
//    var lastMessageDate: Map<String, Double>? = null,
//    var isLoggedUser: Boolean? = null,
//    var lastMessageType: Double? = null
)
