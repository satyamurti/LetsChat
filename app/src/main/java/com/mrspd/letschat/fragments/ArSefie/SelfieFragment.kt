package com.mrspd.letschat.fragments.ArSefie

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log.d
import android.view.View
import androidx.fragment.app.Fragment
import com.camerakit.CameraKit
import com.camerakit.CameraKitView
import com.mrspd.letschat.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.arselfe_fragment.*


class SelfieFragment : Fragment(R.layout.arselfe_fragment) {
    private var Hack = 0
    private var mFlag = true
    private var cameraKitView: CameraKitView? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cameraKitView = view?.findViewById(R.id.camera)
        activity?.navView?.visibility = View.GONE
        ivCaptureSelfie.setOnClickListener {
            cameraKitView?.captureImage(CameraKitView.ImageCallback { cameraKitView, bytes ->
                ivSelfie.visibility = View.VISIBLE
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ivSelfie.setImageBitmap(bitmap)
                ivSendSelfie.visibility = View.VISIBLE
                camera.visibility = View.GONE
                ivCaptureSelfie.visibility = View.GONE
                ivFlash.visibility = View.GONE
                ivChangeCamera.visibility = View.GONE

//                val savedPhoto = File(Environment.getExternalStorageDirectory(), "photo.jpg")
//                try {
//                    val outputStream = FileOutputStream(savedPhoto.getPath())
//                    outputStream.write(bytes)
//                    outputStream.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
            })
        }
        ivSendSelfie.setOnClickListener {
            d("gghh", "sending image......")
        }
        ivChangeCamera.setOnClickListener {
            mFlag = when {
                mFlag -> {
                    d("gghh", " front")
                    cameraKitView?.facing = CameraKit.FACING_FRONT
                    false
                }
                else -> {
                    d("gghh", " back")

                    cameraKitView?.facing = CameraKit.FACING_BACK
                    true
                }
            }
        }
        ivFlash.setOnClickListener {
            when (Hack) {
                0 -> {
                    cameraKitView?.flash = CameraKit.FLASH_ON
                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_on)
                    Hack = 1
                }
                1 -> {
                    cameraKitView?.flash = CameraKit.FLASH_AUTO
                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
                    Hack = 2
                }
                2 -> {
                    cameraKitView?.flash = CameraKit.FLASH_OFF
                    ivFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
                    Hack = 0
                }
            }

        }

    }

    override fun onStart() {
        super.onStart()
        cameraKitView?.onStart()
    }

    override fun onPause() {
        cameraKitView!!.onPause()
        super.onPause()
    }

    override fun onStop() {
        cameraKitView!!.onStop()
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraKitView?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}