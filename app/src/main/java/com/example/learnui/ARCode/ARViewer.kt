package com.example.learnui.ARCode

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.learnui.R
import com.google.android.filament.Engine
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@Composable
fun ARViewer(location: String, tts: String, name: String, navController: NavHostController) {
    val context = LocalContext.current
    val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    //Decoding the encoded location and tts
    val decodedLocation = URLDecoder.decode(location, StandardCharsets.UTF_8.toString())
    val decodedTts = URLDecoder.decode(tts, StandardCharsets.UTF_8.toString())
    val decodedName = URLDecoder.decode(name, StandardCharsets.UTF_8.toString())



        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // The destroy calls are automatically made when their disposable effect leaves
            // the composition or its key changes.
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val materialLoader = rememberMaterialLoader(engine)
            val cameraNode = rememberARCameraNode(engine)
            val childNodes = rememberNodes()
            val view = rememberView(engine)
            val collisionSystem = rememberCollisionSystem(view)

            // State for model loading
            var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
            var modelLoadError by remember { mutableStateOf<String?>(null) }
            var isModelLoading by remember { mutableStateOf(true) }

            var planeRenderer by remember { mutableStateOf(true) }

            var trackingFailureReason by remember {
                mutableStateOf<TrackingFailureReason?>(null)
            }
            var frame by remember { mutableStateOf<Frame?>(null) }


            // Cleanup effect when leaving the composable
            DisposableEffect(Unit) {
                onDispose {
                    try {
                        // Clear child nodes first
                        childNodes.clear()

                        // Clean up model instance
                        modelInstance?.let {
                            // If there's a cleanup method, call it
                            // it.cleanup() // Uncomment if available
                        }
                        modelInstance = null

                        // Reset frame
                        frame = null

                        Log.d("ARViewer", "AR resources cleaned up")
                    } catch (e: Exception) {
                        Log.e("ARViewer", "Error during cleanup", e)
                    }
                }
            }

            // Load model from cache on startup
            LaunchedEffect(Unit) {
                loadModelFromCache(
                    context = context,
                    modelLoader = modelLoader,
                    onSuccess = { instance ->
                        modelInstance = instance
                        isModelLoading = false
                        Log.d("ARModel", "Model loaded successfully from cache")
                    },
                    onError = { error ->
                        modelLoadError = error
                        isModelLoading = false
                        Log.e("ARModel", "Failed to load model: $error")
                    },
                    decodedName
                )
            }

            ARScene(
                modifier = Modifier.fillMaxSize(),
                childNodes = childNodes,
                engine = engine,
                view = view,
                modelLoader = modelLoader,
                collisionSystem = collisionSystem,
                sessionConfiguration = { session, config ->
                    config.depthMode =
                        when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            true -> Config.DepthMode.AUTOMATIC
                            else -> Config.DepthMode.DISABLED
                        }
                    config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                    config.lightEstimationMode =
                        Config.LightEstimationMode.ENVIRONMENTAL_HDR
                },
                cameraNode = cameraNode,
                planeRenderer = planeRenderer,
                onTrackingFailureChanged = {
                    trackingFailureReason = it
                },
                onSessionUpdated = { session, updatedFrame ->
                    frame = updatedFrame

                    if (childNodes.isEmpty() && modelInstance != null) {
                        updatedFrame.getUpdatedPlanes()
                            .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                            ?.let { it.createAnchorOrNull(it.centerPose) }?.let { anchor ->
                                childNodes.clear()
                                childNodes.add(
                                    createAnchorNode(
                                        engine = engine,
                                        modelLoader = modelLoader,
                                        materialLoader = materialLoader,
                                        anchor = anchor,
                                        modelInstance = modelInstance!!
                                    )
                                )
                            }
                    }
                },
                onGestureListener = rememberOnGestureListener(
                    onSingleTapConfirmed = { motionEvent, node ->
                        if (node == null && modelInstance != null) {
                            val hitResults = frame?.hitTest(motionEvent.x, motionEvent.y)
                            hitResults?.firstOrNull {
                                it.isValid(
                                    depthPoint = false,
                                    point = false
                                )
                            }?.createAnchorOrNull()
                                ?.let { anchor ->
                                    planeRenderer = false
                                    childNodes.clear()
                                    childNodes.add(
                                        createAnchorNode(
                                            engine = engine,
                                            modelLoader = modelLoader,
                                            materialLoader = materialLoader,
                                            anchor = anchor,
                                            modelInstance = modelInstance!!
                                        )
                                    )
                                }
                        }
                    })
            )

            // Status text overlay
            Text(
                modifier = Modifier
                    .systemBarsPadding()
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 32.dp, end = 32.dp),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                color = Color.White,
                text = when {
                    isModelLoading -> "Loading 3D model..."
                    modelLoadError != null -> "Error: $modelLoadError"
                    trackingFailureReason != null -> trackingFailureReason!!.getDescription(context)
                    childNodes.isEmpty() -> stringResource(R.string.point_your_phone_down)
                    else -> stringResource(R.string.tap_anywhere_to_add_model)
                }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth(0.4f).height(80.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                onClick = {
                    val encodedModel =
                        URLEncoder.encode(decodedLocation, StandardCharsets.UTF_8.toString())
                    val encodedTTS =
                        URLEncoder.encode(decodedTts, StandardCharsets.UTF_8.toString())
                    val encodedName =
                        URLEncoder.encode(decodedName, StandardCharsets.UTF_8.toString())
                    navController.popBackStack()
                    navController.navigate("model/$encodedModel/$encodedTTS/$encodedName")
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.cube_white),
                        contentDescription = "null",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "Back to Model",
                        color = textColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }


private suspend fun loadModelFromCache(
    context: Context,
    modelLoader: ModelLoader,
    onSuccess: (ModelInstance) -> Unit,
    onError: (String) -> Unit,
    name: String
) {
    try {
        withContext(Dispatchers.IO) {
            // Look for the cached model file
            val modelFile = File(context.cacheDir, "$name.glb")
            val permanentFile = File(context.filesDir,"$name.glb")

            // Determine which file to use (prefer permanent storage)
            val modelFileToUse = when {
                permanentFile.exists() && permanentFile.length() > 0 -> {
                    Log.d("ModelLoader", "Using permanent model file: ${permanentFile.absolutePath}")
                    permanentFile
                }
                modelFile.exists() && modelFile.length() > 0 -> {
                    Log.d("ModelLoader", "Using cache model file: ${modelFile.absolutePath}")
                    modelFile
                }
                else -> {
                    onError("Model file not found in either cache or permanent storage")
                    return@withContext
                }
            }


            Log.d("ARModel", "Found cached model: ${modelFile.absolutePath}, size: ${modelFile.length()}")

            // Read the model file as bytes
            val modelBytes = modelFileToUse.readBytes()

            if (modelBytes.isEmpty()) {
                throw Exception("Model file is empty: ${modelFileToUse.absolutePath}")
            }

            // Create ByteBuffer for SceneView
            val byteBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            byteBuffer.put(modelBytes)
            byteBuffer.rewind()

            Log.d("ARModel", "Created ByteBuffer of size: ${byteBuffer.capacity()}")

            // Create model instance on Main thread
            withContext(Dispatchers.Main) {
                val modelInstance = modelLoader.createModelInstance(byteBuffer)

                if (modelInstance != null) {
                    onSuccess(modelInstance)
                } else {
                    onError("Failed to create model instance from cached file")
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ARModel", "Error loading model from cache", e)
        onError("Error loading cached model: ${e.message}")
    }
}

fun createAnchorNode(
    engine: Engine,
    modelLoader: ModelLoader,
    materialLoader: MaterialLoader,
    anchor: Anchor,
    modelInstance: ModelInstance
): AnchorNode {
    val anchorNode = AnchorNode(engine = engine, anchor = anchor)
    val modelNode = ModelNode(
        modelInstance = modelInstance,
        // Scale to fit in a 0.5 meters cube
        scaleToUnits = 0.5f
    ).apply {
        // Model Node needs to be editable for independent rotation from the anchor rotation
        isEditable = true
        editableScaleRange = 0.2f..0.75f
    }
    val boundingBoxNode = CubeNode(
        engine,
        size = modelNode.extents,
        center = modelNode.center,
        materialInstance = materialLoader.createColorInstance(Color.White.copy(alpha = 0.5f))
    ).apply {
        isVisible = false
    }
    modelNode.addChildNode(boundingBoxNode)
    anchorNode.addChildNode(modelNode)

    listOf(modelNode, anchorNode).forEach {
        it.onEditingChanged = { editingTransforms ->
            boundingBoxNode.isVisible = editingTransforms.isNotEmpty()
        }
    }
    return anchorNode
}

