package com.mrspd.letschat.fragments.group_info

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.GroupinfoFragmentBinding
import com.mrspd.letschat.fragments.profile.ProfileFragment
import com.mrspd.letschat.fragments.profile.REQUEST_IMAGE_CAPTURE
import com.mrspd.letschat.fragments.profile.SELECT_PROFILE_IMAGE_REQUEST
import com.mrspd.letschat.models.GroupName
import com.mrspd.letschat.models.User
import com.mrspd.letschat.ui.mainActivity.SharedViewModel
import com.mrspd.letschat.util.CLICKED_USER
import com.mrspd.letschat.util.ClICKED_GROUP
import com.mrspd.letschat.util.LoadState
import com.mrspd.letschat.util.eventbus_events.KeyboardEvent
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream

class GroupInfoFragment : Fragment() {


    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    lateinit var binding: GroupinfoFragmentBinding
    lateinit var adapter: MembersAdapter

    companion object {
        fun newInstance() =
            ProfileFragment()
    }

    private lateinit var viewModel: GroupInfoViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Group Information"
        binding = DataBindingUtil.inflate(inflater, R.layout.groupinfo_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.navView?.visibility = View.GONE

        viewModel = ViewModelProviders.of(this).get(GroupInfoViewModel::class.java)
        sharedViewModel = ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)

        //setup bottom sheet
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)


        //get user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(ClICKED_GROUP, null)
//        val group: GroupName = gson.fromJson(json, GroupName::class.java)
        val group = gson.fromJson(arguments?.getString(ClICKED_GROUP), GroupName::class.java)

        //show user name & email & bio
        binding.descriptionTextView.text = group.description ?: "No Description yet"
        binding.groupName.text = group.group_name
        //download profile photo
        setProfileImage(group.imageurl)


        //create adapter and handle recycle item click callback
        adapter = MembersAdapter(object :
            MembersAdapter.ItemClickCallback {
            override fun onItemClicked(clickedUser: User) {

                val clickedUserString = gson.toJson(clickedUser)

                var bundle = bundleOf(
                    CLICKED_USER to clickedUserString
                )

                findNavController().navigate(
                    R.id.action_groupInfoFragment_to_differentUserProfile,
                    bundle
                )
            }
        })


        //load friends of logged in user and show in recycler
        sharedViewModel.loadMembers(group).observe(viewLifecycleOwner, Observer { memberlist ->
            //hide loading
            binding.loadingFriendsImageView.visibility = View.GONE
            if (memberlist != null) {
                binding.friendsLayout.visibility = View.VISIBLE
                binding.noFriendsLayout.visibility = View.GONE
                showFriendsInRecycler(memberlist)
            } else {
                binding.friendsLayout.visibility = View.GONE
                binding.noFriendsLayout.visibility = View.VISIBLE
                binding.addFriendsButton.setOnClickListener {
                    this@GroupInfoFragment.findNavController()
                        .navigate(R.id.action_profileFragment_to_findUserFragment)
                }
            }

        })



        binding.bottomSheet.cameraButton.setOnClickListener {
            openCamera()
        }
        binding.bottomSheet.galleryButton.setOnClickListener {
            selectFromGallery()
        }

        binding.bottomSheet.hide.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        //show selection bottom sheet when those buttons clicked
        binding.profileImage.setOnClickListener { selectProfilePicture() }
        binding.cameraImageView.setOnClickListener { selectProfilePicture() }


        //edit bio handle click
        binding.editTextview.setOnClickListener {
            if (binding.editTextview.text.equals(getString(R.string.edit))) {
                //show edit text to allow user to edit bio and change text view text to submit
                binding.editTextview.text = getString(R.string.submit)
                binding.editTextview.setTextColor(Color.GREEN)
                binding.descriptionTextView.visibility = View.GONE
                binding.newBioEditText.visibility = View.VISIBLE


            } else if (binding.editTextview.text.equals(getString(R.string.submit))) {
                //hide edit text and upload changes to user document
                binding.editTextview.text = getString(R.string.edit)
                binding.editTextview.setTextColor(Color.parseColor("#b39ddb"))
                binding.descriptionTextView.visibility = View.VISIBLE
                binding.descriptionTextView.text = binding.newBioEditText.text
                binding.newBioEditText.visibility = View.GONE
                EventBus.getDefault().post(KeyboardEvent())
                //upload bio to user document
                viewModel.updateBio(binding.newBioEditText.text.toString())

                //hide keyboard
                EventBus.getDefault().post(KeyboardEvent())
            }
        }


    }

    private fun setProfileImage(groupimage: String?) {
        d("gghh"," image loading...")
        Glide.with(this).load(groupimage)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.anonymous_profile)
                    .circleCrop()
            )
            .into(binding.profileImage)
    }

    private fun showFriendsInRecycler(it: List<User>) {
        adapter.setDataSource(it)
        binding.friendsRecycler.adapter = adapter
        binding.friendsCountTextView.text = it.size.toString()
    }

    private fun setProfileImageLoadUi(it: LoadState?) {
        when (it) {

            LoadState.SUCCESS -> {
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = 1f
            }
            LoadState.FAILURE -> {
                binding.uploadProgressBar.visibility = View.GONE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = 1f
            }
            LoadState.LOADING -> {
                binding.uploadProgressBar.visibility = View.VISIBLE
                binding.uploadText.visibility = View.GONE
                binding.profileImage.alpha = .5f

            }
        }
    }


    private fun selectProfilePicture() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED


        //result of selecting image from gallery
        if (requestCode == SELECT_PROFILE_IMAGE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {

            //set selected image in profile image view and upload it

            //upload image
            viewModel.uploadProfileImageByUri(data.data)


        }


        //result of taking camera image
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap


            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()


            //upload image
            viewModel.uploadImageAsBytearray(byteArray)


        }

        //show loading layout while uploading
        viewModel.uploadImageLoadStateMutableLiveData.observe(this, Observer { imageUploadState ->
            setProfileImageLoadUi(imageUploadState)
        })


        //set new image in profile image view
        viewModel.newImageUriMutableLiveData.observe(this, Observer {
            setProfileImage(it.toString())
        })
    }


    private fun selectFromGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            SELECT_PROFILE_IMAGE_REQUEST
        )
    }


    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                startActivityForResult(takePictureIntent,
                    REQUEST_IMAGE_CAPTURE
                )
            }
        }
    }


}

