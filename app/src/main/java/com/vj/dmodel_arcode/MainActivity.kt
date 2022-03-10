package com.vj.dmodel_arcode

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*


const val MIN_OPENGL_VERSION = 3.0

class MainActivity : AppCompatActivity() {

    private var anchorNode: AnchorNode? = null
    private lateinit var arFragment: CustomArFragment
    private var objectRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isDeviceSupported()) {
            return
        }
        setContentView(R.layout.activity_main)
        arFragment = ar_fragment as CustomArFragment
        initialize()

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            addToScene(anchor)
        }
    }

    private fun initialize() {

        ModelRenderable.builder()
            .setSource(this, Uri.parse("scene.sfb"))
            .build()
            .thenAccept { renderable ->
//                customMaterial = renderable.material.makeCopy()
                objectRenderable = renderable
            }


//        try {
//            ModelRenderable.builder()
//                .setSource(this,
//                    RenderableSource.builder().setSource(
//                        this,
//                        Uri.parse("scene.sfb"),
//                        RenderableSource.SourceType.GLB)/*RenderableSource.SourceType.GLTF2)*/
//                        .setScale(0.75f)
//                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                        .build())
//                .setRegistryId(arModel)
//                .build()
//                .thenAccept { renderable: ModelRenderable ->
//                    objectRenderable = renderable
//                    Toast.makeText(this, getString(R.string.model_ready), Toast.LENGTH_SHORT).show()
//
//                }
//                .exceptionally { throwable: Throwable? ->
//                    Log.i("Model", "cant load")
//                    null
//                }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun addToScene(anchor: Anchor) {
        val size = 0.3f
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment?.transformationSystem)

        node.renderable = objectRenderable

        node.setParent(anchorNode)
        arFragment?.arSceneView?.scene?.addChild(anchorNode)
    }

    private fun isDeviceSupported(): Boolean {
        if (ArCoreApk.getInstance().checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "ARFrames requires ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString =  (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion

        openGlVersionString?.let { s ->
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
        }
        return true

    }
}