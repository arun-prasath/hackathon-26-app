package com.demo.mobilebankingassistant.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.demo.mobilebankingassistant.R
import com.demo.mobilebankingassistant.data.BankingUser
import com.demo.mobilebankingassistant.ui.theme.ScBlue
import com.demo.mobilebankingassistant.ui.theme.ScGreen
import com.demo.mobilebankingassistant.ui.theme.ScLightBlue

@Composable
fun LoginScreen(
    user: BankingUser,
    onLoginSuccess: () -> Unit,
    onBiometricClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF064AF4), ScBlue, ScLightBlue, ScGreen, Color.White),
                    startY = 0f,
                    endY = 1500f
                )
            )
    ) {
        Icon(
            imageVector = Icons.Default.HelpOutline,
            contentDescription = "Help",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 34.dp, end = 22.dp)
                .size(24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 74.dp, start = 24.dp, end = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.smart_banking_header),
                    contentDescription = "Smart banking assistant",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xDD06307A), Color(0x88064AF4), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp, end = 18.dp, bottom = 10.dp)
                ) {
                    Text(
                        "SC Smart Banking",
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 29.sp
                    )
                    Text(
                        "powered by AINigmas",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 236.dp, start = 34.dp, end = 34.dp)
        ) {
            Text("Welcome back", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(user.name.uppercase(), color = Color.White, fontSize = 17.sp)
                Spacer(Modifier.width(14.dp))
                Text("Not you?", color = Color.White, fontSize = 12.sp, textDecoration = TextDecoration.Underline)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .align(Alignment.Center),
            color = Color(0xFFF9F4F7),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 34.dp)
        ) {
            Column(Modifier.padding(start = 34.dp, end = 34.dp, top = 38.dp)) {
                Text("Password", color = Color(0xFF747184), fontSize = 13.sp)
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF8F8A95),
                        unfocusedIndicatorColor = Color(0xFF8F8A95)
                    )
                )
                TextButton(onClick = { }) {
                    Text("Forgot?", color = Color(0xFF1D64D8), fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 22.dp, bottom = 28.dp)
        ) {
            TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("Generate Offline PIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScGreen),
                    shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ScBlue)
                    Spacer(Modifier.width(14.dp))
                    Text("Login", color = ScBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Surface(
                    onClick = onBiometricClick,
                    modifier = Modifier.size(58.dp),
                    color = Color(0xFFE8EEF8),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = ScBlue, modifier = Modifier.size(35.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricLoginScreen(onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F2F7))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(310.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF3C2CA8), Color(0xFF332080))),
                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Text("SC Mobile India", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Text("Login with Biometrics", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(42.dp))
            Surface(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(36.dp), color = Color.White.copy(alpha = 0.08f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(48.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Face not recognised. Use fingerprint instead.", color = Color(0xFFFF9A9A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.Start)) {
                Text("Cancel", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
