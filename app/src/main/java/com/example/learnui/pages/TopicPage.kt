package com.example.learnui.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnui.DataClass.Topic
import com.example.learnui.Firebase.FirebaseRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

@Composable
fun TopicPage(subject: String, navController: NavController) {
    var topics by remember { mutableStateOf(listOf<Topic>()) }

    LaunchedEffect(subject) {
        FirebaseRepository.fetchTopics(subject) {
            topics = it
        }
    }

    LazyColumn (modifier = Modifier.padding(16.dp, top = 70.dp)) {
        items(topics) { topic ->
            TopicCard(topic, navController)
        }

    }
}

@Composable
private fun TopicCard(topic: Topic, navController: NavController) {
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(topic.image) {
        FirebaseStorage.getInstance()
            .reference.child(topic.image)
            .downloadUrl
            .addOnSuccessListener { uri ->
                imageUrl = uri.toString()
            }
            .addOnFailureListener {
                imageUrl = null // handle error if needed
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("model/{model}/{tts}")
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = topic.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = topic.name, fontSize = 20.sp)
        }
    }
}