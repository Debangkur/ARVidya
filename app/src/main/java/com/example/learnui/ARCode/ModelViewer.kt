package com.example.learnui.ARCode


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.learnui.R
import com.google.android.filament.gltfio.Animator
import com.google.firebase.storage.FirebaseStorage
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Locale
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
            val permanentFile = File(context.filesDir,"$decodedName.glb")
            // If model exists in permanent storage, we're good
            if (permanentFile.exists() && permanentFile.length() > 0) {
                Log.d("ModelViewer", "Model found in permanent storage: ${permanentFile.absolutePath}")
                isModelReady = true
                isLoading = false
                return@LaunchedEffect
            }

            // If model exists in cache, we're also good
            if (tempFile.exists() && tempFile.length() > 0) {
                Log.d("ModelViewer", "Model found in cache: ${tempFile.absolutePath}")
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

enum class ttsState{
    PLAYING,
    NOT_PLAYING
}

@Composable
private fun ModelLoaderPart(
    context: Context,
    name: String,
    decodedLocation: String,
    decodedTts: String,
    navController: NavHostController,
    ) {
    val modelLoadError by remember { mutableStateOf<String?>(null) }
    val modelNode by remember { mutableStateOf<ModelNode?>(null) }
    val animator by remember { mutableStateOf<Animator?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            TopPart(modelNode, context, name, animator, modelLoadError)
            BottomPart(decodedTts, decodedLocation, navController, name, context)
        }
    }
}


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomPart(
    decodedTts: String,
    decodedLocation: String,
    navController: NavHostController,
    name: String,
    context: Context
) {

    var speakerState by remember { mutableStateOf(ttsState.NOT_PLAYING) }

    // TTS initialization with proper state management
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsInitialized by remember { mutableStateOf(false) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res: Int = textToSpeech?.setLanguage(Locale.UK) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if(res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(context,"Lang not supported",Toast.LENGTH_LONG).show()
                } else {
                    isTtsInitialized = true
                }
            }else{
                Toast.makeText(context,"Failed to initialize", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Set up TTS completion listener when TTS is initialized
    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized && textToSpeech != null) {
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // TTS started - no need to change state as it's already set
                }

                override fun onDone(utteranceId: String?) {
                    // TTS completed - reset state on main thread
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        speakerState = ttsState.NOT_PLAYING
                    }
                }

                override fun onError(utteranceId: String?) {
                    // TTS error - reset state on main thread
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        speakerState = ttsState.NOT_PLAYING
                    }
                }
            })
        }
    }

    // Cleanup TTS when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    BackHandler(
        onBack = {
            textToSpeech?.stop()
            navController.popBackStack()
        }
    )

    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(modifier = Modifier.height(250.dp)) {
            Text(
                text = name,
                modifier = Modifier.padding(start = 15.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(15.dp),
                text = decodedTts,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 15.sp
            )
        }
        Row(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 50.dp)) {
            Button(
                modifier = Modifier
                    .height(60.dp)
                    .width(170.dp),
                onClick = {
                    // Check if TTS is initialized before using it
                    if (isTtsInitialized && textToSpeech != null) {
                        when(speakerState) {
                            ttsState.PLAYING -> {
                                textToSpeech?.stop()
                                speakerState = ttsState.NOT_PLAYING
                            }
                            ttsState.NOT_PLAYING -> {
                                // Use the older API that works more reliably
                                val params = HashMap<String, String>()
                                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "tts_utterance_id"
                                textToSpeech?.speak(decodedTts, TextToSpeech.QUEUE_FLUSH, params)
                                speakerState = ttsState.PLAYING
                            }
                        }
                    } else {
                        Toast.makeText(context, "TTS not ready yet", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = buttonColor(speakerState)
            ) {
                Row {

                    when(speakerState){
                        ttsState.PLAYING -> {
                            Icon(
                                modifier = Modifier
                                    .width(19.dp)
                                    .height(19.dp),
                                painter = painterResource(R.drawable.stop),
                                contentDescription = "stop",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Stop",
                                style = MaterialTheme.typography.labelMedium.copy(Color.White)
                            )
                        }
                        ttsState.NOT_PLAYING -> {
                            Icon(
                                modifier = Modifier
                                    .width(19.dp)
                                    .height(19.dp),
                                painter = painterResource(R.drawable.speaker_white),
                                contentDescription = "speaker",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Listen",
                                style = MaterialTheme.typography.labelMedium.copy(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                modifier = Modifier
                    .height(60.dp)
                    .width(170.dp),
                onClick = {
                    val encodedModel =
                        URLEncoder.encode(decodedLocation, StandardCharsets.UTF_8.toString())
                    val encodedTTS =
                        URLEncoder.encode(decodedTts, StandardCharsets.UTF_8.toString())
                    val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                    navController.popBackStack()
                    navController.navigate("ar/$encodedModel/$encodedTTS/$encodedName")

                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.purple)
                )
            ) {
                Row {
                    Icon(
                        modifier = Modifier
                            .width(19.dp)
                            .height(19.dp),
                        painter = painterResource(R.drawable.cube_white),
                        contentDescription = "Ar icon",
                        tint = Color.White,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "View in AR",
                        style = MaterialTheme.typography.labelMedium.copy(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun buttonColor(speakerState: ttsState): ButtonColors {
    return when(speakerState){
        ttsState.PLAYING -> {
            ButtonDefaults.buttonColors(containerColor = Color(0xFFF50A1F))
        }
        ttsState.NOT_PLAYING -> {
            ButtonDefaults.buttonColors(containerColor = colorResource(R.color.dark_blue))
        }
    }
}

@Composable
private fun TopPart(
    modelNode: ModelNode?,
    context: Context,
    name: String,
    animator: Animator?,
    modelLoadError: String?
) {
    var modelNode1 = modelNode
    var animator1 = animator
    var modelLoadError1 = modelLoadError
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(bottom = 30.dp)
    ) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(y = 2.0f, z = 0.8f)
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
            initialValue = 0.4f, // Tighter range
            targetValue = 0.7f,
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
        LaunchedEffect(cameraRotationY, cameraVerticalOffset, cameraDistance, modelNode1) {
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

                val cacheModelFile = File(context.cacheDir, "$name.glb")
                val permanentModelFile = File(context.filesDir, "$name.glb")

                // Determine which file to use (prefer permanent storage)
                val modelFileToUse = when {
                    permanentModelFile.exists() && permanentModelFile.length() > 0 -> {
                        Log.d(
                            "ModelLoader",
                            "Using permanent model file: ${permanentModelFile.absolutePath}"
                        )
                        permanentModelFile
                    }

                    cacheModelFile.exists() && cacheModelFile.length() > 0 -> {
                        Log.d(
                            "ModelLoader",
                            "Using cache model file: ${cacheModelFile.absolutePath}"
                        )
                        cacheModelFile
                    }

                    else -> {
                        throw Exception("Model file not found in either cache or permanent storage")
                    }
                }

                Log.d(
                    "ModelLoader",
                    "Model file exists: ${modelFileToUse.absolutePath}, size: ${modelFileToUse.length()}"
                )

                // Read the model file bytes
                val modelBytes = modelFileToUse.readBytes()

                if (modelBytes.isEmpty()) {
                    throw Exception("Model file is empty: ${modelFileToUse.absolutePath}")
                }


                /*// Create a new file with .glb extension in the app's cache directory
                // Use a unique name to avoid conflicts
                val tempModelFile = File(context.cacheDir, "sceneview_${System.currentTimeMillis()}.glb")*/

                Log.d("ModelLoader", "Read ${modelBytes.size} bytes from model file")

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

                modelNode1 = ModelNode(
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
                    modelAnimator.applyAnimation(animationIndex, 0.0f)

                    // Get animation duration for looping
                    val animationDuration = modelAnimator.getAnimationDuration(animationIndex)
                    Log.d("ModelLoader", "Animation duration: $animationDuration seconds")

                    animator1 = modelAnimator
                } else {
                    Log.d("ModelLoader", "Model has no animations")
                }


                centerNode.addChildNode(modelNode1!!)
                Log.d("ModelLoader", "Model successfully loaded and added to scene")


            } catch (e: Exception) {
                modelLoadError1 = "Error loading 3D model: ${e.message}"
                Log.e("ModelLoader", "Model loading failed", e)
            }
        }

        if (modelLoadError1 != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modelLoadError1!!,
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
            /*cameraManipulator = rememberCameraManipulator(
                orbitHomePosition = Position(y = 2.0f, z = -3f),
                targetPosition = centerNode.worldPosition
            ),*/
            childNodes = listOfNotNull(centerNode, modelNode1),
            // Remove onFrame since we handle lookAt in LaunchedEffect
            /*onGestureListener = rememberOnGestureListener(
                onDoubleTap = { _, _ ->
                }
            )*/
        )
    }
}