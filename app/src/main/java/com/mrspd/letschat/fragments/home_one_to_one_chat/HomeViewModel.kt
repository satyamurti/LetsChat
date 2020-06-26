package com.mrspd.letschat.fragments.home_one_to_one_chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrspd.letschat.models.ChatParticipant
import com.google.firebase.firestore.Query
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.FirestoreUtil
import java.util.*


class HomeViewModel : ViewModel() {

    var calledBefore = false
    init {
        getUserData()
    }

    private val chatParticipantList: MutableList<ChatParticipant> by lazy { mutableListOf<ChatParticipant>() }
    private val chatParticipantsListMutableLiveData =
        MutableLiveData<MutableList<ChatParticipant>>()
    val loggedUserMutableLiveData = MutableLiveData<User>()


    fun getChats(loggedUser: User): LiveData<MutableList<ChatParticipant>>? {

        //this method is called each time user document changes but i want to attach listener only once so check with calledBefore
        if (calledBefore) {
            return chatParticipantsListMutableLiveData

        }

        calledBefore = true

        val loggedUserId = loggedUser.uid.toString()

        val query: Query = FirestoreUtil.firestoreInstance.collection("messages")
            .whereArrayContains("chat_members", loggedUserId)

        query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {

                chatParticipantList.clear()

                if (!querySnapshot?.documents.isNullOrEmpty()) {
                    //user has chats , now get last message and receiver user
                    querySnapshot?.documents?.forEach { messageDocument ->

                        val chatParticipant = ChatParticipant()

                        //get last message & last message sender
                        val messagesList =
                            messageDocument.get("messages") as List<HashMap<String, Any>>?
                        val lastMessage = messagesList?.get(messagesList.size - 1)

                        //get message or photo url depending on last message type

                        val lastMessageType = lastMessage?.get("type") as Double?
                        chatParticipant.lastMessage = lastMessage?.get("text") as String?
                        chatParticipant.lastMessageType = lastMessageType
                        chatParticipant.lastMessageDate =
                            lastMessage?.get("created_at") as HashMap<String, Double>?
                        println("HomeViewModel.getChats:${chatParticipant.lastMessageDate?.get("seconds")}")
                        val lastMessageOwnerId = lastMessage?.get("from") as String?


                        //set isLoggedUser to know if logged user typed last message or not
                        chatParticipant.isLoggedUser = (lastMessageOwnerId == loggedUserId)

                        //get other chat participant id and use it to get his information
                        if (lastMessageOwnerId == loggedUserId) {
                            val recipient = lastMessage?.get("to") as String?
                            if (recipient != null) {
                                FirestoreUtil.firestoreInstance.collection("users")
                                    .document(recipient).get()
                                    .addOnSuccessListener { chatMember ->
                                        FirestoreUtil.firestoreInstance.collection("users")
                                            .document(recipient).get().addOnSuccessListener {
                                                val particpant = it.toObject(User::class.java)
                                                chatParticipant.particpant = particpant
                                                chatParticipantList.add(chatParticipant)
                                                chatParticipantsListMutableLiveData.value =
                                                    chatParticipantList

                                            }.addOnFailureListener {

                                            }
                                    }
                            }
                        } else {
                            val sender = lastMessage?.get("from") as String?
                            if (sender != null) {
                                FirestoreUtil.firestoreInstance.collection("users")
                                    .document(sender).get()
                                    .addOnSuccessListener { chatMember ->
                                        FirestoreUtil.firestoreInstance.collection("users")
                                            .document(sender).get().addOnSuccessListener {
                                                val particpant = it.toObject(User::class.java)
                                                chatParticipant.particpant = particpant
                                                chatParticipantList.add(chatParticipant)
                                                chatParticipantsListMutableLiveData.value =
                                                    chatParticipantList

                                            }.addOnFailureListener {

                                            }
                                    }
                            }
                        }

                    }
                } else {
                    //user has no chats
                    chatParticipantsListMutableLiveData.value = null
                }
            } else {
                //error
                println("HomeViewModel.getChats:${firebaseFirestoreException.message}")
                chatParticipantsListMutableLiveData.value = null
            }
        }
        return chatParticipantsListMutableLiveData
    }

    fun getUserData() {

        FirestoreUtil.firestoreInstance.collection("users").document(AuthUtil.getAuthId())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    val loggedUser = documentSnapshot?.toObject(User::class.java)
                    if (loggedUser != null){
                        loggedUserMutableLiveData.value = loggedUser
                    }
                } else {
                    println("HomeViewModel.getUserData:${firebaseFirestoreException.message}")
                }
            }
    }


}
