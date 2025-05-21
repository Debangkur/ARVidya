
package com.example.learnui.loginAndsignup

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnui.R
import com.example.learnui.ui.theme.Black
import com.example.learnui.ui.theme.BlueGray

@Composable
fun SignUpScreen(onSignUp: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {

            SignUpTopSection()
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
            ) {

                SignUpSection(
                    username = username,
                    onUsernameChanged = {username = it},
                    email = email,
                    onEmailChanged = {email = it },
                    password = password,
                    onPasswordChanged = {password = it},
                    onSignUpClick = {onSignUp(email,password)}
                )
                Spacer(modifier = Modifier.height(30.dp))

                SocialMediaSection()

            }
        }
    }
}

@Composable
fun SignUpTopSection() {
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
            modifier = Modifier.padding(top = 120.dp),
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
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            text = stringResource(R.string.sign_up) ,
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Composable
private fun SignUpSection(
    username: String,
    onUsernameChanged: (String) -> Unit,
    email: String,
    onEmailChanged: (String) -> Unit,
    password: String,
    onPasswordChanged: (String) -> Unit,
    onSignUpClick: () -> Unit
){
    LoginTextField(
        label = "Username",
        trailing = "",
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChanged = onUsernameChanged
    )
    Spacer(modifier = Modifier.height(15.dp))

    LoginTextField(label = "Email",
        trailing = "",
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChanged = onEmailChanged
    )
    Spacer(modifier = Modifier.height(15.dp))

    LoginTextField(label = "Password",
        trailing = "Forgot?",
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChanged = onPasswordChanged
    )
    Spacer(modifier = Modifier.height(15.dp))

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        onClick = onSignUpClick,
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
