package com.mrspd.letschat.fragments.groupchat

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.d
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.ChatFragmentRoomsBinding
import com.mrspd.letschat.models.*
import com.mrspd.letschat.util.AuthUtil
import com.mrspd.letschat.util.ClICKED_GROUP
import com.mrspd.letschat.util.LOGGED_USER
import com.mrspd.letschat.util.eventbus_events.PermissionEvent
import com.mrspd.letschat.util.eventbus_events.UpdateRecycleItemEvent
import com.stfalcon.imageviewer.StfalconImageViewer
import com.stfalcon.imageviewer.loader.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.attachment_layout.view.*
import kotlinx.android.synthetic.main.chat_fragment_rooms.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.*


const val SELECT_CHAT_IMAGE_REQUEST = 3
const val CHOOSE_FILE_REQUEST = 4


class RoomChatFragment : Fragment() {

    private var recordStart = 0L
    private var recordDuration = 0L

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private var recorder: MediaRecorder? = null
    var isRecording = false //whether is recoding now or not
    var isRecord = true //whether it is text message or record
    private lateinit var loggedUser: User
    private lateinit var clickedGroup: GroupName


    private var messageList = mutableListOf<Message>()
    lateinit var binding: ChatFragmentRoomsBinding
    private val adapter: ChatAdapter by lazy {
        ChatAdapter(context, object : MessageClickListener {
            override fun onMessageClick(position: Int, message: Message) {
                //if clicked item is image open in full screen with pinch to zoom
                if (message.type == 1.0) {

                    binding.fullSizeImageView.visibility = View.VISIBLE

                    StfalconImageViewer.Builder<MyImage>(
                        activity!!,
                        listOf(MyImage((message as ImageMessage).uri!!)),
                        ImageLoader<MyImage> { imageView, myImage ->
                            Glide.with(activity!!)
                                .load(myImage.url)
                                .apply(RequestOptions().error(R.drawable.ic_broken_image_black_24dp))
                                .into(imageView)
                        })
                        .withDismissListener { binding.fullSizeImageView.visibility = View.GONE }
                        .show()


                }
                //show dialog confirming user want to download file then proceed to download or cancel
                else if (message.type == 2.0) {
                    //file message we should download
                    val dialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }
                    dialogBuilder?.setMessage("Do you want to download clicked file?")
                        ?.setPositiveButton(
                            "yes"
                        ) { _, _ ->
                            downloadFile(message)
                        }?.setNegativeButton("cancel", null)?.show()

                } else if (message.type == 3.0) {
                    adapter.notifyDataSetChanged()
                }
            }

        })
    }

    private fun downloadFile(message: Message) {
        //check for storage permission then download if granted
        Dexter.withActivity(activity!!)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    //download file
                    val downloadManager =
                        activity!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val uri = Uri.parse((message as FileMessage).uri)
                    val request = DownloadManager.Request(uri)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        uri.lastPathSegment
                    )
                    downloadManager.enqueue(request)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                    //notify parent activity that permission denied to show toast for manual permission giving
                    showSnackBar()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    //notify parent activity that permission denied to show toast for manual permission giving
                    EventBus.getDefault().post(PermissionEvent())
                }
            }).check()
    }


    companion object {
        fun newInstance() = RoomChatFragment()
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var viewModeldFactory: ChatViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment_rooms, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

pinbar.visibility = View.GONE
        //setup bottom sheet
        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        //set record view
        handleRecord()
        getActivity()?.navView?.visibility = View.GONE


        //get logged user from shared preferences
        val mPrefs: SharedPreferences = activity!!.getPreferences(Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = mPrefs.getString(LOGGED_USER, null)
        loggedUser = gson.fromJson(json, User::class.java)

        //get receiver data from contacts fragment(NOTE:IF NAVIGATING FROM FCM-NOTIFICATION USER ONLY HAS id,username)
       clickedGroup = gson.fromJson(arguments?.getString(ClICKED_GROUP), GroupName::class.java)

        activity?.title = clickedGroup.name

        //user view model factory to pass ids on creation of view model
        viewModeldFactory = ChatViewModelFactory(loggedUser.uid, clickedGroup.name!!)
        viewModel =
            ViewModelProviders.of(this, viewModeldFactory).get(ChatViewModel::class.java)


        //Move layouts up when soft keyboard is shown
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)


        //send message on keyboard done click
        binding.messageEditText.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }


        binding.recycler.adapter = adapter

        //pass messages list for recycler to show
        viewModel.loadMessages().observe(viewLifecycleOwner, Observer { mMessagesList ->
            messageList = mMessagesList as MutableList<Message>
            ChatAdapter.messageList = messageList
            adapter.submitList(mMessagesList)
            //scroll to last items in recycler (recent messages)
            binding.recycler.scrollToPosition(mMessagesList.size - 1)

        })


        //handle click of bottomsheet items
        binding.bottomSheet.sendPictureButton.setOnClickListener {
            selectFromGallery()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheet.sendFileButton.setOnClickListener {
            openFileChooser()
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheet.hide.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        //show bottom sheet
        binding.attachmentImageView.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


        //observe when new record is uploaded
        viewModel.chatRecordDownloadUriMutableLiveData.observe(
            viewLifecycleOwner,
            Observer { recordUri ->
                println("observer called")
                viewModel.sendMessage(
                    RecordMessage(
                        AuthUtil.getAuthId(),
                        Timestamp(Date()),
                        3.0,
                        clickedGroup.name,
                        loggedUser.username,
                        recordDuration.toString(),
                        recordUri.toString(),
                        null,
                        null
                    )
                )
            })
    }


    private fun handleRecord() {


        //change fab icon depending on is text message empty or not
        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    //empty text message
                    binding.recordFab.setImageResource(R.drawable.mic)
                    isRecord = true
                } else {
                    binding.recordFab.setImageResource(R.drawable.sendicon)
                    isRecord = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        binding.recordFab.setOnClickListener {
            if (isRecord) {
                //record message
                if (isRecording) {
                    //chnage size and color or button so user know its finished recording
                    val regainer = AnimatorInflater.loadAnimator(
                        context,
                        R.animator.regain_size
                    ) as AnimatorSet
                    regainer.setTarget(binding.recordFab)
                    regainer.start()
                    binding.recordFab.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#b39ddb"))
                    //stop recording and upload record
                    stopRecording()
                    showPlaceholderRecord()
                    viewModel.uploadRecord("${activity!!.externalCacheDir?.absolutePath}/audiorecord.3gp")
                    Toast.makeText(context, "Finished recording", Toast.LENGTH_SHORT).show()
                    isRecording = !isRecording

                } else {

                    Dexter.withActivity(activity)
                        .withPermission(Manifest.permission.RECORD_AUDIO)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                                //chnage size and color or button so user know its recording
                                val increaser = AnimatorInflater.loadAnimator(
                                    context,
                                    R.animator.increase_size
                                ) as AnimatorSet
                                increaser.setTarget(binding.recordFab)
                                increaser.start()
                                binding.recordFab.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#EE4B4B"))
                                //start recording
                                startRecording()
                                Toast.makeText(context, "Recording", Toast.LENGTH_SHORT).show()
                                isRecording = !isRecording
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permission: com.karumi.dexter.listener.PermissionRequest?,
                                token: PermissionToken?
                            ) {
                                token?.continuePermissionRequest()
                                //notify parent activity that permission denied to show toast for manual permission giving
                                showSnackBar()
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                                //notify parent activity that permission denied to show toast for manual permission giving
                                showSnackBar()
                            }
                        }).check()

                }

            } else {
                //text message
                sendMessage()
            }
        }


    }


    private fun sendMessage() {
        if (binding.messageEditText.text.isEmpty()) {
            Toast.makeText(context, getString(R.string.empty_message), Toast.LENGTH_LONG).show()
            return
        }
        viewModel.sendMessage(
            TextMessage(
                loggedUser.uid,
                Timestamp(Date()),
                0.0,
                clickedGroup.name,
                loggedUser.username,
                binding.messageEditText.text.toString()
            )
        )

        binding.messageEditText.setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //select file result
        if (requestCode == CHOOSE_FILE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {

            val filePath = data.data

            showPlaceholderFile(filePath)

            //chat file was uploaded now store the uri with the message
            viewModel.uploadChatFileByUri(filePath).observe(this, Observer { chatFileMap ->
                viewModel.sendMessage(
                    FileMessage(
                        loggedUser.uid,
                        Timestamp(Date()),
                        2.0,
                        clickedGroup.name,
                        loggedUser.username,
                        chatFileMap["fileName"].toString(),
                        chatFileMap["downloadUri"].toString()
                    )
                )

            })

        }

        //select picture result
        if (requestCode == SELECT_CHAT_IMAGE_REQUEST && data != null && resultCode == AppCompatActivity.RESULT_OK) {

            //show fake item with image in recycler until image is uploaded
            showPlaceholderPhoto(data.data)

            //upload image to firebase storage
            viewModel.uploadChatImageByUri(data.data)
                .observe(this, Observer { uploadedChatImageUri ->
                    //chat image was uploaded now store the uri with the message
                    viewModel.sendMessage(
                        ImageMessage(
                            loggedUser.uid,
                            Timestamp(Date()),
                            1.0,
                            clickedGroup.name,
                            loggedUser.username,
                            uploadedChatImageUri.toString()
                        )
                    )
                })

        }


    }


    private fun openFileChooser() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "*/*"
        try {
            startActivityForResult(i, CHOOSE_FILE_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No suitable file manager was found on this device",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showPlaceholderPhoto(data: Uri?) {
        messageList.add(
            ImageMessage(
                AuthUtil.getAuthId(),
                null,
                1.0,
                clickedGroup.name,
                loggedUser.username,
                data.toString()
            )
        )
        adapter.submitList(messageList)
        adapter.notifyItemInserted(messageList.size - 1)
        binding.recycler.scrollToPosition(messageList.size - 1)
    }


    private fun showPlaceholderRecord() {
        //show fake item with progress bar while record uploads
        messageList.add(
            RecordMessage(
                AuthUtil.getAuthId(),
                null,
                8.0,
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
        adapter.submitList(messageList)
        adapter.notifyItemInserted(messageList.size - 1)
        binding.recycler.scrollToPosition(messageList.size - 1)
    }


    private fun showPlaceholderFile(data: Uri?) {
        messageList.add(
            FileMessage(
                AuthUtil.getAuthId(),
                null,
                2.0,
                clickedGroup.name,
                loggedUser.username,
                data.toString(),
                data?.path.toString()
            )
        )
        adapter.submitList(messageList)
        adapter.notifyItemInserted(messageList.size - 1)
        binding.recycler.scrollToPosition(messageList.size - 1)
    }

    private fun selectFromGallery() {
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            SELECT_CHAT_IMAGE_REQUEST
        )
    }

    private fun showSnackBar() {
        Snackbar.make(
            binding.coordinator,
            "Permission is needed for this feature to work",
            Snackbar.LENGTH_LONG
        ).setAction(
            "Grant", View.OnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity!!.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        ).show()

    }


    private fun startRecording() {

        //name of the file where record will be stored
        val fileName = "${activity!!.externalCacheDir?.absolutePath}/audiorecord.3gp"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()

            } catch (e: IOException) {
                println("ChatFragment.startRecording${e.message}")
            }

            start()
            recordStart = Date().time
        }
    }

    private fun stopRecording() {

        recorder?.apply {
            stop()
            release()
            recorder = null
        }

        recordDuration = Date().time - recordStart

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecycleItemEvent(event: UpdateRecycleItemEvent) {
        adapter.notifyItemChanged(event.adapterPosition)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)


        inflater.inflate(R.menu.chat_menu_room, menu)
        val menuItem = menu.findItem(R.id.action_incoming_requests)
        val actionView = menuItem?.actionView


        actionView?.setOnClickListener { onOptionsItemSelected(menuItem) }

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
//        R.id.action_logout -> {
//            logout()
//            true
//        }
        R.id.action_information -> {
            Toast.makeText(
                activity?.applicationContext,
                "Number of members  are ${clickedGroup.listOfmembers?.size.toString()} ",
                Toast.LENGTH_LONG
            ).show()
//            GlobalScope.launch {
//                showsizse()
//            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }

    }

    suspend fun showsizse() {
        var gg = -1
        gg = viewModel.getNumberOfGroupMembers()
        d("gghh", "$gg ewrewr")
        Toast.makeText(
            activity?.applicationContext,
            "Number of members  are ",
            Toast.LENGTH_LONG
        ).show()
    }

}

