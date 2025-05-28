package com.example.learnui.ARCode

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.learnui.R
import com.google.firebase.storage.FirebaseStorage
import io.github.sceneview.Scene
import io.github.sceneview.animation.Transition.animateRotation
import io.github.sceneview.environment.Environment
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun ModelViewer(location: String, tts: String) {
    val context = LocalContext.current

    //Decoding the encoded location and tts
    val decodedLocation = "gs://internship-project-61853.appspot.com/models/atommodel2.glb"
    val decodedTts = URLDecoder.decode(tts, StandardCharsets.UTF_8.toString())

    // State management
    var modelFile by remember { mutableStateOf<File?>(null) }
    var ttsFile by remember { mutableStateOf<File?>(null) }

    var isModelReady by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(decodedLocation) {
        try {
            isLoading = true
            errorMessage = null

            // Download to cache first
            val tempFile = File(context.cacheDir, "temp_model.glb")

            // Extract the Firebase path correctly from decodedLocation
            val firebasePath = if (decodedLocation.startsWith("gs://")) {
                decodedLocation.substringAfter("gs://internship-project-61853.appspot.com/", "")
            } else {
                decodedLocation // Assume it's already a path
            }

            Log.d("FirebaseDownload", "Attempting to download from path: $firebasePath")
            Log.d("FirebaseDownload", "Original location: $decodedLocation")

            val storageRef = FirebaseStorage.getInstance().reference.child(firebasePath)
            storageRef.getFile(tempFile).await()

            Log.d("FirebaseDownload", "Model downloaded at: ${tempFile.absolutePath}")
            Log.d("FirebaseDownload", "File size: ${tempFile.length()} bytes")

            if (tempFile.exists() && tempFile.length() > 0) {
                // Copy to app's private internal storage
                val modelFile = File(context.filesDir, "downloaded_model.glb")
                tempFile.copyTo(modelFile, overwrite = true)
                tempFile.delete() // Clean up temp file

                Log.d("FirebaseDownload", "Model copied to: ${modelFile.absolutePath}")
                isModelReady = true
            } else {
                errorMessage = "Downloaded model file is empty or missing"
                Log.e("Firebase", errorMessage!!)
            }
        } catch (e: Exception) {
            errorMessage = "Failed to download model: ${e.message}"
            Log.e("Firebase", "Failed to download model", e)
        } finally {
            isLoading = false
        }
    }

    Log.d("ModelViewer", "Model file path: ${modelFile?.absolutePath}")
    Log.d("ModelViewer", "Is loading: $isLoading")
    Log.d("ModelViewer", "Error message: $errorMessage")

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading 3D Model...")
                }
            }
        }
        errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $errorMessage",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Retry logic - trigger recomposition
                        modelFile = null
                        isLoading = true
                        errorMessage = null
                    }) {
                        Text("Retry")
                    }
                }
            }
        }
        isModelReady -> {
            ModelLoaderPart(context)
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No model file available")
            }
        }
    }
}

@Composable
private fun ModelLoaderPart(context: Context) {
    var modelLoadError by remember { mutableStateOf<String?>(null) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(z = 0.0f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        val cameraTransition = rememberInfiniteTransition(label = "CameraTransition")
        val cameraRotation by cameraTransition.animateRotation(
            initialValue = Rotation(y = 0.0f),
            targetValue = Rotation(360.0f),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7.seconds.toInt(DurationUnit.MILLISECONDS))
            )
        )

        LaunchedEffect(Unit) {
            try {
                Log.d("ModelLoader", "Starting model loading process...")

                val modelFile = File(context.filesDir, "downloaded_model.glb")

                if (!modelFile.exists()) {
                    throw Exception("Model file not found: ${modelFile.absolutePath}")
                }

                Log.d("ModelLoader", "Model file exists: ${modelFile.absolutePath}, size: ${modelFile.length()}")

                // Read the model file as bytes and create a temporary file in cache
                // that SceneView can access
                val modelBytes = modelFile.readBytes()

                // Create a new file with .glb extension in the app's cache directory
                // Use a unique name to avoid conflicts
                val tempModelFile = File(context.cacheDir, "sceneview_${System.currentTimeMillis()}.glb")
                tempModelFile.writeBytes(modelBytes)

                Log.d("ModelLoader", "Created temp file: ${tempModelFile.absolutePath}")

                // Try to load using the ByteBuffer approach
                val byteBuffer = ByteBuffer.allocateDirect(modelBytes.size)
                byteBuffer.put(modelBytes)
                byteBuffer.rewind()

                Log.d("ModelLoader", "Created ByteBuffer of size: ${byteBuffer.capacity()}")

                // Use the ModelLoader with ByteBuffer - this is the correct approach for SceneView
                val modelInstance = withContext(Dispatchers.Main) {
                    modelLoader.createModelInstance(byteBuffer)
                }

                if (modelInstance == null) {
                    throw Exception("ModelLoader.createModelInstance returned null")
                }

                Log.d("ModelLoader", "Model instance created successfully")

                modelNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 0.25f
                )

                centerNode.addChildNode(modelNode!!)
                Log.d("ModelLoader", "Model successfully loaded and added to scene")

                // Clean up temp file
                tempModelFile.delete()

            } catch (e: Exception) {
                modelLoadError = "Error loading 3D model: ${e.message}"
                Log.e("ModelLoader", "Model loading failed", e)
            }
        }

        if (modelLoadError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modelLoadError!!,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Scene(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = rememberCameraManipulator(
                orbitHomePosition = cameraNode.worldPosition,
                targetPosition = centerNode.worldPosition
            ),
            childNodes = listOfNotNull(centerNode, modelNode),
            onFrame = {
                centerNode.rotation = cameraRotation
                cameraNode.lookAt(centerNode)
            },
            onGestureListener = rememberOnGestureListener(
                onDoubleTap = { _, node ->
                    node?.apply {
                        scale *= 2.0f
                    }
                }
            )
        )
    }
}