package com.mrspd.letschat.fragments.ArSefie

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mrspd.letschat.BuildConfig
import com.mrspd.letschat.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.arselfe_fragment.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SelfieFragment : Fragment(R.layout.arselfe_fragment) {
    lateinit var currentPhotoPath: String
    private var Hack = 0
    private var mFlag = true
//    private var cameraKitView: CameraKitView? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dispatchTakePictureIntent()
    }

    val REQUEST_TAKE_PHOTO = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context?.packageManager!!)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        Objects.requireNonNull(context)!!,
                        BuildConfig.APPLICATION_ID + ".fileprovider", it);
//                    val photoURI: Uri = FileProvider.getUriForFile(
//                        context!!,
//                        "com.example.android.fileprovider",
//                        it
//                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    setPic(photoURI)
                }
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            val imageBitmap = data?.extras?.get("data") as Bitmap
//            ivSelfie.setImageBitmap(imageBitmap)
//        }
//    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
          //  sendBroadcast(mediaScanIntent)
        }
    }

    private fun setPic(photoURI: Uri) {
//        // Get the dimensions of the View
//        val targetW: Int = ivSelfie.width
//        val targetH: Int = ivSelfie.height
//
//        val bmOptions = BitmapFactory.Options().apply {
//            // Get the dimensions of the bitmap
//            inJustDecodeBounds = true
//
//            val photoW: Int = outWidth
//            val photoH: Int = outHeight
//
//            // Determine how much to scale down the image
//            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)
//
//            // Decode the image file into a Bitmap sized to fill the View
//            inJustDecodeBounds = false
//            inSampleSize = scaleFactor
//            inPurgeable = true
//        }
        ivSelfie.setImageURI(photoURI)
//        BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
//            ivSelfie.setImageBitmap(bitmap)
//        }
    }


//        cameraKitView = view?.findViewById(R.id.camera)
//        activity?.navView?.visibility = View.GONE
//        ivCaptureSelfie.setOnClickListener {
//            cameraKitView?.captureImage(CameraKitView.ImageCallback { cameraKitView, bytes ->
//                ivSelfie.visibility = View.VISIBLE
//                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                ivSelfie.setImageBitmap(bitmap)
//                ivSendSelfie.visibility = View.VISIBLE
//                camera.visibility = View.GONE
//                ivCaptureSelfie.visibility = View.GONE
//                ivFlash.visibility = View.GONE
//                ivChangeCamera.visibility = View.GONE
//
//
//            })
//        }
//        ivSendSelfie.setOnClickListener {
//            d("gghh", "sending image......")
//        }
//        ivChangeCamera.setOnClickListener {
//            mFlag = when {
//                mFlag -> {
//                    d("gghh", " front")
//                    cameraKitView?.facing = CameraKit.FACING_FRONT
//                    false
//                }
//                else -> {
//                    d("gghh", " back")
//
//                    cameraKitView?.facing = CameraKit.FACING_BACK
//                    true
//                }
//            }
//        }
//        ivFlash.setOnClickListener {
//            when (Hack) {
//                0 -> {
//                    cameraKitView?.flash = CameraKit.FLASH_ON
//                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_on)
//                    Hack = 1
//                }
//                1 -> {
//                    cameraKitView?.flash = CameraKit.FLASH_AUTO
//                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
//                    Hack = 2
//                }
//                2 -> {
//                    cameraKitView?.flash = CameraKit.FLASH_OFF
//                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
//                    Hack = 0
//                }
//            }
//
//        }



//    override fun onStart() {
//        super.onStart()
//        //cameraKitView?.onStart()
//    }
//
//    override fun onPause() {
//        cameraKitView!!.onPause()
//        super.onPause()
//    }
//
//    override fun onStop() {
//        cameraKitView!!.onStop()
//        super.onStop()
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        cameraKitView?.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    activity?.navView?.visibility = View.VISIBLE
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }
}