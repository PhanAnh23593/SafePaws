package com.hanoipetadoption.features.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.R
import com.hanoipetadoption.ui.components.OtpInputField
import com.hanoipetadoption.ui.components.PrimaryButton
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.DarkGreen
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme
import com.hanoipetadoption.ui.theme.SecondaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    email: String = "ngohung0231@gmail.com",
    onBackClick: () -> Unit = {},
    onVerificationSuccess: () -> Unit = {}
) {
    var otpCode by remember { mutableStateOf("") }
    var isOtpError by remember { mutableStateOf(false) }
    var isOtpSuccess by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // Timer countdown
    var timeLeft by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = isTimerRunning, key2 = timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0) {
            isTimerRunning = false
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Ambient decorative blobs
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 80.dp, start = 16.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0CC).copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 120.dp, end = 20.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDF9F).copy(alpha = 0.35f))
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFEAD8))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.paw_print),
                                    contentDescription = null,
                                    tint = DarkBrownText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "HPA SUPPORT",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrownText
                                    )
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = DarkBrownText
                                )
                            }
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(52.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(horizontal = 24.dp)
            ) {
                // Phần nội dung cuộn bên trên
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Avatar with paw badge overlay
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFFFDBC8).copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.email),
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.paw_print),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = "Xác thực mã OTP 🐾",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    Text(
                        text = "Mã 6 chữ số vừa được gửi đến Email\n${email.ifBlank { "ngohung0231@gmail.com" }}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF6B584C),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card containing OTP input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nhập mã gồm 6 chữ số",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrownText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SecondaryGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Đã gửi Email",
                                        color = DarkGreen,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OtpInputField(
                                otpText = otpCode,
                                onOtpTextChange = { newCode ->
                                    otpCode = newCode
                                    isOtpSuccess = false
                                    isOtpError = false
                                },
                                isError = isOtpError,
                                isSuccess = isOtpSuccess
                            )

                            if (isOtpError) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mã OTP không chính xác, vui lòng thử lại!",
                                    color = Color(0xFFFF655B),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            } else if (isOtpSuccess) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✔ Xác thực thành công! Đang chuyển hướng...",
                                    color = DarkGreen,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Timer & Resend
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.timer),
                                    contentDescription = null,
                                    tint = if (timeLeft > 0) Color.Gray else PrimaryOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (timeLeft > 0) {
                                    Text(
                                        text = "(${timeLeft}s) ",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = "Gửi lại mã ngay",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (timeLeft > 0) Color.Gray else PrimaryOrange,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable(enabled = timeLeft == 0) {
                                        timeLeft = 60
                                        isTimerRunning = true
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            PrimaryButton(
                                leadingIcon = R.drawable.paw_print,
                                text = "Xác nhận & Hoàn tất đăng ký",
                                containerColor = PrimaryOrange,
                                contentColor = Color.White,
                                shadowColor = PrimaryOrange,
                                isLoading = isVerifying,
                                onClick = {
                                    if (otpCode.length == 6 && !isVerifying) {
                                        isVerifying = true
                                        coroutineScope.launch {
                                            delay(800L)
                                            isVerifying = false
                                            if (otpCode == "123456" || otpCode.length == 6) {
                                                isOtpSuccess = true
                                                delay(1000L)
                                                onVerificationSuccess()
                                            } else {
                                                isOtpError = true
                                            }
                                        }
                                    } else {
                                        isOtpError = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Box gọi hỗ trợ cố định ở đáy (cách đáy 12.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dùng Row để sắp xếp Icon và Text nằm ngang hàng nhau
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Trợ giúp",
                            tint = PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp)) // Tạo khoảng cách nhỏ giữa icon và chữ

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cần trợ giúp trực tiếp? Liên hệ Đội cứu trợ HPA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkBrownText,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "0343107742",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryOrange,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpVerificationScreenPreview() {
    SafePawsTheme {
        OtpVerificationScreen(email = "ngohung0231@gmail.com")
    }
}
