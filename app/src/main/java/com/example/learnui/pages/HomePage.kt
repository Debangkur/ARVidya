package com.example.learnui.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnui.Firebase.FirebaseRepository

@Composable
fun HomePage(navController: NavController) {
    var subjects by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchSubjects {
            subjects = it
            Log.d("Subject", "$subjects")
        }
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(subjects) { subject ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("topics/$subject")
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Text(
                    text = subject.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp
                )
            }
        }
    }


}