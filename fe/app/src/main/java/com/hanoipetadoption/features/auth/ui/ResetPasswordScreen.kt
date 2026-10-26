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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hanoipetadoption.R
import com.hanoipetadoption.core.utils.UserPreferences
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
fun ResetPasswordScreen(
    userEmail: String = "ngohung0231@gmail.com",
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var logoutOtherDevices by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
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

    // Các tiêu chí kiểm tra mật khẩu
    val hasMinLength = newPassword.length >= 8
    val hasUpperAndLower = newPassword.any { it.isUpperCase() } && newPassword.any { it.isLowerCase() }
    val hasDigit = newPassword.any { it.isDigit() }
    val hasSpecialChar = newPassword.any { !it.isLetterOrDigit() }

    val criteriaCount = listOf(hasMinLength, hasUpperAndLower, hasDigit, hasSpecialChar).count { it }

    val (strengthLabel, strengthColor) = when {
        newPassword.isEmpty() -> "Chưa nhập" to Color.Gray
        criteriaCount <= 1 -> "Yếu" to Color(0xFFFF3B30)
        criteriaCount == 2 -> "Trung bình" to TertiaryYellow
        criteriaCount == 3 -> "An toàn" to SecondaryGreen
        else -> "Rất an toàn" to DarkGreen
    }

    val isConfirmMatch = confirmPassword.isNotEmpty() && confirmPassword == newPassword
    val isConfirmMismatch = confirmPassword.isNotEmpty() && confirmPassword != newPassword

    fun handleResetPassword() {
        newPasswordError = null
        confirmPasswordError = null
        errorMessage = null

        var hasError = false
        if (newPassword.isBlank()) {
            newPasswordError = "Vui lòng nhập mật khẩu mới"
            hasError = true
        } else if (criteriaCount < 2) {
            newPasswordError = "Mật khẩu chưa đủ độ an toàn. Vui lòng hoàn thành thêm tiêu chí!"
            hasError = true
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Vui lòng xác nhận lại mật khẩu mới"
            hasError = true
        } else if (newPassword != confirmPassword) {
            confirmPasswordError = "Mật khẩu xác nhận không trùng khớp!"
            hasError = true
        }

        if (hasError) return

        isLoading = true
        coroutineScope.launch {
            delay(1000L) // Giả lập gọi API cập nhật mật khẩu
            isLoading = false

            /*
             * TODO: Tích hợp API Cập nhật mật khẩu:
             * val result = authRepository.resetPassword(userEmail, newPassword, logoutOtherDevices)
             */
            UserPreferences.setLoggedIn(context, false)
            showSuccessDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
    ) {
        // Background Accent Circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDBC8).copy(alpha = 0.4f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 120.dp, start = 16.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFE0CC).copy(alpha = 0.35f))
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
                // Column Nội dung cuộn
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Icon Circle: Cam + Vòng xanh lục với Icon reset_password ở giữa
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
                            Icon(
                                painter = painterResource(id = R.drawable.reset_password),
                                contentDescription = "Reset Password Icon",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkGreen)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tiêu đề
                    Text(
                        text = "Tạo mật khẩu mới",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText,
                            fontSize = 26.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mã OTP đã được xác thực thành công. Vui lòng thiết lập mật khẩu mới an toàn cho tài khoản bảo hộ của bạn.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pill hiển thị email đã xác thực
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFFF5F3F0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tài khoản: ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF584235),
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBrownText,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Mật khẩu mới
                    CustomTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            newPasswordError = null
                            errorMessage = null
                        },
                        label = "Mật khẩu mới *",
                        placeholder = "Nhập mật khẩu mới",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = newPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Thanh đo độ bảo mật mật khẩu
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Độ bảo mật:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF584235),
                                    fontSize = 13.sp
                                )
                            )
                            Text(
                                text = strengthLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = strengthColor,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 4 vạch độ bền
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Danh sách 4 tiêu chí dạng 2 cột
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                CriteriaCheckItem("Ít nhất 8 ký tự", hasMinLength)
                                Spacer(modifier = Modifier.height(6.dp))
                                CriteriaCheckItem("Ít nhất 1 chữ số", hasDigit)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                CriteriaCheckItem("Chữ hoa & chữ thường", hasUpperAndLower)
                                Spacer(modifier = Modifier.height(6.dp))
                                CriteriaCheckItem("Ký tự đặc biệt (@, #...)", hasSpecialChar)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 2: Xác nhận mật khẩu mới
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                            errorMessage = null
                        },
                        label = "Xác nhận mật khẩu mới *",
                        placeholder = "••••••••••••",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        background = Color.White
                    )

                    if (isConfirmMatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mật khẩu trùng khớp hoàn toàn",
                                color = DarkGreen,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else if (isConfirmMismatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFBA1A1A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mật khẩu xác nhận không trùng khớp",
                                color = Color(0xFFBA1A1A),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Card Checkbox đăng xuất khỏi thiết bị khác
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { logoutOtherDevices = !logoutOtherDevices },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (logoutOtherDevices) PrimaryOrange else Color(0xFFEAE8E5)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoutOtherDevices) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Đăng xuất khỏi tất cả các thiết bị khác để đảm bảo an toàn tối đa",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DarkBrownText,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hiển thị Banner lỗi nếu có
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

                    // Nút Đăng nhập / Cập nhật
                    PrimaryButton(
                        leadingIcon = R.drawable.paw_print,
                        text = "Cập nhật",
                        containerColor = PrimaryOrange,
                        contentColor = Color.White,
                        shadowColor = PrimaryOrange,
                        isLoading = isLoading,
                        onClick = { handleResetPassword() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dòng Hủy bỏ
                    Text(
                        text = "Hủy bỏ và quay lại Đăng nhập",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateToLogin() }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )

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

        // Popup Dialog thông báo Đổi mật khẩu thành công
        if (showSuccessDialog) {
            Dialog(
                onDismissRequest = { /* Modal, không tắt khi click ra ngoài */ },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Badge thành công: Vòng tròn xanh lục + Dấu tích + Paw cam
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2F3E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(DarkGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Check",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryOrange)
                                        .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.paw_print),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Tiêu đề
                            Text(
                                text = "Đổi mật khẩu thành công!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBrownText,
                                    fontSize = 22.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mô tả
                            Text(
                                text = buildAnnotatedString {
                                    append("Mật khẩu cho tài khoản ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkBrownText)) {
                                        append(userEmail)
                                    }
                                    append(" đã được cập nhật thành công. Bạn có thể sử dụng mật khẩu mới để đăng nhập ngay bây giờ.")
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF584235),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Nút Đăng nhập ngay
                            PrimaryButton(
                                leadingIcon = R.drawable.login,
                                text = "Đăng nhập ngay",
                                containerColor = PrimaryOrange,
                                contentColor = Color.White,
                                shadowColor = PrimaryOrange,
                                onClick = {
                                    showSuccessDialog = false
                                    UserPreferences.setLoggedIn(context, false)
                                    onNavigateToLogin()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mẫu Item Checkmark Tiêu chí Mật khẩu
 */
@Composable
private fun CriteriaCheckItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isMet) DarkGreen else Color(0xFFB0A8A0),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isMet) DarkGreen else Color(0xFF786B60),
                fontSize = 12.sp,
                fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    SafePawsTheme {
        ResetPasswordScreen()
    }
}
