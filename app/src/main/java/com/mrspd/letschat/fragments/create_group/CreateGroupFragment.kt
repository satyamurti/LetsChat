package com.mrspd.letschat.fragments.create_group

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.CreategroupFragmentBinding
import com.mrspd.letschat.fragments.groupchat.gson
import com.mrspd.letschat.models.GroupName
import com.mrspd.letschat.util.ErrorMessage
import com.mrspd.letschat.util.LoadState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.issue_layout.view.*
import java.util.regex.Pattern

class CreateGroupFragment : Fragment() {
    private lateinit var binding: CreategroupFragmentBinding
    private lateinit var pattern: Pattern

    companion object {
        fun newInstance() = CreateGroupFragment()
    }

    private lateinit var viewModel: CreateGroupViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.creategroup_fragment, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CreateGroupViewModel::class.java)


        //regex pattern to check email format
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)\$"
        pattern = Pattern.compile(emailRegex)

        activity?.navView?.visibility = View.GONE

        //handle register click
        binding.createGroupButton.setOnClickListener {
            createGroup()
        }


        //hide issue layout on x icon click
        binding.issueLayout.cancelImage.setOnClickListener {
            binding.issueLayout.visibility = View.GONE
        }

        //show proper loading/error ui
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            when (it) {
                LoadState.LOADING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.SUCCESS -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.GONE
                }
                LoadState.FAILURE -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.issueLayout.visibility = View.VISIBLE
                    binding.issueLayout.textViewIssue.text = ErrorMessage.errorMessage
                }

            }
        })


        //sign up on keyboard done click when focus is on passwordEditText
        binding.newgroupDescriptionEditText.setOnEditorActionListener { _, actionId, _ ->
            createGroup()
            true
        }

    }

    private fun createGroup() {

        binding.groupName.isErrorEnabled = false
        binding.description.isErrorEnabled = false


        if (binding.groupName.editText!!.text.length < 4) {
            binding.groupName.error = "Group name should be at least 4 characters"
            return
        }


        //check if email is empty or wrong format
        if (binding.description.editText!!.text.isEmpty()) {
            binding.description.error = "Please Enter Group Description."
            return

        }


        //email and pass are matching requirements now we can register to firebase auth


        //get user data
        viewModel.loggedUserMutableLiveData.observe(viewLifecycleOwner, Observer { loggedUser ->
            //save logged user data in shared pref to use in other fragments
            val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
            val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
            val json = gson.toJson(loggedUser)
            prefsEditor.putString("loggedUser", json)
            prefsEditor.apply()
            var groupName = GroupName()
            groupName.description = binding.description.editText!!.text.toString()
            groupName.group_name = binding.groupName.editText!!.text.toString()
            groupName.chat_members_in_group = listOf(loggedUser.uid.toString())


            if (groupName != null) {
                viewModel.createGroup(
                    loggedUser,
                    groupName
                )
            } else
                d("gghh", "failed")
        })


    }


//    private fun signUp() {
//        EventBus.getDefault().post(KeyboardEvent())
//
//        binding.groupName.isErrorEnabled = false
//        binding.description.isErrorEnabled = false
//
//
//        if (binding.groupName.editText!!.text.length < 4) {
//            binding.groupName.error = "Group name should be at least 4 characters"
//            return
//        }
//
//
//        //check if email is empty or wrong format
//        if (!binding.description.editText!!.text.isEmpty()) {
//            binding.description.error = "Please Enter Group Description."
//            return
//
//        }
//
//
//        //email and pass are matching requirements now we can register to firebase auth
//
//        viewModel.createGroup(
//            AuthUtil.firebaseAuthInstance,
//            binding.groupName.editText!!.text.toString(),
//            binding.description.editText!!.text.toString()
//        )
//
//
//        viewModel.navigateToHomeMutableLiveData.observe(
//            viewLifecycleOwner,
//            Observer { navigateToHome ->
//                if (navigateToHome != null && navigateToHome) {
//                    this@CreateGroupFragment.findNavController()
//                        .navigate(R.id.action_signupFragment_to_homeFragment)
//                    Toast.makeText(context, "Sign up successful", Toast.LENGTH_LONG).show()
//                    viewModel.doneNavigating()
//                }
//            })
//
//    }

}



