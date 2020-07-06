package com.mrspd.letschat.fragments.home_one_to_one_chat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.HomeFragmentBinding
import com.mrspd.letschat.models.ChatParticipant
import com.mrspd.letschat.models.User
import com.mrspd.letschat.services.MyFirebaseMessagingService
import com.mrspd.letschat.ui.mainActivity.SharedViewModel
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.CLICKED_USER
import com.mrspd.letschat.util.FirestoreUtil
import kotlinx.android.synthetic.main.activity_main.*

class HomeFragment : Fragment() {
    private var receivedRequestsCount: Int? = null
    lateinit var binding: HomeFragmentBinding
    val gson = Gson()
    private lateinit var countBadgeTextView: TextView
    private val adapter: ChatPreviewAdapter by lazy {
        ChatPreviewAdapter(ClickListener { chatParticipant ->
            //navigate to chat with selected user on chat outer item click
            activity?.navView?.visibility = View.GONE
            val clickedUser = gson.toJson(chatParticipant.particpant)
            findNavController().navigate(
                R.id.action_homeFragment_to_chatFragment, bundleOf(
                    CLICKED_USER to clickedUser
                )
            )
        })
    }


    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Chats"

        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
        activity?.navView?.visibility = View.VISIBLE
        //get logged user token and add it to user document (for FCM)
        MyFirebaseMessagingService.getInstanceId()


        //theses intent extras are coming from FCM notification click so we need to move to specific chat if not null
        val senderId = activity!!.intent.getStringExtra("senderId")
        val senderName = activity!!.intent.getStringExtra("senderName")
        if (senderId != null && senderName != null) {
            val receiverUser =
                User(uid = senderId, username = senderName)
            findNavController().navigate(
                R.id.action_homeFragment_to_chatFragment, bundleOf(
                    CLICKED_USER to gson.toJson(receiverUser)
                )
            )
            val nullSting: CharSequence? = null
            activity!!.intent.putExtra("senderId", nullSting)
            activity!!.intent.putExtra("senderName", nullSting)
        }


        //get user data
        viewModel.loggedUserMutableLiveData.observe(viewLifecycleOwner, Observer { loggedUser ->
            //save logged user data in shared pref to use in other fragments
            val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
            val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
            val json = gson.toJson(loggedUser)
            prefsEditor.putString("loggedUser", json)
            prefsEditor.apply()


            //show notification badge if there is incoming requests
            receivedRequestsCount = loggedUser.receivedRequests?.size ?: 0
            setupBadge(receivedRequestsCount)


            //get user chat history
            viewModel.getChats(loggedUser!!)
                ?.observe(viewLifecycleOwner, Observer { chatParticipantsList ->

                    //Hide loading image
                    binding.loadingChatImageView.visibility = View.GONE
                    if (chatParticipantsList.isNullOrEmpty()) {
                        //show no chat layout
                        binding.noChatLayout.visibility = View.VISIBLE
                    } else {

                        //sort messages by date so newwst show on top
                        val sortedChatParticipantsList: List<ChatParticipant> =
                            chatParticipantsList.sortedWith(compareBy { it.lastMessageDate?.get("seconds") })
                                .reversed()

                        binding.noChatLayout.visibility = View.GONE
                        binding.recycler.adapter = adapter
                        adapter.submitList(sortedChatParticipantsList)
                        adapter.chatList = sortedChatParticipantsList
                    }

                })


        })


        //handle startChatFab click
        binding.startChatFab.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_contactsFragment)
        }


    }


    private fun logout() {
        removeUserToken()
        FirebaseAuth.getInstance().signOut()
//        LoginManager.getInstance().logOut()
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    private fun removeUserToken() {
        val loggedUserID = AuthUtil.firebaseAuthInstance.currentUser?.uid
        if (loggedUserID != null) {
            FirestoreUtil.firestoreInstance.collection("users").document(loggedUserID)
                .update("token", null)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


        inflater.inflate(R.menu.main_menu, menu)
        val menuItem = menu.findItem(R.id.action_incoming_requests)
        val actionView = menuItem?.actionView
        countBadgeTextView = actionView?.findViewById<View>(R.id.count_badge) as TextView
        //if fragment is coming from back stack setupBadge will be called before onCreateOptionsMenu so we have to call setupbadge again
        setupBadge(receivedRequestsCount)



        actionView.setOnClickListener { onOptionsItemSelected(menuItem) }

        //do filtering when i type in search or click search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: kotlin.String?): Boolean {
                adapter.filter.filter(queryString)
                return false
            }

            override fun onQueryTextChange(queryString: kotlin.String?): Boolean {
                adapter.filter.filter(queryString)
                if (queryString != null) {
                    adapter.onChange(queryString)
                }

                return false
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

//        R.id.action_add_friend -> {
//            findNavController().navigate(R.id.action_homeFragment_to_findUserFragment)
//            true
//        }
//        R.id.action_edit_profile -> {
//            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
//            true
//        }
        R.id.action_logout -> {
            logout()
            true
        }
        R.id.action_incoming_requests -> {
            findNavController().navigate(R.id.action_homeFragment_to_incomingRequestsFragment)


            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }


    private fun setupBadge(count: Int?) {
        if (::countBadgeTextView.isInitialized) {
            if (null == count || count == 0) {
                countBadgeTextView.visibility = View.GONE
            } else {
                countBadgeTextView.visibility = View.VISIBLE
                countBadgeTextView.text =
                    count.let { Math.min(it, 99) }.toString()
            }
        }
    }


}