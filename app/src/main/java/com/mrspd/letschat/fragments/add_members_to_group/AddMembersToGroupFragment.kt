package com.mrspd.letschat.fragments.add_members_to_group

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.ListOfFreindsToAddInGroupFragmentBinding
import com.mrspd.letschat.models.User
import com.mrspd.letschat.ui.mainActivity.SharedViewModel
import com.mrspd.letschat.util.CLICKED_USER
import com.mrspd.letschat.util.LOGGED_USER
import java.util.*

class AddMembersToGroupFragment : Fragment() {
    lateinit var binding: ListOfFreindsToAddInGroupFragmentBinding
    lateinit var adapterr: FriendsAdapter
    var selectedItems: ArrayList<String>? = null
    var nonselectedItems: ArrayList<String>? = null
    private lateinit var sharedViewModel: SharedViewModel
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //get user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(LOGGED_USER, null)
        val loggedUser: User = gson.fromJson(json, User::class.java)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
        //create adapter and handle recycle item click callback
        adapterr = FriendsAdapter(object :
            FriendsAdapter.ItemClickCallback {
            override fun onItemClicked(clickedUser: User) {

                val clickedUserString = gson.toJson(clickedUser)

                var bundle = bundleOf(
                    CLICKED_USER to clickedUserString
                )

                findNavController().navigate(
                    R.id.action_profileFragment_to_differentUserProfile,
                    bundle
                )
            }
        })

        //load friends of logged in user and show in recycler
        sharedViewModel.loadFriends(loggedUser).observe(viewLifecycleOwner, Observer { friendsList ->
            //hide loading
            binding.loadingImage .visibility = View.GONE
            if (friendsList != null) {
//                binding.friendsLayout.visibility = View.VISIBLE
//                binding.noFriendsLayout.visibility = View.GONE
                showFriendsInRecycler(friendsList)
            } else {
//                binding.friendsLayout.visibility = View.GONE
//                binding.noFriendsLayout.visibility = View.VISIBLE
//                binding.addFriendsButton.setOnClickListener {
//                    this@ProfileFragment.findNavController()
//                        .navigate(R.id.action_profileFragment_to_findUserFragment)
//                }
            }

        })
    }

    private fun showFriendsInRecycler(it: List<User>) {
        adapterr.setDataSource(it)
        binding.recyclerWithCheckboxes.adapter = adapterr
    }
    fun OnStart(userlist: ArrayList<String>) {
        nonselectedItems = userlist
        val chl =
            view?.findViewById<View>(R.id.recycler_with_checkboxes) as ListView

        chl.apply {
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            adapter = ArrayAdapter(
                context,
                R.layout.checkable_list_layout,
                R.id.txt_title,
                userlist
            )
        }
        chl.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val selectedItem = (view as TextView).text.toString()
                if (selectedItems?.contains(selectedItem)!!) selectedItems?.remove(selectedItem) else selectedItems?.add(
                    selectedItem
                )
            }
    }
}