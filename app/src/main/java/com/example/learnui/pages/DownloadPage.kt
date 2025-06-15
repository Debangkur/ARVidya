package com.example.learnui.pages

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnui.DataClass.LocalModels
import com.example.learnui.LocalModelsDao
import com.example.learnui.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DownloadPage(dao: LocalModelsDao, navController: NavController) {
    val downloadedModels by dao.getModelList().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val textColor = if(isSystemInDarkTheme()) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Downloaded Models",
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (downloadedModels.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(R.drawable.download),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No downloaded models",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the download icon on any model to save it here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding() + 50.dp,
                        start = 25.dp,
                        end = 25.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(downloadedModels) { model ->
                        DownloadedModelCard(
                            navController,
                            model = model,
                            onDelete = {
                                coroutineScope.launch {
                                    try {
                                        // Delete the model file first
                                        val modelFile = File(context.filesDir, "${model.name}.glb")
                                        var modelFileDeleted = true
                                        if (modelFile.exists()) {
                                            modelFileDeleted = modelFile.delete()
                                            if (!modelFileDeleted) {
                                                Log.w("DownloadPage", "Failed to delete model file: ${model.location}")
                                            }
                                        }

                                        // Delete the image file
                                        val imageFile = File(context.filesDir, "${model.name}.jpg")
                                        var imageFileDeleted = true
                                        if (imageFile.exists()) {
                                            imageFileDeleted = imageFile.delete()
                                            if (!imageFileDeleted) {
                                                Log.w("DownloadPage", "Failed to delete image file: ${imageFile.absolutePath}")
                                            }
                                        }

                                        dao.deleteModel(model)

                                        // Show appropriate toast message
                                        if (modelFileDeleted && imageFileDeleted) {
                                            Toast.makeText(
                                                context,
                                                "${model.name} and associated files deleted successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "${model.name} deleted from database, but some files may remain",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DownloadPage", "Failed to delete model", e)
                                        Toast.makeText(
                                            context,
                                            "Failed to delete ${model.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            textColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedModelCard(
    navController: NavController,
    model: LocalModels,
    onDelete: () -> Unit,
    textColor: Color,
) {
    val context = LocalContext.current
    val cardColor = if(isSystemInDarkTheme()) Color(0xFF02263A) else Color(0xFFB1E4FB)
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable {
                val encodedModel = URLEncoder.encode(model.location, StandardCharsets.UTF_8.toString())
                val encodedTTS = URLEncoder.encode(model.tts, StandardCharsets.UTF_8.toString())
                val encodedName = URLEncoder.encode(model.name, StandardCharsets.UTF_8.toString())
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
                val imageFile = File(context.filesDir, "${model.name}.jpg")

                if (imageFile.exists()) {
                    val bitmap = remember(model.name) {
                        BitmapFactory.decodeFile(imageFile.absolutePath)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Model image for ${model.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.7f),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback icon if image can't be decoded
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.7f)
                                .background(Color.Gray)
                        )
                    }
                } else {
                    // Fallback icon if image doesn't exist
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .background(Color.Gray)
                    )
                }
                // Name section
                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    modifier = Modifier,
                    text = model.name,
                    fontSize = 15.sp,
                    style = MaterialTheme.typography.labelMedium.copy(color = textColor)
                )
            }

                // Delete button overlay
                IconButton(
                    onClick = {showDialog = true},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.dustbin),
                        contentDescription = "Delete model",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

            if(showDialog){
                BasicAlertDialog(
                    onDismissRequest = { showDialog = false }
                ){
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Are you sure?")
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("No")
                                }
                                TextButton(onClick = {
                                    showDialog = false
                                    onDelete()
                                }) {
                                    Text("Yes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
