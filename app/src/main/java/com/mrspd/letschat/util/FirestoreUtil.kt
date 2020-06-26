package com.mrspd.letschat.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings




object FirestoreUtil {

    val firestoreInstance: FirebaseFirestore by lazy {

        val firebaseFirestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        firebaseFirestore.firestoreSettings = settings

        firebaseFirestore
    }
}