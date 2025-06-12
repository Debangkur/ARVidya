package com.example.learnui.pages

import android.app.Activity
import android.content.Intent
import android.text.Layout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnui.MainActivity
import com.example.learnui.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(modifier: Modifier = Modifier) {
    val textColor = if(isSystemInDarkTheme()) Color.White else Color.Black
    val boxColor = if(isSystemInDarkTheme()) Color(0xFF02263A) else Color(0xFFB1E4FB)
    var showDialog by remember { mutableStateOf(false) }
    var showChangePassDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val intent = Intent(context,MainActivity::class.java)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 20.dp)
                    ) {
                        Text(
                            text = "Settings",
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
    ){ innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding()+ 30.dp,
                    start = 15.dp,
                    end = 15.dp
                )
        ) {
            // Log Out Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { showDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                        fontSize = 16.sp
                    )
                }
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "Navigate to log out",
                    tint = textColor.copy(alpha = 0.7f)
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = textColor.copy(alpha = 0.1f)
            )

            // Change Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        showChangePassDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                        fontSize = 16.sp
                    )
                }
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "Navigate to change password",
                    tint = textColor.copy(alpha = 0.7f)
                )
            }
        }

            // Dialog boxes for password change and log out
            if(showChangePassDialog){
                BasicAlertDialog(
                    onDismissRequest = {showChangePassDialog = false}
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Enter your email", modifier = Modifier.align(Alignment.CenterHorizontally))
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                var value by remember { mutableStateOf("") }
                                TextField(
                                    value = value,
                                    onValueChange = { it ->
                                        value = it
                                    }
                                )
                                TextButton(onClick = {
                                    showChangePassDialog = false
                                    if(value.isNotEmpty()) {
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(value)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(context, "Please Check Your Email",Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error changing password",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }else{
                                        Toast.makeText(context, "Please enter valid email",Toast.LENGTH_LONG).show()
                                    }
                                }) {
                                    Text("Change Password")
                                }
                            }
                        }
                    }
                }
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
                                    FirebaseAuth.getInstance().signOut()
                                    context.startActivity(intent)
                                    activity?.finish()
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
