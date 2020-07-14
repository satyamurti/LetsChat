package com.mrspd.letschat.fragments.create_group

import android.util.Log.d
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.mrspd.letschat.models.GroupName
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.ErrorMessage
import com.mrspd.letschat.util.FirestoreUtil
import com.mrspd.letschat.util.LoadState


class CreateGroupViewModel : ViewModel() {

    val navigateToHomeMutableLiveData = MutableLiveData<Boolean?>()
    val loadingState = MutableLiveData<LoadState>()
    val loggedUserMutableLiveData = MutableLiveData<User>()
    val createdGroupFlag = MutableLiveData<Boolean>()
    private val groupCollectionReference = FirestoreUtil.firestoreInstance.collection("messages")
    private var userDocRef: DocumentReference? = AuthUtil.getAuthId().let {
        FirestoreUtil.firestoreInstance.collection("users").document(it)
    }

    init {
        getUserData()
    }

    fun createGroup(
        user: User,
        groupName: GroupName
    ) {
        val db = FirestoreUtil.firestoreInstance
        groupName.group_name?.let { name ->
            db.collection("messages").document(name).set(groupName).addOnSuccessListener {
                d("gghh", "created group succesfully")
                updateUserProfileForGroups(groupName.group_name.toString())
            }.addOnFailureListener {
                loadingState.value = LoadState.FAILURE
                ErrorMessage.errorMessage = it.message
            }
        }


        print("Yes created room")
    }

    fun updateUserProfileForGroups(groupName: String) {
        userDocRef?.update(
            "groups_in",
            FieldValue.arrayUnion(groupName, groupName)
        )
            ?.addOnSuccessListener {
                // bioLoadState.value = LoadState.SUCCESS
                createdGroupFlag.value = true
                d("gghh", "added group in user succesfully")

            }
            ?.addOnFailureListener {
                // bioLoadState.value = LoadState.FAILURE
                d("gghh", "added group in user failurly")

            }

    }


    fun getUserData() {

        FirestoreUtil.firestoreInstance.collection("users").document(AuthUtil.getAuthId())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    val loggedUser = documentSnapshot?.toObject(User::class.java)
                    if (loggedUser != null) {
                        loggedUserMutableLiveData.value = loggedUser
                    }
                } else {
                    println("HomeViewModel.getUserData:${firebaseFirestoreException.message}")
                }
            }
    }

    fun doneNavigating() {
        navigateToHomeMutableLiveData.value = null
    }

}