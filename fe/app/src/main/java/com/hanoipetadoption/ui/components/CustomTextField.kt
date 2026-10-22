package com.hanoipetadoption.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme

import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String?,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    @DrawableRes leadingIconRes: Int? = null,
    trailingIcon: ImageVector? = null,
    @DrawableRes trailingIconRes: Int? = null,
    trailingIconComposable: (@Composable () -> Unit)? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    background: Color? = null
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Label trên ô nhập liệu
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkBrownText
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (background != null) Modifier.background(background, RoundedCornerShape(32.dp))
                    else Modifier
                ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            leadingIcon = when {
                leadingIconRes != null -> {
                    {
                        Icon(
                            painter = painterResource(id = leadingIconRes),
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
                leadingIcon != null -> {
                    {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
                else -> null
            },
            trailingIcon = when {
                trailingIconComposable != null -> trailingIconComposable
                isPassword -> {
                    {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            val image = if (isPasswordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            }

                            Icon(
                                imageVector = image,
                                contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                tint = Color.Gray
                            )
                        }
                    }
                }
                trailingIconRes != null -> {
                    {
                        IconButton(
                            onClick = { onTrailingIconClick?.invoke() },
                            enabled = onTrailingIconClick != null
                        ) {
                            Icon(
                                painter = painterResource(id = trailingIconRes),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
                trailingIcon != null -> {
                    {
                        IconButton(
                            onClick = { onTrailingIconClick?.invoke() },
                            enabled = onTrailingIconClick != null
                        ) {
                            Icon(
                                imageVector = trailingIcon,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
                else -> null
            },
            shape = RoundedCornerShape(32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = background ?: Color.White,
                unfocusedContainerColor = background ?: Color.White,
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = PrimaryOrange,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = Color.Red
            )
        )

        // Hiển thị thông báo lỗi nếu có
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomTextFieldEmailPreview() {
    SafePawsTheme {
        var email by remember { mutableStateOf("hanoipetrescue@gmail.com") }

        Surface(
            color = NeutralBackground,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "Nhập email của bạn",
                    leadingIcon = Icons.Default.Email
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomTextFieldPasswordPreview() {
    SafePawsTheme {
        var password by remember { mutableStateOf("secret123") }

        Surface(
            color = NeutralBackground,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Mật khẩu",
                    placeholder = "Nhập mật khẩu",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    background = Color(0xfff5f3f0)
                )
            }
        }
    }
}
