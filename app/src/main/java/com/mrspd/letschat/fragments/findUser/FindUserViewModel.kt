package com.mrspd.letschat.fragments.findUser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrspd.letschat.fragments.incoming_requests.FRIENDS
import com.google.firebase.firestore.EventListener
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.FirestoreUtil

class FindUserViewModel : ViewModel() {


    private val userDocumentsMutableLiveData = MutableLiveData<MutableList<User?>>()


    fun loadUsers(): LiveData<MutableList<User?>> {


        val docRef = FirestoreUtil.firestoreInstance.collection("users")
        docRef.get()
            .addOnSuccessListener { querySnapshot ->
                //add any user that isn't logged in user to result
                val result = mutableListOf<User?>()
                for (document in querySnapshot.documents) {
                    if (!document.get("uid").toString().equals(AuthUtil.getAuthId())) {
                        val user = document.toObject(User::class.java)
                        result.add(user)
                    }

                }


                // remove friends of logged in user from result list
                docRef.whereArrayContains(FRIENDS, AuthUtil.getAuthId())
                    .addSnapshotListener(
                        EventListener { querySnapshot, firebaseFirestoreException ->
                            if (firebaseFirestoreException == null) {
                                val documents = querySnapshot?.documents
                                if (documents != null) {
                                    for (document in documents) {
                                        val user = document.toObject(User::class.java)
                                        result.remove(user)

                                    }

                                    userDocumentsMutableLiveData.value = result


                                }
                            } else {
                                userDocumentsMutableLiveData.value = null
                            }
                        })





            }
            .addOnFailureListener { exception ->
                userDocumentsMutableLiveData.value = null
            }

        return userDocumentsMutableLiveData
    }


}
