package com.mrspd.letschat.fragments.incoming_requests

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.IncomingRequestsFragmentBinding
import com.mrspd.letschat.models.User
import com.mrspd.letschat.util.LOGGED_USER

class IncomingRequestsFragment : Fragment() {


    private lateinit var adapter: IncomingRequestsAdapter
    private lateinit var binding: IncomingRequestsFragmentBinding
    var sendersList: MutableList<User>? = null


    companion object {
        fun newInstance() =
            IncomingRequestsFragment()
    }

    private lateinit var viewModel: IncomingRequestsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Received friend requests"
        binding =
            DataBindingUtil.inflate(inflater, R.layout.incoming_requests_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(IncomingRequestsViewModel::class.java)


        //get user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(LOGGED_USER, null)
        val loggedUser: User = gson.fromJson(json, User::class.java)

        //get friend requests if receivedRequest isn't empty
        val receivedRequest = loggedUser.receivedRequests
        if (!receivedRequest.isNullOrEmpty()) {
            viewModel.downloadRequests(receivedRequest).observe(viewLifecycleOwner, Observer { requestersList ->
                //hide loading
                binding.loadingRequestsImageView.visibility = View.GONE

                if (requestersList == null) {
                    //error while getting received requests
                    binding.noIncomingRequestsLayout.visibility = View.VISIBLE
                    Toast.makeText(
                        context,
                        "Error while loading incoming friend requests",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    //got requests successfully
                    binding.noIncomingRequestsLayout.visibility = View.GONE
                    adapter.setDataSource(requestersList)
                    sendersList = requestersList
                    binding.receivedRequestsRecycler.adapter = adapter
                }
            })
        } else {
            //no received requests
            binding.noIncomingRequestsLayout.visibility = View.VISIBLE
            binding.loadingRequestsImageView.visibility = View.GONE

        }


        //handle click on item of friend request recycler
        adapter =
            IncomingRequestsAdapter(
                object :
                    IncomingRequestsAdapter.ButtonCallback {
                    override fun onConfirmClicked(requestSender: User, position: Int) {
                        viewModel.addToFriends(requestSender.uid!!, loggedUser.uid!!)

                        Toast.makeText(
                            context,
                            "${requestSender.username} added to your friends",
                            Toast.LENGTH_LONG
                        ).show()
                        deleteFromRecycler(position)
                    }

                    override fun onDeleteClicked(requestSender: User, position: Int) {
                        viewModel.deleteRequest(requestSender.uid!!, loggedUser.uid!!)
                        Toast.makeText(context, "Request deleted", Toast.LENGTH_LONG).show()
                        deleteFromRecycler(position)
                    }


                    //Delete accepted/declind request from recycler
                    private fun deleteFromRecycler(position: Int) {
                        sendersList?.removeAt(position)
                        adapter.setDataSource(sendersList)
                        adapter.notifyItemRemoved(position)
                        //if no requests left (after user accept or delete)show the empty layout
                        if (sendersList?.size == 0) {
                            binding.noIncomingRequestsLayout.visibility = View.VISIBLE
                        }
                    }

                })


    }


}
