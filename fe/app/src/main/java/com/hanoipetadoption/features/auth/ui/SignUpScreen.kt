package com.hanoipetadoption.features.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.R
import com.hanoipetadoption.ui.components.CustomTextField
import com.hanoipetadoption.ui.components.PrimaryButton
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.DarkGreen
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme
import com.hanoipetadoption.ui.theme.SecondaryGreen
import com.hanoipetadoption.ui.theme.TertiaryYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOtp: (String) -> Unit = {}
) {
    var fullName by remember { mutableStateOf("Nguyễn Văn A") }
    var phoneNumber by remember { mutableStateOf("0987654321") }
    var email by remember { mutableStateOf("ban@email.com") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    // Tự động cuộn xuống khi bật bàn phím
    LaunchedEffect(imeBottomPadding) {
        if (imeBottomPadding > 0.dp) {
            delay(150L)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Tiêu chí kiểm tra mật khẩu
    val hasMinLength = password.length >= 8
    val hasUpperAndLower = password.any { it.isUpperCase() } && password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    val criteriaCount = listOf(hasMinLength, hasUpperAndLower, hasDigit, hasSpecialChar).count { it }

    val (strengthLabel, strengthColor) = when {
        password.isEmpty() -> "Chưa nhập" to Color.Gray
        criteriaCount <= 1 -> "Yếu" to Color(0xFFFF3B30)
        criteriaCount == 2 -> "Trung bình" to TertiaryYellow
        criteriaCount == 3 -> "Tốt" to SecondaryGreen
        else -> "Rất tốt" to DarkGreen
    }

    val isConfirmMatch = confirmPassword.isNotEmpty() && confirmPassword == password

    fun handleSignUp() {
        errorMessage = null

        if (fullName.isBlank()) {
            errorMessage = "Vui lòng nhập họ và tên"
            return
        }
        if (phoneNumber.isBlank()) {
            errorMessage = "Vui lòng nhập số điện thoại"
            return
        }
        if (email.isBlank()) {
            errorMessage = "Vui lòng nhập địa chỉ email"
            return
        }
        if (password.isBlank()) {
            errorMessage = "Vui lòng nhập mật khẩu"
            return
        }
        if (criteriaCount < 2) {
            errorMessage = "Mật khẩu quá yếu. Vui lòng đặt mật khẩu mạnh hơn!"
            return
        }
        if (confirmPassword.isBlank() || password != confirmPassword) {
            errorMessage = "Mật khẩu xác nhận không trùng khớp"
            return
        }
        if (!agreeTerms) {
            errorMessage = "Bạn cần đồng ý với Điều khoản và Chính sách của HPA"
            return
        }

        isLoading = true
        coroutineScope.launch {
            delay(800L) // Giả lập gọi API đăng ký
            isLoading = false
            onNavigateToOtp(email)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Đốm mờ trang trí phía trên
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDBC8).copy(alpha = 0.35f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 140.dp, start = 10.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDF9F).copy(alpha = 0.25f))
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {},
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, top = 8.dp)
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Column phần trên cuộn nội dung
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tiêu đề
                    Text(
                        text = "Tạo tài khoản mới 🐶",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText,
                            fontSize = 28.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trở thành người đồng hành hoặc nhận nuôi những bé cún, bé mèo đáng yêu tại Hà Nội.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Field 1: Họ và tên
                    CustomTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = "Họ và tên *",
                        placeholder = "Nguyễn Văn A",
                        leadingIconRes = R.drawable.profile,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Số điện thoại
                    CustomTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            errorMessage = null
                        },
                        label = "Số điện thoại *",
                        placeholder = "0987 654 321",
                        leadingIconRes = R.drawable.phone,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 3: Địa chỉ Email
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = "Địa chỉ Email *",
                        placeholder = "ban@email.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 4: Mật khẩu
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mật khẩu *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBrownText
                                )
                            )

                            if (password.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.shield_check),
                                        contentDescription = null,
                                        tint = strengthColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strengthLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = strengthColor,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        CustomTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = null,
                            placeholder = "••••••••••••",
                            leadingIconRes = R.drawable.lock,
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            background = Color.White
                        )

                        if (password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Thanh đo độ cứng mật khẩu 4 vạch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🐾 Độ cứng & an toàn: $strengthLabel",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = strengthColor,
                                        fontSize = 12.sp
                                    )
                                )
                                Text(
                                    text = "$criteriaCount/4 🐾",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = strengthColor,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(4) { index ->
                                    val isFilled = index < criteriaCount
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isFilled) strengthColor else Color(0xFFE0E0E0))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 5: Xác nhận mật khẩu
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Xác nhận mật khẩu *",
                        placeholder = "••••••••••••",
                        leadingIconRes = R.drawable.shield_check,
                        isPassword = true,
                        trailingIconComposable = {
                            if (isConfirmMatch) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Trùng khớp",
                                    tint = DarkGreen,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(22.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Checkbox Đồng ý điều khoản
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreeTerms = !agreeTerms },
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (agreeTerms) PrimaryOrange else Color(0xFFEAE8E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (agreeTerms) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Tôi đồng ý với ")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrownText,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("Điều khoản nhận nuôi")
                                }
                                append(" & ")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBrownText,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("Chính sách bảo vệ động vật")
                                }
                                append(" của HPA.")
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF584235),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Banner thông báo lỗi nếu có
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
                                        contentDescription = null,
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

                    // Nút Đăng ký (Gửi mã OTP)
                    PrimaryButton(
                        leadingIcon = R.drawable.paw_print,
                        text = "Gửi mã OTP",
                        containerColor = PrimaryOrange,
                        contentColor = Color.White,
                        shadowColor = PrimaryOrange,
                        isLoading = isLoading,
                        onClick = { handleSignUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Dòng Chuyển sang Đăng nhập ở chân trang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đã có tài khoản? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Đăng nhập",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SafePawsTheme {
        SignUpScreen()
    }
}
