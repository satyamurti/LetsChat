package com.mrspd.letschat.services


import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.FirestoreUtil

class MyFirebaseMessagingService : FirebaseMessagingService() {


    companion object {


        //FCM uses tokens to identify devices
        fun getInstanceId(): Unit {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        println("MyFirebaseMessagingService.getInstanceId:${task.exception}")
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token
                    println("MyFirebaseMessagingService.s:${token}")
                    if (token != null) {
                        addTokenToUserDocument(token)
                    }

                })

        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        println("MyFirebaseMessagingService.onNewToken:${token}")
        addTokenToUserDocument(token)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            //pushing to github
            println("MyFirebaseMessagingService.onMessageReceived:${remoteMessage.data}")
        }
    }


}

fun addTokenToUserDocument(token: String) {
    val loggedUserID = AuthUtil.firebaseAuthInstance.currentUser?.uid
    if (loggedUserID != null) {
        FirestoreUtil.firestoreInstance.collection("users").document(loggedUserID)
            .update("token", token)
    }

}