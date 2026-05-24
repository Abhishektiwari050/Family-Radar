package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rememberMe by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StarkOffWhite)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header: Giant Chaos Branded Sticker Area
            Box(
                modifier = Modifier
                    .neoShadow(DeepInkBlack, 6.dp, 6.dp, 12.dp)
                    .background(CyberGreen, RoundedCornerShape(12.dp))
                    .neoBorder(3.dp, DeepInkBlack, 12.dp)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(DeepInkBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radar,
                            contentDescription = "Radar Symbol",
                            tint = CyberGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SYS_ACTIVE",
                            color = DeepInkBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "RADAR HUB",
                            color = DeepInkBlack,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Neo Subtitle Sticker label
            Box(
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 4.dp)
                    .background(PinkGlow, RoundedCornerShape(4.dp))
                    .neoBorder(2.dp, DeepInkBlack, 4.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STARK WIREFRAME ED_02",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Brutalist Control Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoShadow(DeepInkBlack, 8.dp, 8.dp, 8.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .neoBorder(3.dp, DeepInkBlack, 8.dp)
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTHENTICATE //",
                            color = DeepInkBlack,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        NeoSticker(
                            text = "HIGH PRIORITY",
                            backgroundColor = CyberGreen
                        )
                    }

                    // Error Box with heavy outline
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PinkGlow.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .neoBorder(2.dp, PinkGlow, 4.dp)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = "Error icon",
                                    tint = PinkGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg.uppercase(),
                                    color = DeepInkBlack,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Username Input inside stark neo border
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "EMAIL OR USERNAME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepInkBlack,
                            letterSpacing = 1.sp
                        )
                        TextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            placeholder = { Text("enter user identifier...", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = DeepInkBlack
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = StarkOffWhite,
                                unfocusedContainerColor = StarkOffWhite,
                                focusedTextColor = DeepInkBlack,
                                unfocusedTextColor = DeepInkBlack,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBorder(2.dp, DeepInkBlack, 6.dp)
                                .testTag("login_username_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    // Password Input inside stark neo border
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SECURITY ACCESS CODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepInkBlack,
                            letterSpacing = 1.sp
                        )
                        TextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("xxxxxx", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = DeepInkBlack
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = DeepInkBlack
                                    )
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = StarkOffWhite,
                                unfocusedContainerColor = StarkOffWhite,
                                focusedTextColor = DeepInkBlack,
                                unfocusedTextColor = DeepInkBlack,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .neoBorder(2.dp, DeepInkBlack, 6.dp)
                                .testTag("login_password_input"),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (username.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter valid login credentials"
                                    } else if (password.length < 4) {
                                        errorMessage = "Password must be at least 4 characters"
                                    } else {
                                        onLoginSuccess(username)
                                    }
                                }
                            )
                        )
                    }

                    // Remember & Action rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DeepInkBlack,
                                    checkmarkColor = CyberGreen,
                                    uncheckedColor = DeepInkBlack
                                )
                            )
                            Text(
                                text = "SAVE_ID",
                                color = DeepInkBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "BYPASS GATEWAY",
                            color = PinkGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .border(1.5.dp, DeepInkBlack, RoundedCornerShape(2.dp))
                                .background(StarkOffWhite)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .clickable {
                                    errorMessage = "security system bypass protocol requires master physical key."
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary Sign-in Button: Heavy Cyber Green Block
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter your Email or Username"
                            } else if (password.length < 4) {
                                errorMessage = "Password must contain at least 4 coordinates"
                            } else {
                                isLoading = true
                                onLoginSuccess(username)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neoShadow(DeepInkBlack, 4.dp, 4.dp, 6.dp)
                            .neoBorder(3.dp, DeepInkBlack, 6.dp)
                            .testTag("login_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberGreen,
                            contentColor = DeepInkBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = DeepInkBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "CONNECT LIVE RADAR 📡",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Guest Bypass option: High Contrast Pink/White Outlined style
                    Button(
                        onClick = {
                            val randomId = (1000..9999).random()
                            onLoginSuccess("Guest_$randomId")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .neoShadow(DeepInkBlack, 4.dp, 4.dp, 6.dp)
                            .neoBorder(3.dp, DeepInkBlack, 6.dp)
                            .testTag("guest_login_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DeepInkBlack
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Guest icon option",
                            tint = PinkGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTINUE AS GUEST //",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Divider chaos-labeled ticker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(1f)
                        .background(DeepInkBlack)
                )
                Text(
                    text = " FAST FEED SYNC ",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = DeepInkBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(1f)
                        .background(DeepInkBlack)
                )
            }

            // Social/Demo Bypass Buttons Stacked or Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onLoginSuccess("GoogleUser")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .neoShadow(DeepInkBlack, 3.dp, 3.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndustrialGray,
                        contentColor = DeepInkBlack
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Google Option",
                        tint = DeepInkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GOOGLE", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = {
                        onLoginSuccess("AppleUser")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .neoShadow(DeepInkBlack, 3.dp, 3.dp, 4.dp)
                        .neoBorder(2.dp, DeepInkBlack, 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndustrialGray,
                        contentColor = DeepInkBlack
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockPerson,
                        contentDescription = "Apple ID Option",
                        tint = DeepInkBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APPLE ID", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Demo notice ticker sticker style
            Box(
                modifier = Modifier
                    .neoShadow(DeepInkBlack, 2.dp, 2.dp, 2.dp)
                    .background(CyberGreen, RoundedCornerShape(2.dp))
                    .neoBorder(2.dp, DeepInkBlack, 2.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "📢 TAP GUEST OR SOCIAL TO BYPASS PROTOCOL",
                    color = DeepInkBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
