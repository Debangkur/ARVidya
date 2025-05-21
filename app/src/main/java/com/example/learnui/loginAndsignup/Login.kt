package com.example.learnui.loginAndsignup

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learnui.MainActivity
import com.example.learnui.R
import com.example.learnui.ui.theme.Black
import com.example.learnui.ui.theme.BlueGray
import com.example.learnui.ui.theme.Roboto

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {

            TopSection()
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
            ) {

                LoginSection(
                    email = email,
                    onEmailChanged = {email = it },
                    password = password,
                    onPasswordChanged = {password = it},
                    onLoginClick = {onLogin(email,password)}
                )
                Spacer(modifier = Modifier.height(30.dp))

                SocialMediaSection()

                NewAccountCreation()

            }
        }
    }
}

@Composable
private fun NewAccountCreation() {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.8f),
        contentAlignment = Alignment.BottomCenter
    ) {

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append("Don't have account?")
                }
                withStyle(
                    style = SpanStyle(
                        color = uiColor,
                        fontSize = 14.sp,
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("Create New")
                }
            }
        )
    }
}

@Composable
 fun SocialMediaSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Or continue with",
            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B))
        )
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialMediaLogin(
                icon = R.drawable.google,
                text = "Google",
                modifier = Modifier.weight(1f)
            ) {

            }
            Spacer(modifier = Modifier.width(20.dp))
            SocialMediaLogin(
                icon = R.drawable.facebook,
                text = "Facebook",
                modifier = Modifier.weight(1f)
            ) {

            }
        }
    }
}

@Composable
    private fun TopSection() {
        val uiColor = if (isSystemInDarkTheme()) Color.White else Black

        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(fraction = 0.5f),
                painter = painterResource(if (isSystemInDarkTheme()) R.drawable.shape_night else R.drawable.shape),
                contentDescription = null
            )

            Row(
                modifier = Modifier.padding(top = 110.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(42.dp),
                    painter = painterResource(R.drawable.arlogo),
                    contentDescription = stringResource(id = R.string.app_logo),
                    tint = uiColor
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = stringResource(id = R.string.app_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = uiColor
                )
            }

            Text(
                modifier = Modifier.padding(bottom = 10.dp)
                    .align(alignment = Alignment.BottomCenter),
                text = stringResource(R.string.login) ,
                style = MaterialTheme.typography.headlineLarge,
            )
        }
    }

    @Composable
    private fun LoginSection(
        email: String,
        onEmailChanged: (String) -> Unit,
        password: String,
        onPasswordChanged: (String) -> Unit,
        onLoginClick: () -> Unit
    ) {
        LoginTextField(
            label = "Email",
            trailing = "",
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChanged = onEmailChanged
        )
        Spacer(modifier = Modifier.height(15.dp))

        LoginTextField(
            label = "Password",
            trailing = "Forgot?",
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChanged = onPasswordChanged
        )
        Spacer(modifier = Modifier.height(15.dp))

        Button(
            modifier = Modifier.fillMaxWidth()
                .height(40.dp),
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(size = 4.dp)
        ) {
            Text(
                text = "Log in",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            )
        }

    }




