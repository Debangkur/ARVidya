package com.example.learnui.loginAndsignup

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.learnui.ui.theme.Black
import com.example.learnui.ui.theme.focusedTextFieldText
import com.example.learnui.ui.theme.textFieldContainer
import com.example.learnui.ui.theme.unfocusedTextFieldText

@Composable
fun SignUpTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    ) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black

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
        )
    )
}