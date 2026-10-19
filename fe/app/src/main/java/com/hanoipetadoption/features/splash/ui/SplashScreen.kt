package com.hanoipetadoption.features.splash.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.core.utils.UserPreferences
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.PrimaryOrange
import com.hanoipetadoption.ui.theme.SafePawsTheme
import com.hanoipetadoption.ui.theme.PlusJakartaSans
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current

    // Kiểm tra trạng thái đăng nhập sau 2 giây
    LaunchedEffect(Unit) {
        delay(2000L)
        val isLoggedIn = UserPreferences.isLoggedIn(context)
        if (isLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Warm Glow Halo ở giữa màn hình
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFDBC8).copy(alpha = 0.55f),
                            Color(0xFFFFEAD8).copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Nội dung chính
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Card Logo màu Trắng với Shadow mềm
            Surface(
                modifier = Modifier.size(128.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    HousePawLogo(
                        modifier = Modifier.size(76.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tiêu đề thương hiệu HANOI PET ADOPTION
            Text(
                text = "HANOI PET ADOPTION",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.8.sp,
                    color = DarkBrownText
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Slogan / Tagline
            Text(
                text = "Vì mỗi thú cưng đều xứng đáng có một mái ấm",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF584235)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Indicator 3 dấu chấm chuyển động mềm mại
            PulsingDotsIndicator()

            Spacer(modifier = Modifier.weight(1f))

            // Website Domain ở chân trang
            Text(
                text = "hanoipetadoption.com",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Color(0xFFA09080)
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * Vẽ Logo Ngôi nhà + Chân chó (Gradient Vàng - Cam)
 */
@Composable
private fun HousePawLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Đường vẽ Ngôi nhà
        val housePath = Path().apply {
            moveTo(width * 0.5f, height * 0.12f)
            lineTo(width * 0.88f, height * 0.40f)
            lineTo(width * 0.80f, height * 0.40f)
            lineTo(width * 0.80f, height * 0.26f)
            lineTo(width * 0.72f, height * 0.26f)
            lineTo(width * 0.72f, height * 0.35f)
            lineTo(width * 0.82f, height * 0.44f)
            lineTo(width * 0.82f, height * 0.82f)
            cubicTo(width * 0.82f, height * 0.87f, width * 0.77f, height * 0.90f, width * 0.72f, height * 0.90f)
            lineTo(width * 0.28f, height * 0.90f)
            cubicTo(width * 0.23f, height * 0.90f, width * 0.18f, height * 0.87f, width * 0.18f, height * 0.82f)
            lineTo(width * 0.18f, height * 0.44f)
            lineTo(width * 0.12f, height * 0.40f)
            close()
        }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFCC00), Color(0xFFFF8A00))
        )
        drawPath(path = housePath, brush = gradientBrush)

        // Lòng bàn chân chó trung tâm (Trắng)
        val padRadius = width * 0.11f
        val padCenter = Offset(width * 0.5f, height * 0.65f)
        drawCircle(
            color = Color.White,
            radius = padRadius,
            center = padCenter
        )

        // 4 ngón chân chó (Trắng)
        val toeRadius = width * 0.042f
        val toes = listOf(
            Offset(width * 0.37f, height * 0.48f),
            Offset(width * 0.45f, height * 0.43f),
            Offset(width * 0.55f, height * 0.43f),
            Offset(width * 0.63f, height * 0.48f)
        )
        toes.forEach { toeCenter ->
            drawCircle(
                color = Color.White,
                radius = toeRadius,
                center = toeCenter
            )
        }
    }
}

/**
 * Hiệu ứng 3 chấm nảy nhịp nhàng
 */
@Composable
private fun PulsingDotsIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsTransition")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange.copy(alpha = alpha1))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange.copy(alpha = alpha2))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryOrange.copy(alpha = alpha3))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SafePawsTheme {
        SplashScreen(
            onNavigateToLogin = {},
            onNavigateToHome = {}
        )
    }
}
