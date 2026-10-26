package com.hanoipetadoption.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.DarkGreen
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme

@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    otpLength: Int = 6,
    isError: Boolean = false,
    isSuccess: Boolean = false
) {
    BasicTextField(
        value = otpText,
        onValueChange = { if (it.length <= otpLength) onOtpTextChange(it) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        cursorBrush = SolidColor(PrimaryOrange),
        decorationBox = {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(otpLength) { index ->
                    val isFocused = index == otpText.length

                    val boxBackground = when {
                        isSuccess -> Color(0xFF5DFF95).copy(alpha = 0.3f)
                        isError -> Color(0xFFFF655B).copy(alpha = 0.3f)
                        isFocused -> Color(0xFFFFF8F4)
                        else -> Color(0xFFF2F0EB)
                    }

                    val textColor = when {
                        isSuccess -> DarkGreen
                        isError -> Color(0xFFBA1A1A)
                        else -> DarkBrownText
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(boxBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            index < otpText.length -> {
                                Text(
                                    text = otpText[index].toString(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 22.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                            index == otpText.length -> {
                                // Con trỏ nhập liệu
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height(20.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryOrange)
                                )
                            }
                            else -> {
                                // Chấm tròn xám nhạt ở giữa ô khi chưa có số
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isError) Color(0xFFFF655B)
                                            else if (isSuccess) Color(0xFF5DFF95)
                                            else Color(0xFFD3D3D3)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun OtpInputFieldPreview() {
    SafePawsTheme {
        Surface(
            color = NeutralBackground,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OtpInputField(
                    otpText = "123",
                    onOtpTextChange = {}
                )
                OtpInputField(
                    otpText = "123456",
                    onOtpTextChange = {},
                    isSuccess = true
                )
                OtpInputField(
                    otpText = "123456",
                    onOtpTextChange = {},
                    isError = true
                )
            }
        }
    }
}
