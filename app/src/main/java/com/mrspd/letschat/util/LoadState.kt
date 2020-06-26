package com.mrspd.letschat.util

class ErrorMessage {
    companion object {
        var errorMessage: String? = "Something went wrong"
    }
}

enum class LoadState {
    SUCCESS, FAILURE, LOADING
}
