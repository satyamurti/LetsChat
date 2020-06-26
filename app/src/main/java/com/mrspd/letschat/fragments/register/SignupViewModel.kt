package com.mrspd.letschat.fragments.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.firebase.auth.FirebaseAuth
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.ErrorMessage
import com.mrspd.letschat.util.FirestoreUtil
import com.mrspd.letschat.util.LoadState


class SignupViewModel : ViewModel() {

    val navigateToHomeMutableLiveData = MutableLiveData<Boolean?>()
    val loadingState = MutableLiveData<LoadState>()


    fun registerEmail(
        auth: FirebaseAuth,
        email: String,
        password: String,
        username: String
    ) {

        loadingState.value = LoadState.LOADING

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                storeUserInFirestore(User(it.user?.uid, username, email))
            }.addOnFailureListener {
                ErrorMessage.errorMessage = it.message
                loadingState.value = LoadState.FAILURE
            }

    }


    fun storeUserInFirestore(user: User) {
        val db = FirestoreUtil.firestoreInstance
        user.uid?.let { uid ->
            db.collection("users").document(uid).set(user).addOnSuccessListener {
                navigateToHomeMutableLiveData.value = true
            }.addOnFailureListener {
                loadingState.value = LoadState.FAILURE
                ErrorMessage.errorMessage = it.message
            }
        }

    }


    fun doneNavigating() {
        navigateToHomeMutableLiveData.value = null
    }

}