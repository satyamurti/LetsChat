package com.mrspd.letschat.fragments.ArSefie

import android.app.Activity
import android.media.CamcorderProfile
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.mrspd.letschat.R
import com.mrspd.letschat.databinding.ArselfeFragmentBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.arselfe_fragment.*
import java.util.concurrent.CompletableFuture


class ARSelfieFragment : Fragment() {
    private lateinit var binding: ArselfeFragmentBinding

    private lateinit var arFragment: ArFragment
    private var faceRenderable: ModelRenderable? = null
    private var isRecording = false

    private var faceTexture: Texture? = null
    private lateinit var photoSaver: PhotoSaver
    private lateinit var videoRecorder: VideoRecorder
    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.arselfe_fragment, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getActivity()?.navView?.visibility = View.GONE

        arFragment = childFragmentManager.findFragmentById(R.id.fragment12) as ArFragment
        loadModel()
        setupFab()
        photoSaver = PhotoSaver(context!! as Activity)
        videoRecorder = VideoRecorder(context!! as Activity).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }

        arFragment.arSceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        arFragment.arSceneView.scene.addOnUpdateListener {
            if (faceRenderable != null && faceTexture != null) {
                addTrackedFaces()
                removeUntrackedFaces()
            }
        }

    }

    private fun setupFab() {
        fab1.setOnClickListener {
            if (!isRecording) {
                photoSaver.takePhoto(arFragment.arSceneView)
            }
        }
        fab1.setOnLongClickListener {
            isRecording = videoRecorder.toggleRecordingState()
            true
        }
        fab1.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP && isRecording) {
                isRecording = videoRecorder.toggleRecordingState()
                Toast.makeText(context, "Saved video to gallery!", Toast.LENGTH_LONG).show()
                true
            } else false
        }
    }


    private fun addTrackedFaces() {
        val session = arFragment.arSceneView.session ?: return
        val faceList = session.getAllTrackables(AugmentedFace::class.java)
        for (face in faceList) {
            if (!faceNodeMap.containsKey(face)) {
                AugmentedFaceNode(face).apply {
                    setParent(arFragment.arSceneView.scene)
                    faceRegionsRenderable = faceRenderable
                    faceMeshTexture = faceTexture
                    faceNodeMap[face] = this
                }
            }
        }
    }

    private fun removeUntrackedFaces() {
        val entries = faceNodeMap.entries
        for (entry in entries) {
            val face = entry.key
            if (face.trackingState == TrackingState.STOPPED) {
                val faceNode = entry.value
                faceNode.setParent(null)
                entries.remove(entry)
            }
        }
    }

    private fun loadModel() {
        val modelRenderable = ModelRenderable.builder()
            .setSource(context, R.raw.fox_face)
            .build()
        val texture = Texture.builder()
            .setSource(context, R.drawable.clown_face_mesh_texture)
            .build()
        CompletableFuture.allOf(modelRenderable, texture)
            .thenAccept {
                faceRenderable = modelRenderable.get().apply {
                    isShadowCaster = false
                    isShadowReceiver = false
                }
                faceTexture = texture.get()
            }.exceptionally {
                Log.e("MainActivity", "ERROR: $it")
                Toast.makeText(context, "Error loading model: $it", Toast.LENGTH_LONG).show()
                null
            }
    }
}