package com.example.learnui.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnui.DataClass.LocalModels
import com.example.learnui.DataClass.Topic
import com.example.learnui.Firebase.FirebaseRepository
import com.example.learnui.LocalModelsDao
import com.example.learnui.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class DownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicPage(subject: String, navController: NavController, dao: LocalModelsDao) {
    var topics by remember { mutableStateOf(listOf<Topic>()) }
    val textColor = if(isSystemInDarkTheme()) Color.White else Color.Black
    val cardColor = if(isSystemInDarkTheme()) Color(0xFF02263A) else Color(0xFFB1E4FB)


    LaunchedEffect(subject) {
        FirebaseRepository.fetchTopics(subject) {
            topics = it
            Log.d("TopicModel", "$topics")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier.height(120.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Topics",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = textColor,
                                fontSize = 28.sp
                            ),
                            modifier = Modifier.align(Alignment.BottomStart),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 80.dp,
                start = 25.dp,
                end = 25.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(topics){ topic ->
                TopicCard(topic = topic, navController = navController, dao, textColor, cardColor)
            }
        }
        /*LazyColumn() {
            items(topics) { topic ->
                TopicCard(topic = topic, navController = navController, dao)
            }
        }*/
    }
}

@Composable
private fun TopicCard(
    topic: Topic,
    navController: NavController,
    dao: LocalModelsDao,
    textColor: Color,
    cardColor: Color
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var downloadState by remember { mutableStateOf(DownloadState.NOT_DOWNLOADED) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val shortenedImageLocation = topic.image
        .substringAfter("gs://internship-project-61853.appspot.com/")
    val shortenedModelLocation = topic.location
        .substringAfter("gs://internship-project-61853.appspot.com/")

    // Check if model is already downloaded
    LaunchedEffect(topic.name) {
        dao.getModelList().collect { models ->
            downloadState = if (models.any { it.name == topic.name }) {
                DownloadState.DOWNLOADED
            } else {
                DownloadState.NOT_DOWNLOADED
            }
        }
    }

    LaunchedEffect(shortenedImageLocation) {
        FirebaseStorage.getInstance()
            .reference.child(shortenedImageLocation)
            .downloadUrl
            .addOnSuccessListener { uri ->
                imageUrl = uri.toString()
            }
            .addOnFailureListener {
                Log.e("TopicCard", "Failed to load image", it)
                imageUrl = null
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable {
                val encodedModel = URLEncoder.encode(topic.location, StandardCharsets.UTF_8.toString())
                val encodedTTS = URLEncoder.encode(topic.tts, StandardCharsets.UTF_8.toString())
                val encodedName = URLEncoder.encode(topic.name, StandardCharsets.UTF_8.toString())
                navController.navigate("model/$encodedModel/$encodedTTS/$encodedName")
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardColors(
            containerColor = cardColor,
            contentColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified,
            disabledContainerColor = Color.Unspecified
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = topic.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.73f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder or loading fallback
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.73f)
                            .background(Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier,
                    text = topic.name,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.labelMedium.copy(color = textColor)
                )
            }
            IconButton(
                onClick = {
                    if (downloadState == DownloadState.NOT_DOWNLOADED) {
                        downloadState = DownloadState.DOWNLOADING
                        coroutineScope.launch {
                            try {
                                val modelFileName = File(context.filesDir, "${topic.name}.glb")
                                val imageFileName = File(context.filesDir, "${topic.name}.jpg")

                                // Firebase Storage downloading files to internal storage
                                val locationRef = FirebaseStorage.getInstance()
                                    .reference.child(shortenedModelLocation)
                                locationRef.getFile(modelFileName).await()
                                val imageRef = FirebaseStorage.getInstance()
                                    .reference.child(shortenedImageLocation)
                                imageRef.getFile(imageFileName).await()

                                Log.d("TopicPage", "$locationRef, $imageRef")
                                val localModel = LocalModels(
                                    name = topic.name,
                                    location = topic.location,
                                    tts = topic.tts
                                )
                                dao.upsertModel(localModel)

                                downloadState = DownloadState.DOWNLOADED
                                Toast.makeText(
                                    context,
                                    "${topic.name} downloaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } catch (e: Exception) {
                                Log.e("TopicCard", "Failed to save model", e)
                                downloadState = DownloadState.NOT_DOWNLOADED
                                Toast.makeText(
                                    context,
                                    "Failed to download ${topic.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                enabled = downloadState == DownloadState.NOT_DOWNLOADED
            ) {
                when (downloadState) {
                    DownloadState.NOT_DOWNLOADED -> {
                        Icon(
                            painter = painterResource(R.drawable.download) ,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DownloadState.DOWNLOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.5.dp
                        )
                    }
                    DownloadState.DOWNLOADED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Downloaded",
                            tint = Color.Green,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}