package com.mrspd.letschat.fragments.different_user_profile

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mrspd.letschat.fragments.profile.FriendsAdapter
import com.google.gson.Gson
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.DifferentUserProfileFragmentBinding
import com.mrspd.letschat.models.User
import com.mrspd.letschat.ui.mainActivity.SharedViewModel
import com.mrspd.letschat.util.CLICKED_USER

class DifferentUserProfileFragment : Fragment() {
    private lateinit var binding: DifferentUserProfileFragmentBinding
    private val adapter by lazy {
        FriendsAdapter(object :
            FriendsAdapter.ItemClickCallback {
            override fun onItemClicked(user: User) {
                d("gg", "ok!!You clicked")
            }


        })
    }

    companion object {
        fun newInstance() =
            DifferentUserProfileFragment()
    }

    private lateinit var viewModel: DifferentUserProfileFragmentViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.different_user_profile_fragment,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProviders.of(this).get(DifferentUserProfileFragmentViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)

        //get data of clicked user from find user fragment
        val gson = Gson()
        val user = gson.fromJson(arguments?.getString(CLICKED_USER), User::class.java)


        activity?.title = user.username?.split("\\s".toRegex())?.get(0) + "'s profile"

        //check if alreadyFriends
        viewModel.checkIfFriends(user.uid).observe(this, Observer { friendRequestState ->
            when (friendRequestState) {//change button color and icon to show that a request is sent or not
                DifferentUserProfileFragmentViewModel.FriendRequestState.SENT -> {
                    showButtonAsSentRequest()
                }
                DifferentUserProfileFragmentViewModel.FriendRequestState.NOT_SENT -> {
                    showButtonAsRequestNotSent()
                }
                DifferentUserProfileFragmentViewModel.FriendRequestState.ALREADY_FRIENDS -> {
                    showButtonAsAlreadyFriends()
                }
            }
        })

        //set data to views and download image
        binding.bioTextView.text = user.bio ?: "No bio yet"
        binding.name.text = user.username
        viewModel.downloadProfilePicture(user.profile_picture_url)


        //show downloaded image in profile imageview
        viewModel.loadedImage.observe(viewLifecycleOwner, Observer {
            it.into(binding.profileImage)
        })


        binding.sendFriendRequestButton.setOnClickListener {
            //add id to sentRequests document in user
            if (binding.sendFriendRequestButton.text == getString(R.string.friend_request_not_sent)) {
                viewModel.updateSentRequestsForSender(user.uid)
                showButtonAsSentRequest()
            } else if (binding.sendFriendRequestButton.text == getString(R.string.cancel_request)) {
                viewModel.cancelFriendRequest(user.uid)
                showButtonAsRequestNotSent()
            } else if (binding.sendFriendRequestButton.text == getString(R.string.delete_from_friends)) {
                viewModel.removeFromFriends(user.uid)
                showButtonAsRequestNotSent()
            }
        }




        //load friends of that user
        sharedViewModel.loadFriends(user).observe(viewLifecycleOwner, Observer { friendsList ->
            if (friendsList.isNullOrEmpty()) {
                binding.friendsTextView.text = getString(R.string.no_friends)
            } else {
                binding.friendsTextView.text = getString(R.string.friends)
                binding.friendsCountTextView.text = friendsList.size.toString()
                showFriendsInRecycler(friendsList)
            }
        })

    }

    private fun showFriendsInRecycler(friendsList: List<User>?) {
        adapter.setDataSource(friendsList)
        binding.friendsRecycler.adapter = adapter

    }

    //change button to show that users are friends
    private fun showButtonAsAlreadyFriends() {
        binding.sendFriendRequestButton.text =
            getString(R.string.delete_from_friends)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_remove_circle_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.red) }
    }


    //change sent button to show that no request is sent
    private fun showButtonAsRequestNotSent() {
        binding.sendFriendRequestButton.text =
            getString(R.string.friend_request_not_sent)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_person_add_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.grey) }
    }


    //change sent button to show that  request is sent
    private fun showButtonAsSentRequest() {
        binding.sendFriendRequestButton.text = getString(R.string.cancel_request)
        binding.sendFriendRequestButton.setIconResource(R.drawable.ic_done_black_24dp)
        binding.sendFriendRequestButton.backgroundTintList =
            context?.let { it1 -> ContextCompat.getColorStateList(it1, R.color.green) }
    }


}
