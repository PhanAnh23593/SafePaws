package com.hanoipetadoption.features.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.R
import com.hanoipetadoption.ui.components.CustomTextField
import com.hanoipetadoption.ui.components.OtpInputField
import com.hanoipetadoption.ui.components.PrimaryButton
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.DarkGreen
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme
import com.hanoipetadoption.ui.theme.SecondaryGreen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onNavigateToResetPassword: () -> Unit
) {
    var email by remember { mutableStateOf("hanoipetrescue@gmail.com") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // State kiểm tra đúng/sai mã OTP và API loading
    var isOtpError by remember { mutableStateOf(false) }
    var isOtpSuccess by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    // Quản lý đếm ngược Timer
    var timeLeft by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Coroutine đếm ngược mỗi 1 giây
    LaunchedEffect(key1 = isTimerRunning, key2 = timeLeft) {
        if (isTimerRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
        } else if (timeLeft == 0) {
            isTimerRunning = false
        }
    }

    // State cuộn trang & Coroutine xử lý cuộn tự động khi bật bàn phím
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    // Tự động cuộn xuống dưới cùng để hiển thị mã OTP và nút xác nhận ngay trên bàn phím
    LaunchedEffect(key1 = isOtpSent, key2 = imeBottomPadding) {
        if (isOtpSent) {
            delay(250L) // Chờ khung OTP xả ra và bàn phím nẩy lên
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Đốm mờ góc trên - bên trái
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 100.dp, start = 12.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0CC).copy(alpha = 0.5f))
        )

        // Đốm mờ góc trên - bên phải
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 20.dp)
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0CC).copy(alpha = 0.3f))
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.paw_print),
                                    contentDescription = "Icon Support",
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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

                    // Icon Hero Badge: Vòng ngoài nhạt + Vòng trong cam
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFDBC8).copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔑",
                                fontSize = 36.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tiêu đề
                    Text(
                        text = "Quên mật khẩu? 🐾",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Nhập số điện thoại hoặc email đã đăng ký của bạn để nhận mã xác minh đặt lại mật khẩu.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Card nhập liệu chính
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color.White)
                            .padding(20.dp)
                    ) {
                        Column {
                            CustomTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email",
                                placeholder = "Nhập email",
                                leadingIcon = Icons.Default.Email,
                                background = Color(0xFFF5F3F0)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PrimaryButton(
                                leadingIcon = R.drawable.send_otp,
                                text = "Gửi mã xác thực OTP",
                                style = TextStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily(Font(R.font.plusjakartasans_regular)),
                                    fontSize = 16.sp
                                ),
                                containerColor = if (isOtpSent) Color(0xFFEAE8E5) else PrimaryOrange,
                                contentColor = if (isOtpSent) DarkBrownText else Color.White,
                                shadowColor = if (isOtpSent) Color.Transparent else PrimaryOrange,
                                onClick = {
                                    isOtpSent = true
                                    isTimerRunning = true
                                    coroutineScope.launch {
                                        delay(250L)
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )

                            // Thả xuống khung OTP khi click Gửi mã
                            AnimatedVisibility(
                                visible = isOtpSent,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Thanh ngang màu xám phân cách
                                    HorizontalDivider(
                                        color = Color(0xFFEFEEEB),
                                        thickness = 1.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

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
                                                text = "Đã gửi SMS",
                                                color = DarkGreen,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Khung nhập 6 số OTP với xử lý màu nền đúng (5dff95) / sai (ff655b)
                                    OtpInputField(
                                        otpText = otpCode,
                                        onOtpTextChange = { newCode ->
                                            otpCode = newCode
                                            // Reset thông báo đúng/sai khi người dùng nhập lại mã
                                            isOtpSuccess = false
                                            isOtpError = false
                                        },
                                        isError = isOtpError,
                                        isSuccess = isOtpSuccess
                                    )

                                    if (isOtpError) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Mã OTP không chính xác, vui lòng thử lại!",
                                            color = Color(0xFFFF655B),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    } else if (isOtpSuccess) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "✔ Mã OTP chính xác!",
                                            color = DarkGreen,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.timer),
                                            contentDescription = "Timer",
                                            tint = if (timeLeft > 0) Color.Gray else PrimaryOrange,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Số giây đếm ngược (nếu đang đếm)
                                        if (timeLeft > 0) {
                                            Text(
                                                text = "(${timeLeft}s) ",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        // Text Gửi lại mã
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
                                                /* TODO: Gọi lại API gửi OTP */
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    PrimaryButton(
                                        leadingIcon = R.drawable.paw_print,
                                        text = "Xác nhận & Tiếp tục",
                                        containerColor = PrimaryOrange,
                                        contentColor = Color.White,
                                        shadowColor = PrimaryOrange,
                                        isLoading = isVerifyingOtp,
                                        onClick = {
                                            if (otpCode.length == 6 && !isVerifyingOtp) {
                                                isVerifyingOtp = true
                                                isOtpSuccess = false
                                                isOtpError = false

                                                coroutineScope.launch {
                                                    // Giả lập gọi API kiểm tra OTP (800ms)
                                                    delay(800L)

                                                    /*
                                                     * TODO: Tích hợp API kiểm tra OTP thực tế tại đây:
                                                     * val result = authRepository.verifyOtp(email, otpCode)
                                                     * isApiSuccess = result.isSuccess
                                                     */
                                                    val isApiSuccess = (otpCode == "123456") // Mã OTP đúng mẫu: 123456

                                                    if (isApiSuccess) {
                                                        isOtpSuccess = true
                                                        isOtpError = false
                                                        isVerifyingOtp = false

                                                        // Chờ 500ms hiển thị kết quả đúng trước khi chuyển màn hình
                                                        delay(500L)
                                                        onNavigateToResetPassword()
                                                    } else {
                                                        isOtpSuccess = false
                                                        isOtpError = true
                                                        isVerifyingOtp = false
                                                    }
                                                }
                                            }
                                        },
                                        enabled = otpCode.length == 6 && !isVerifyingOtp
                                    )
                                }
                            }
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

@Preview
@Composable
fun ForgotPasswordScreenPreview() {
    SafePawsTheme {
        ForgotPasswordScreen(
            onBackClick = {},
            onNavigateToResetPassword = {}
        )
    }
}
