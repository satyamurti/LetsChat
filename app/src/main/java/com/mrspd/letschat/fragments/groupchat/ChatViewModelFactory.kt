package com.mrspd.letschat.fragments.groupchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(
    private val senderId: String?,
    private val receiverId: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(senderId, receiverId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
