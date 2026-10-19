package com.hanoipetadoption.features.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.hanoipetadoption.core.utils.UserPreferences
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.R
import com.hanoipetadoption.ui.components.CustomTextField
import com.hanoipetadoption.ui.components.PrimaryButton
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onGoogleLoginClick: () -> Unit = {}
) {
    var identifier by remember { mutableStateOf("taikhoan@gmail.com") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    // Quản lý trạng thái Validation & API Loading
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Hàm xử lý đăng nhập (đã thiết lập sẵn để tích hợp API trong tương lai)
    fun handleLogin() {
        // Reset thông báo lỗi cũ
        identifierError = null
        passwordError = null
        errorMessage = null

        var hasError = false
        if (identifier.isBlank()) {
            identifierError = "Vui lòng nhập số điện thoại hoặc email"
            hasError = true
        }
        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            hasError = true
        }

        if (hasError) return

        isLoading = true
        coroutineScope.launch {
            // Giả lập thời gian phản hồi API (800ms)
            delay(800)
            isLoading = false

            /*
             * TODO: Tích hợp API Đăng nhập thực tế tại đây:
             * Example:
             * val result = authRepository.login(identifier, password)
             * if (result.isSuccess) {
             *     onLoginSuccess()
             * } else {
             *     errorMessage = result.errorMessage ?: "Tài khoản hoặc mật khẩu không chính xác"
             * }
             */

            // Giả lập kiểm tra tài khoản & mật khẩu (Mock Validation)
            // Tài khoản đúng mẫu: email/sđt chuẩn và mật khẩu "123456" hoặc "admin123"
            val isMockValidAccount = ((identifier == "nhanvien@hpa.org" || identifier == "0912345678" || identifier.contains("@")))
                    && (password == "123456" || password == "admin123" || password == "12345678")

            if (isMockValidAccount) {
                UserPreferences.setLoggedIn(context, true)
                onLoginSuccess()
            } else {
                // Hiện thông báo sai tài khoản/mật khẩu theo yêu cầu
                errorMessage = "Tài khoản hoặc mật khẩu không chính xác"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Đốm mờ nền trang trí phía trên - bên phải
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDBC8).copy(alpha = 0.35f))
        )

        // Đốm mờ nền trang trí phía trên - bên trái
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 180.dp, start = 10.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDF9F).copy(alpha = 0.25f))
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Column phần trên: chứa toàn bộ nội dung đăng nhập và có thể cuộn
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(28.dp))

                    // Badge thương hiệu Hanoi Pet Adoption
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFFFDBC8).copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hanoi Pet Adoption",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF321200),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tiêu đề chào mừng
                    Text(
                        text = "Chào mừng trở lại! 🐾",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText,
                            fontSize = 26.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mô tả phụ
                    Text(
                        text = "Đăng nhập để tiếp tục hành trình yêu thương và tìm mái ấm cho thú cưng",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Ô nhập Số điện thoại / Email
                    CustomTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            identifierError = null
                            errorMessage = null
                        },
                        label = "Số điện thoại  hoặc Email",
                        placeholder = "taikhoan@gmail.com",
                        leadingIcon = Icons.Default.Email,
                        errorMessage = identifierError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ô nhập Mật khẩu
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = null
                            errorMessage = null
                        },
                        label = "Mật khẩu",
                        placeholder = "Nhập mật khẩu của bạn",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hàng tiện ích: Ghi nhớ đăng nhập & Quên mật khẩu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox Ghi nhớ đăng nhập
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (rememberMe) PrimaryOrange else Color(0xFFEAE8E5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ghi nhớ đăng nhập",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (rememberMe) DarkBrownText else Color.Gray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Quên mật khẩu?
                        Text(
                            text = "Quên mật khẩu?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onNavigateToForgotPassword() }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Khung hiển thị thông báo lỗi sai tài khoản hoặc mật khẩu
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFDAD6),
                                border = BorderStroke(1.dp, Color(0xFFFFB4AB))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Lỗi",
                                        tint = Color(0xFFBA1A1A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color(0xFF93000A),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Nút Đăng nhập chính
                    PrimaryButton(
                        leadingIcon = R.drawable.paw_print,
                        text = "Đăng nhập",
                        onClick = { handleLogin() },
                        isLoading = isLoading,
                        containerColor = PrimaryOrange,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Thanh phân cách "Hoặc tiếp tục với"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0C0AF).copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                        Text(
                            text = "Hoặc tiếp tục với",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF584235),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0C0AF).copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Nút Đăng nhập bằng Google
                    Surface(
                        onClick = onGoogleLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleLogoIcon()
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tiếp tục với Google",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBrownText,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Dòng Đăng ký tài khoản
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chưa có tài khoản? ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF584235),
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "Đăng ký ngay",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToSignUp() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Column phần dưới: chứa Badge sứ mệnh cố định sát đáy màn hình, cách đáy 12dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFFEAE8E5).copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.paw_print),
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hanoi Pet Adoption • Mái ấm cho các bé thú cưng",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF584235),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vẽ Logo Google chuẩn 4 màu
 */
@Composable
private fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        // Màu Xanh Dương (#4285F4)
        val bluePath = Path().apply {
            moveTo(width * 0.94f, height * 0.51f)
            cubicTo(width * 0.94f, height * 0.48f, width * 0.937f, height * 0.448f, width * 0.932f, height * 0.418f)
            lineTo(width * 0.5f, height * 0.418f)
            lineTo(width * 0.5f, height * 0.595f)
            lineTo(width * 0.747f, height * 0.595f)
            cubicTo(width * 0.736f, height * 0.652f, width * 0.703f, height * 0.701f, width * 0.654f, height * 0.733f)
            lineTo(width * 0.654f, height * 0.849f)
            lineTo(width * 0.803f, height * 0.849f)
            cubicTo(width * 0.89f, height * 0.769f, width * 0.94f, height * 0.651f, width * 0.94f, height * 0.51f)
            close()
        }
        drawPath(bluePath, color = Color(0xFF4285F4))

        // Màu Xanh Lá (#34A853)
        val greenPath = Path().apply {
            moveTo(width * 0.5f, height * 0.958f)
            cubicTo(width * 0.624f, height * 0.958f, width * 0.728f, height * 0.917f, width * 0.803f, height * 0.849f)
            lineTo(width * 0.654f, height * 0.733f)
            cubicTo(width * 0.613f, height * 0.761f, width * 0.561f, height * 0.778f, width * 0.5f, height * 0.778f)
            cubicTo(width * 0.381f, height * 0.778f, width * 0.28f, height * 0.698f, width * 0.243f, height * 0.589f)
            lineTo(width * 0.091f, height * 0.589f)
            lineTo(width * 0.091f, height * 0.708f)
            cubicTo(width * 0.166f, height * 0.857f, width * 0.321f, height * 0.958f, width * 0.5f, height * 0.958f)
            close()
        }
        drawPath(greenPath, color = Color(0xFF34A853))

        // Màu Vàng (#FBBC05)
        val yellowPath = Path().apply {
            moveTo(width * 0.243f, height * 0.589f)
            cubicTo(width * 0.234f, height * 0.561f, width * 0.229f, height * 0.532f, width * 0.229f, height * 0.5f)
            cubicTo(width * 0.229f, height * 0.468f, width * 0.234f, height * 0.439f, width * 0.243f, height * 0.411f)
            lineTo(width * 0.243f, height * 0.292f)
            lineTo(width * 0.091f, height * 0.292f)
            cubicTo(width * 0.06f, height * 0.354f, width * 0.042f, height * 0.424f, width * 0.042f, height * 0.5f)
            cubicTo(width * 0.042f, height * 0.576f, width * 0.06f, height * 0.646f, width * 0.091f, height * 0.708f)
            lineTo(width * 0.243f, height * 0.589f)
            close()
        }
        drawPath(yellowPath, color = Color(0xFFFBBC05))

        // Màu Đỏ (#EA4335)
        val redPath = Path().apply {
            moveTo(width * 0.5f, height * 0.222f)
            cubicTo(width * 0.568f, height * 0.222f, width * 0.628f, height * 0.245f, width * 0.675f, height * 0.29f)
            lineTo(width * 0.806f, height * 0.159f)
            cubicTo(width * 0.727f, height * 0.085f, width * 0.624f, height * 0.042f, width * 0.5f, height * 0.042f)
            cubicTo(width * 0.321f, height * 0.042f, width * 0.166f, height * 0.143f, width * 0.091f, height * 0.292f)
            lineTo(width * 0.243f, height * 0.411f)
            cubicTo(width * 0.28f, height * 0.302f, width * 0.381f, height * 0.222f, width * 0.5f, height * 0.222f)
            close()
        }
        drawPath(redPath, color = Color(0xFFEA4335))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SafePawsTheme {
        LoginScreen()
    }
}
