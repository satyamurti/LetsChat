package com.mrspd.letschat.util

import com.google.firebase.storage.FirebaseStorage

object StorageUtil {

    val storageInstance: FirebaseStorage by lazy {
        println("StorageUtil.:")
        FirebaseStorage.getInstance()

    }
}