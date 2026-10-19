package com.hanoipetadoption.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.ui.theme.PrimaryOrange

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes leadingIcon: Int? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = PrimaryOrange,
    contentColor: Color = Color.White,
    shadowColor: Color = PrimaryOrange,
    elevation: Dp = 10.dp,
    style: TextStyle = TextStyle.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Hiệu ứng bấm nảy (active:scale-[0.98]) giống Tailwind active:scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val isButtonActive = enabled && !isLoading

    // Tính toán độ mờ của Shadow theo trạng thái nút
    val shadowAlpha = when {
        shadowColor == Color.Transparent -> 0f
        isButtonActive -> 0.35f
        !enabled && containerColor == PrimaryOrange -> 0.20f
        else -> 0f
    }

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .coloredShadow(
                color = shadowColor,
                alpha = shadowAlpha,
                borderRadius = 50.dp,
                blurRadius = elevation * 1.4f,
                offsetY = 6.dp,
                enabled = shadowAlpha > 0f
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = isButtonActive,
        shape = RoundedCornerShape(50.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        painter = painterResource(id = leadingIcon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = style,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Extension vẽ Shadow đổ bóng màu Cam / Custom Color mượt mà chuẩn Tailwind / Figma
 */
fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.35f,
    borderRadius: Dp = 50.dp,
    blurRadius: Dp = 14.dp,
    offsetY: Dp = 6.dp,
    enabled: Boolean = true
): Modifier = if (enabled && alpha > 0f) {
    this.drawBehind {
        val shadowColor = color.copy(alpha = alpha).toArgb()
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blurRadius.toPx(),
                    0f,
                    offsetY.toPx(),
                    shadowColor
                )
            }
            canvas.nativeCanvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
        }
    }
} else this
