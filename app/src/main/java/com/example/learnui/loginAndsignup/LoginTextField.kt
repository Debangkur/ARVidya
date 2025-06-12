package com.example.learnui.loginAndsignup

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.learnui.MainActivity
import com.example.learnui.ui.theme.Black
import com.example.learnui.ui.theme.focusedTextFieldText
import com.example.learnui.ui.theme.textFieldContainer
import com.example.learnui.ui.theme.unfocusedTextFieldText
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    modifier: Modifier = Modifier,
    label: String,
    trailing: String,
    value: String,
    onValueChanged: (String) -> Unit,
    context: Context
    ) {

    val uiColor = if (isSystemInDarkTheme()) Color.White else Black
    var showChangePassDialog by remember { mutableStateOf(false) }

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChanged,
        label = {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = uiColor)
        },
        colors = TextFieldDefaults.colors(
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
            focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
            unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
            focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
        ),
        trailingIcon = {
            TextButton(onClick = {}) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = uiColor,
                    modifier = Modifier.clickable { showChangePassDialog = true }
                )
            }
        }
    )

    // Dialog boxes for password change and log out
    if (showChangePassDialog) {
        BasicAlertDialog(
            onDismissRequest = { showChangePassDialog = false }
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enter your email",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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
                            if (value.isNotEmpty()) {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(value)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context, "Please Check Your Email",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error changing password",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter valid email",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }) {
                            Text("Change Password")
                        }
                    }
                }
            }
        }
    } else {

    }
}
