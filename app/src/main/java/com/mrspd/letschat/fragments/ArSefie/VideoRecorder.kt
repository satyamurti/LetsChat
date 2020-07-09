package com.mrspd.letschat.fragments.ArSefie

import android.app.Activity
import android.content.ContentValues
import android.content.res.Configuration
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.ar.sceneform.SceneView
import java.io.File
import java.io.FileDescriptor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VideoRecorder(
    private val activity: Activity
) {

    private val qualityLevels = arrayOf(
        CamcorderProfile.QUALITY_HIGH,
        CamcorderProfile.QUALITY_2160P,
        CamcorderProfile.QUALITY_1080P,
        CamcorderProfile.QUALITY_720P,
        CamcorderProfile.QUALITY_480P
    )

    lateinit var sceneView: SceneView

    private var mediaRecorder: MediaRecorder? = null
    private lateinit var videoSize: Size

    private var videoEncoder = MediaRecorder.VideoEncoder.DEFAULT
    private var bitRate = 10000000
    private var frameRate = 30
    private var encoderSurface: Surface? = null

    private var isRecording = false

    private fun setupMediaRecorder() {
        if(mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }
        mediaRecorder?.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                setOutputFile(buildFile()?.absolutePath)
            } else {
                setOutputFile(buildFileApi29())
            }
            setVideoEncodingBitRate(bitRate)
            setVideoFrameRate(frameRate)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(videoEncoder)
            prepare()
            start()
        }
    }

    private fun buildFile(): File? {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val path =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)?.absolutePath +
                "/TryOutFurniture/${date}_video.mp4"
        val file = File(path)
        val dir = file.parentFile
        if(!dir.exists()) {
            dir.mkdirs()
        }
        return file
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun buildFileApi29(): FileDescriptor? {
        val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "${date}_video.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/TryOutFurniture")
        }
        val uri = activity.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        return activity.contentResolver.openFileDescriptor(uri ?: return null, "w")?.fileDescriptor
    }

    fun toggleRecordingState(): Boolean {
        if(isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
        return isRecording
    }

    private fun startRecording() {
        try {
            setupMediaRecorder()
        } catch(e: IOException) {
            Toast.makeText(activity, "Error recording the video.", Toast.LENGTH_LONG).show()
        }

        encoderSurface = mediaRecorder?.surface
        sceneView.startMirroringToSurface(encoderSurface, 0, 0, videoSize.width, videoSize.height)
        isRecording = true
    }

    private fun stopRecording() {
        encoderSurface?.let {
            sceneView.stopMirroringToSurface(encoderSurface)
            encoderSurface = null
        }
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        isRecording = false
    }

    fun setVideoQuality(quality: Int, orientation: Int) {
        var profile: CamcorderProfile? = null
        if(CamcorderProfile.hasProfile(quality)) {
            profile = CamcorderProfile.get(quality)
        } else {
            for(level in qualityLevels) {
                if(CamcorderProfile.hasProfile(level)) {
                    profile = CamcorderProfile.get(level)
                    break
                }
            }
        }
        profile?.let {
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                videoSize = Size(profile.videoFrameWidth, profile.videoFrameHeight)
            } else {
                videoSize = Size(profile.videoFrameHeight, profile.videoFrameWidth)
            }
            videoEncoder = profile.videoCodec
            bitRate = profile.videoBitRate
            frameRate = profile.videoFrameRate
        }
    }
}

