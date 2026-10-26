package com.hanoipetadoption.features.auth.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToOtp: (String) -> Unit = {}
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val initialYear = currentYear - 18

    var fullName by remember { mutableStateOf("") }
    var selectedDay by remember { mutableIntStateOf(1) }
    var selectedMonth by remember { mutableIntStateOf(1) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var birthDateStr by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(true) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
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
        fullNameError = null
        birthDateError = null
        phoneNumberError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null
        errorMessage = null

        var hasError = false

        if (fullName.isBlank()) {
            fullNameError = "Vui lòng nhập họ và tên"
            hasError = true
        }
        if (birthDateStr.isBlank()) {
            birthDateError = "Vui lòng chọn ngày sinh"
            hasError = true
        }
        if (phoneNumber.isBlank()) {
            phoneNumberError = "Vui lòng nhập số điện thoại"
            hasError = true
        }
        if (email.isBlank()) {
            emailError = "Vui lòng nhập địa chỉ email"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Địa chỉ email không hợp lệ (VD: name@example.com)"
            hasError = true
        }
        if (password.isBlank()) {
            passwordError = "Vui lòng nhập mật khẩu"
            hasError = true
        } else if (criteriaCount < 2) {
            passwordError = "Mật khẩu quá yếu. Vui lòng đặt mật khẩu mạnh hơn!"
            hasError = true
        }
        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Vui lòng nhập mật khẩu xác nhận"
            hasError = true
        } else if (password != confirmPassword) {
            confirmPasswordError = "Mật khẩu xác nhận không trùng khớp"
            hasError = true
        }
        if (!agreeTerms) {
            errorMessage = "Bạn cần đồng ý với Điều khoản và Chính sách của HPA"
            hasError = true
        }

        if (hasError) return

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
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

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = "Tạo tài khoản mới 🐶",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBrownText,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
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
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Trở thành người đồng hành hoặc nhận nuôi những bé cún, bé mèo đáng yêu tại Hà Nội.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF584235),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Họ và tên
                    CustomTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            fullNameError = null
                            errorMessage = null
                        },
                        label = "Họ và tên *",
                        placeholder = "Nguyễn Văn A",
                        leadingIconRes = R.drawable.profile,
                        errorMessage = fullNameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Ngày sinh
                    CustomTextField(
                        value = birthDateStr,
                        onValueChange = {},
                        label = "Ngày sinh *",
                        placeholder = "01/01/2008",
                        leadingIconRes = R.drawable.birthday,
                        errorMessage = birthDateError,
                        background = Color.White,
                        readOnly = true,
                        onClick = {
                            birthDateError = null
                            errorMessage = null
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 3: Số điện thoại
                    CustomTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            phoneNumberError = null
                            errorMessage = null
                        },
                        label = "Số điện thoại *",
                        placeholder = "0987 654 321",
                        leadingIconRes = R.drawable.phone,
                        errorMessage = phoneNumberError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 4: Địa chỉ Email
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = null
                            errorMessage = null
                        },
                        label = "Địa chỉ Email *",
                        placeholder = "email@address.com",
                        leadingIconRes = R.drawable.email,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        background = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 5: Mật khẩu
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("Mật khẩu ")
                                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                        append("*")
                                    }
                                },
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
                                passwordError = null
                                errorMessage = null
                            },
                            label = null,
                            placeholder = "••••••••••••",
                            leadingIconRes = R.drawable.lock,
                            isPassword = true,
                            errorMessage = passwordError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            background = Color.White
                        )

                        if (password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Thanh đo độ bảo mật mật khẩu & 4 tiêu chí
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
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 6: Xác nhận mật khẩu
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = null
                            errorMessage = null
                        },
                        label = "Xác nhận mật khẩu *",
                        placeholder = "••••••••••••",
                        leadingIconRes = R.drawable.shield_check,
                        isPassword = true,
                        errorMessage = confirmPasswordError,
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

        // Modal Bottom Sheet Chọn ngày sinh
        if (showDatePicker) {
            DatePickerBottomSheet(
                initialDay = selectedDay,
                initialMonth = selectedMonth,
                initialYear = selectedYear,
                onDismiss = { showDatePicker = false },
                onDateConfirm = { d, m, y ->
                    selectedDay = d
                    selectedMonth = m
                    selectedYear = y
                    birthDateStr = String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", d, m, y)
                    birthDateError = null
                    errorMessage = null
                    showDatePicker = false
                }
            )
        }
    }
}

/**
 * Modal Bottom Sheet chọn ngày sinh (Hiển thị đầy đủ skipPartiallyExpanded = true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerBottomSheet(
    initialDay: Int,
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onDateConfirm: (day: Int, month: Int, year: Int) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(initialDay) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }

    var isWheelView by remember { mutableStateOf(false) }
    var slideDirection by remember { mutableIntStateOf(1) }

    fun goToNextMonth() {
        slideDirection = 1
        if (selectedMonth < 12) {
            selectedMonth++
        } else {
            selectedMonth = 1
            selectedYear++
        }
    }

    fun goToPreviousMonth() {
        slideDirection = -1
        if (selectedMonth > 1) {
            selectedMonth--
        } else {
            selectedMonth = 12
            selectedYear--
        }
    }

    // Dùng sheetState với skipPartiallyExpanded = true để hiện MỞ RỘNG HOÀN TOÀN
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E0E0))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row: Icon + Tiêu đề "Chọn ngày sinh" + Nút Đóng
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.birthday),
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chọn ngày sinh",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBrownText,
                            fontSize = 16.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2F0EB))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-Header Row: <  Tháng X, YYYY ▼  > (Ẩn nút mũi tên khi ở màn hình cuộn)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (!isWheelView) Arrangement.SpaceBetween else Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isWheelView) {
                    IconButton(
                        onClick = { goToPreviousMonth() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Tháng trước",
                            tint = DarkBrownText
                        )
                    }
                }

                // Nút Pill chuyển đổi giữa Calendar View và Wheel View
                Surface(
                    onClick = { isWheelView = !isWheelView },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF5F3F0)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tháng $selectedMonth, $selectedYear",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBrownText,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isWheelView) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (!isWheelView) {
                    IconButton(
                        onClick = { goToNextMonth() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Tháng sau",
                            tint = DarkBrownText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị Lưới Lịch (vuốt ngang chuyển tháng) hoặc Giao diện Cuộn Wheel
            if (!isWheelView) {
                AnimatedContent(
                    targetState = Pair(selectedMonth, selectedYear),
                    transitionSpec = {
                        if (slideDirection > 0) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "calendarSlide"
                ) { (m, y) ->
                    CalendarGridView(
                        selectedDay = selectedDay,
                        selectedMonth = m,
                        selectedYear = y,
                        onDaySelected = { day -> selectedDay = day },
                        onNextMonth = { goToNextMonth() },
                        onPreviousMonth = { goToPreviousMonth() }
                    )
                }
            } else {
                WheelPickerView(
                    selectedDay = selectedDay,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onDateChanged = { d, m, y ->
                        selectedDay = d
                        selectedMonth = m
                        selectedYear = y
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hàng Nút Bấm: Hủy & Xác nhận
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nút Hủy
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hủy",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DarkBrownText,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Nút Xác nhận
                PrimaryButton(
                    leadingIcon = R.drawable.verify,
                    text = "Xác nhận",
                    containerColor = PrimaryOrange,
                    contentColor = Color.White,
                    shadowColor = PrimaryOrange,
                    onClick = {
                        onDateConfirm(selectedDay, selectedMonth, selectedYear)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * View Lịch Dạng Lưới (Hỗ trợ vuốt vuốt ngang chuyển tháng)
 */
@Composable
private fun CalendarGridView(
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    onDaySelected: (Int) -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    val weekDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val firstDayOffset = (dayOfWeek + 5) % 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragX < -40f) {
                            onNextMonth()
                        } else if (totalDragX > 40f) {
                            onPreviousMonth()
                        }
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C7263),
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val totalCells = firstDayOffset + maxDays
        val rows = (totalCells + 6) / 7

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (colIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + colIndex
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..maxDays) {
                            val isSelected = (dayNumber == selectedDay)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryOrange else Color.Transparent)
                                    .clickable { onDaySelected(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else DarkBrownText,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(38.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val prevOrNextDay = if (dayNumber <= 0) (28 + dayNumber + firstDayOffset) else (dayNumber - maxDays)
                                Text(
                                    text = if (prevOrNextDay in 1..31) prevOrNextDay.toString() else "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFC0B8B0),
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * View Cuộn Wheel Ngày/Tháng/Năm tự động Snap định vị vào giữa
 */
@Composable
private fun WheelPickerView(
    selectedDay: Int,
    selectedMonth: Int,
    selectedYear: Int,
    onDateChanged: (day: Int, month: Int, year: Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (1940..currentYear).toList()
    val months = (1..12).toList()

    val maxDaysInMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val days = (1..maxDaysInMonth).toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Thanh Highlight màu cam nhạt trải dài 3 cột như trong ảnh
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFFFEAD8))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelColumn(
                items = days.map { it.toString() },
                selectedItem = selectedDay.toString(),
                onItemSelected = { dayStr ->
                    onDateChanged(dayStr.toInt(), selectedMonth, selectedYear)
                },
                modifier = Modifier.weight(1f)
            )

            WheelColumn(
                items = months.map { "Tháng $it" },
                selectedItem = "Tháng $selectedMonth",
                onItemSelected = { monthStr ->
                    val m = monthStr.replace("Tháng ", "").toInt()
                    onDateChanged(selectedDay, m, selectedYear)
                },
                modifier = Modifier.weight(1.2f)
            )

            WheelColumn(
                items = years.map { it.toString() },
                selectedItem = selectedYear.toString(),
                onItemSelected = { yearStr ->
                    onDateChanged(selectedDay, selectedMonth, yearStr.toInt())
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)

    // Cuộn ban đầu đến item selected ở vị trí chính giữa
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            listState.scrollToItem(selectedIndex)
        }
    }

    // Xác định phần tử ở ĐÚNG TRUNG TÂM theo thời gian thực (Real-time frame-by-frame) khi cuộn
    val currentCenteredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }
            closest?.index ?: selectedIndex
        }
    }

    // Tự động định vị phần tử trung tâm khi người dùng dừng cuộn
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && currentCenteredIndex in items.indices) {
            if (items[currentCenteredIndex] != selectedItem) {
                onItemSelected(items[currentCenteredIndex])
            }
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapFlingBehavior,
        contentPadding = PaddingValues(vertical = 58.dp),
        modifier = modifier.height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(items) { index, item ->
            val isCentered = (index == currentCenteredIndex)

            // Đổi màu và kích thước chữ mượt mà theo thời gian thực khi di chuyển vào/ra khỏi box cam
            val textColor by animateColorAsState(
                targetValue = if (isCentered) PrimaryOrange else Color(0xFFB0A8A0),
                animationSpec = tween(durationMillis = 150),
                label = "textColor"
            )

            val textScale by animateFloatAsState(
                targetValue = if (isCentered) 1.1f else 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "textScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = textScale
                            scaleY = textScale
                        }
                        .clip(RoundedCornerShape(32.dp))
                        .clickable {
                            onItemSelected(item)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
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
                fontSize = 11.5.sp,
                fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SafePawsTheme {
        SignUpScreen()
    }
}
