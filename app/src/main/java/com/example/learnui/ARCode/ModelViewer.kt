package com.example.learnui.ARCode


import android.content.Context
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.filament.gltfio.Animator
import com.google.firebase.storage.FirebaseStorage
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import io.github.sceneview.rememberOnGestureListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ModelViewer(location: String, tts: String, name: String, navController: NavHostController) {
    val context = LocalContext.current

    //Decoding the encoded location and tts
    val decodedLocation =  URLDecoder.decode(location, StandardCharsets.UTF_8.toString())
    val decodedTts = URLDecoder.decode(tts, StandardCharsets.UTF_8.toString())
    val decodedName = URLDecoder.decode(name, StandardCharsets.UTF_8.toString())

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
            val tempFile = File(context.cacheDir, "$decodedName.glb")
            if (tempFile.exists() && tempFile.length() > 0) {
                isModelReady = true
                isLoading = false
                return@LaunchedEffect
            }

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
            ModelLoaderPart(context,decodedName,decodedLocation,decodedTts,navController)
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No model file available")
            }
        }
    }
}

@Composable
private fun ModelLoaderPart(
    context: Context,
    name: String,
    decodedLocation: String,
    decodedTts: String,
    navController: NavHostController,
    ) {
    var modelLoadError by remember { mutableStateOf<String?>(null) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var animator by remember { mutableStateOf<Animator?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(bottom = 30.dp)) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(y = -0.5f, z = 2.0f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        val cameraTransition = rememberInfiniteTransition(label = "CameraTransition")

        // Smooth Y-axis rotation with easing
        val cameraRotationY by cameraTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 15000, // Even slower for better viewing
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "CameraRotationY"
        )

        // Gentle vertical bobbing motion
        val cameraVerticalOffset by cameraTransition.animateFloat(
            initialValue = -0.05f, // Reduced range for subtlety
            targetValue = 0.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 5000, // Different timing to avoid sync
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CameraVerticalOffset"
        )

        // Subtle distance variation
        val cameraDistance by cameraTransition.animateFloat(
            initialValue = 1.9f, // Tighter range
            targetValue = 2.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 8000, // Different timing
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CameraDistance"
        )

        // Apply the animations
        LaunchedEffect(cameraRotationY, cameraVerticalOffset, cameraDistance, modelNode) {
            cameraNode.apply {
                val radians = Math.toRadians(cameraRotationY.toDouble())

                // Calculate new position
                val newPosition = Position(
                    x = sin(radians).toFloat() * cameraDistance,
                    y = -0.5f + cameraVerticalOffset,
                    z = cos(radians).toFloat() * cameraDistance
                )

                position = newPosition

                // Always look at the center - this ensures smooth tracking
                lookAt(centerNode)
            }
        }

        LaunchedEffect(Unit) {
            try {
                Log.d("ModelLoader", "Starting model loading process...")

                val modelFile = File(context.cacheDir, "$name.glb")

                if (!modelFile.exists()) {
                    throw Exception("Model file not found: ${modelFile.absolutePath}")
                }

                Log.d("ModelLoader", "Model file exists: ${modelFile.absolutePath}, size: ${modelFile.length()}")

                // Read the model file as bytes and create a temporary file in cache
                // that SceneView can access
                val modelBytes = modelFile.readBytes()

                /*// Create a new file with .glb extension in the app's cache directory
                // Use a unique name to avoid conflicts
                val tempModelFile = File(context.cacheDir, "sceneview_${System.currentTimeMillis()}.glb")*/
                modelFile.writeBytes(modelBytes)

                Log.d("ModelLoader", "Created temp file: ${modelFile.absolutePath}")

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

                // Create animator for the model
                val modelAnimator = modelInstance.animator

                // Check if model has animations
                if (modelAnimator.animationCount > 0) {
                    Log.d("ModelLoader", "Model has ${modelAnimator.animationCount} animation(s)")

                    // Play first animation (or choose specific one)
                    val animationIndex = 0 // Choose which animation to play
                    Log.d("ModelLoader", "Playing animation index: $animationIndex")

                    // Apply the animation
                    modelAnimator.applyAnimation(animationIndex,0.0f)

                    // Get animation duration for looping
                    val animationDuration = modelAnimator.getAnimationDuration(animationIndex)
                    Log.d("ModelLoader", "Animation duration: $animationDuration seconds")

                    animator = modelAnimator
                } else {
                    Log.d("ModelLoader", "Model has no animations")
                }


                centerNode.addChildNode(modelNode!!)
                Log.d("ModelLoader", "Model successfully loaded and added to scene")



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
                orbitHomePosition = Position(y = -0.5f, z = 2.0f),
                targetPosition = centerNode.worldPosition
            ),
            childNodes = listOfNotNull(centerNode, modelNode),
            // Remove onFrame since we handle lookAt in LaunchedEffect
            onGestureListener = rememberOnGestureListener(
                onDoubleTap = { _, node ->
                    node?.apply {
                        scale *= 1.5f // Reduced scaling factor
                    }
                }
            )
        )

        Button(
            modifier = Modifier.height(40.dp).width(90.dp).align(Alignment.BottomCenter),
            onClick = {
                val encodedModel = URLEncoder.encode("$decodedLocation", StandardCharsets.UTF_8.toString())
                val encodedTTS = URLEncoder.encode("$decodedTts", StandardCharsets.UTF_8.toString())
                val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                navController.navigate("ar/$encodedModel/$encodedTTS/$encodedName"){
                    popUpTo("main"){
                        inclusive = true
                    }
                }

            },
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "View in your space")
        }
    }
}