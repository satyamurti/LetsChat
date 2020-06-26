package com.mrspd.letschat.fragments.home_group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.mrspd.letschat.models.GroupName
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.FirestoreUtil


class HomeViewModelRoom : ViewModel() {

    var calledBefore = false
    init {
        getUserData()
    }
 //  val    grouplist = ArrayList<String>()
    private val groupParticipantList: MutableList<GroupName> by lazy { mutableListOf<GroupName>() }
    private val grouplist =
        MutableLiveData<MutableList<GroupName>>()
    val loggedUserMutableLiveData = MutableLiveData<User>()


    fun getRooms(loggedUser: User): MutableLiveData<MutableList<GroupName>> {

        //this method is called each time user document changes but i want to attach listener only once so check with calledBefore
        if (calledBefore) {
            return grouplist

        }

        calledBefore = true

        val loggedUserId = loggedUser.uid.toString()

//        FirestoreUtil.firestoreInstance.collection("users").document(loggedUserId).get()
//            .addOnSuccessListener {group ->
//               grouplist.addAll(group.get("groups") as Collection<String>)
//                getGroups(grouplist)
//            }
        val query: Query = FirestoreUtil.firestoreInstance.collection("messages")
                .whereArrayContains("chat_members_in_group", loggedUserId)

        query.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException == null) {
                groupParticipantList.clear()

                if (!querySnapshot?.documents.isNullOrEmpty()) {
                    println("Aree  hua bhai sorry")

                    val groupName = GroupName()

                    querySnapshot?.documents?.forEach {document ->
                        groupName.name = document.get("group_name") as String?
                        groupParticipantList.add(groupName)
                        grouplist.value = groupParticipantList
                    }
                } else {
                    println("Aree nahi hua bhai sorry $loggedUserId")
                    //user has no chats
                    grouplist.value = null
                }
            } else {
                //error
                println("Aree error hua bhai sorry $loggedUserId")

                println("HomeViewModel.getChats:${firebaseFirestoreException.message}")
                grouplist.value = null
            }
        }
        return grouplist
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
