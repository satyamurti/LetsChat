package com.mrspd.letschat.fragments.different_user_profile

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.mrspd.letschat.fragments.incoming_requests.FRIENDS
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mrspd.letschat.R
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.AuthUtil

const val SENT_REQUEST_ARRAY = "sentRequests"
const val RECEIVED_REQUEST_ARRAY = "receivedRequests"

class DifferentUserProfileFragmentViewModel(val app: Application) : AndroidViewModel(app) {


    private val friendRequestStateMutableLiveData = MutableLiveData<FriendRequestState>()

    var loadedImage = MutableLiveData<RequestBuilder<Drawable>>()

    fun downloadProfilePicture(profilePictureUrl: String?) {
        println("DifferentUserProfileFragmentViewModel.downloadProfilePicture:$profilePictureUrl")
        if (profilePictureUrl == "null") return
        val load: RequestBuilder<Drawable> =
            Glide.with(app).load(profilePictureUrl).placeholder(R.drawable.anonymous_profile)
        loadedImage.value = load
    }

    fun updateSentRequestsForSender(uid: String?) {


        //add id in sentRequest array for logged in user
        val db = FirebaseFirestore.getInstance()
        if (uid != null) {
            AuthUtil.getAuthId().let {
                db.collection("users").document(it)
                    .update(SENT_REQUEST_ARRAY, FieldValue.arrayUnion(uid)).addOnSuccessListener {
                        //add loggedInUserId in receivedRequest array for other user
                        updateReceivedRequestsForReceiver(db, uid, AuthUtil.getAuthId())
                    }.addOnFailureListener {
                        throw it
                    }
            }
        }


    }

    private fun updateReceivedRequestsForReceiver(
        db: FirebaseFirestore,
        uid: String,
        loggedInUserId: String?
    ) {
        db.collection("users").document(uid)
            .update(RECEIVED_REQUEST_ARRAY, FieldValue.arrayUnion(loggedInUserId))
            .addOnSuccessListener {
            }.addOnFailureListener {
                throw it
            }
    }


    enum class FriendRequestState { SENT, NOT_SENT, ALREADY_FRIENDS }


    //get document if logged in user and check if other user id is in the sentRequest list
    fun checkIfFriends(recepiantId: String?): LiveData<FriendRequestState> {
        val db = FirebaseFirestore.getInstance()
        if (recepiantId != null) {
            AuthUtil.getAuthId().let {
                db.collection("users").document(it)
                    .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                        if (firebaseFirestoreException == null) {
                            val user =
                                documentSnapshot?.toObject(User::class.java)

                            //Check if friends already
                            val friendsList = user?.friends
                            if (!friendsList.isNullOrEmpty()) {
                                //user has friends
                                for (friendId in friendsList) {
                                    if (friendId == recepiantId) {
                                        friendRequestStateMutableLiveData.value =
                                            FriendRequestState.ALREADY_FRIENDS
                                        return@addSnapshotListener
                                    }
                                }
                            }

                            val sentRequests = user?.sentRequests
                            if (sentRequests != null) {
                                for (sentRequest in sentRequests) {
                                    if (sentRequest == recepiantId) {
                                        friendRequestStateMutableLiveData.value =
                                            FriendRequestState.SENT
                                        return@addSnapshotListener
                                    }
                                }
                                friendRequestStateMutableLiveData.value =
                                    FriendRequestState.NOT_SENT
                            }
                        } else {
                            println("DifferentUserProfileFragmentViewModel.checkIfFriends:${firebaseFirestoreException.message}")
                        }
                    }

            }


        }
        return friendRequestStateMutableLiveData
    }

    fun cancelFriendRequest(uid: String?) {

        //remove id from sentRequest array for logged in user
        val db = FirebaseFirestore.getInstance()
        if (uid != null) {
            AuthUtil.getAuthId().let {
                db.collection("users").document(it)
                    .update(SENT_REQUEST_ARRAY, FieldValue.arrayRemove(uid)).addOnSuccessListener {
                        //remove loggedInUserId from receivedRequest array for other user
                        db.collection("users").document(uid)
                            .update(
                                RECEIVED_REQUEST_ARRAY,
                                FieldValue.arrayRemove(AuthUtil.getAuthId())
                            )
                            .addOnSuccessListener {
                            }.addOnFailureListener {
                            }
                    }.addOnFailureListener {
                    }
            }
        }


    }

    fun removeFromFriends(uid: String?) {

        //remove id from sentRequest array for logged in user
        val db = FirebaseFirestore.getInstance()
        if (uid != null) {
            AuthUtil.getAuthId().let {
                db.collection("users").document(it)
                    .update(FRIENDS, FieldValue.arrayRemove(uid)).addOnSuccessListener {
                        //remove loggedInUserId from receivedRequest array for other user
                        db.collection("users").document(uid)
                            .update(
                                FRIENDS,
                                FieldValue.arrayRemove(AuthUtil.getAuthId())
                            )
                            .addOnSuccessListener {
                            }.addOnFailureListener {
                            }
                    }.addOnFailureListener {
                    }
            }
        }


    }

}
