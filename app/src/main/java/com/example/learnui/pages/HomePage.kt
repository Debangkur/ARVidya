package com.example.learnui.pages

import android.annotation.SuppressLint
import android.provider.CalendarContract.Colors
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnui.Firebase.FirebaseRepository
import com.example.learnui.R
import kotlin.random.Random

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(navController: NavController) {
    var subjects by remember { mutableStateOf(listOf<String>()) }
    val textColor = if(isSystemInDarkTheme()) Color.White else Color.Black
    val cardColor = if(isSystemInDarkTheme()) Color(0xFF02263A) else Color(0xFFB1E4FB)

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchSubjects {
            subjects = it
            Log.d("Subject", "$subjects")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(120.dp),
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Subjects",
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
    ) {
        showSubjects(subjects, navController, textColor, cardColor)
    }
}

@Composable
private fun showSubjects(
    subjects: List<String>,
    navController: NavController,
    textColor: Color,
    cardColor: Color
) {

    LazyColumn(modifier = Modifier.padding(top = 180.dp)) {
        items(subjects) { subject ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(start = 25.dp, end = 25.dp, bottom = 20.dp)
                    .clickable {
                        navController.navigate("topics/$subject")
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(5.dp),
                colors = CardColors(
                    containerColor = cardColor,
                    contentColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified,
                    disabledContainerColor = Color.Unspecified
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(start = 40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(50.dp)
                            .clip(CircleShape)
                    ){
                        when(subject){
                            "physics" -> Icon(painterResource(R.drawable.physics), contentDescription = "physics", modifier = Modifier.fillMaxSize())
                            "astronomy" -> Icon(painterResource(R.drawable.astronomy), contentDescription = "astronomy", modifier = Modifier.fillMaxSize())
                            "biology" -> Icon(painterResource(R.drawable.biology), contentDescription = "astronomy", modifier = Modifier.fillMaxSize())
                            "chemistry" -> Icon(painterResource(R.drawable.chemistry), contentDescription = "chemistry", modifier = Modifier.fillMaxSize())
                        }
                    }

                    Spacer(modifier = Modifier.width(9.dp))

                    Text(
                        text = subject.replaceFirstChar { it.uppercase() },
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.labelMedium.copy(color = textColor)
                    )
                }
            }
        }
    }
}