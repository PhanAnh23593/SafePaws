package com.hanoipetadoption.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanoipetadoption.core.utils.UserPreferences
import com.hanoipetadoption.ui.components.PrimaryButton
import com.hanoipetadoption.ui.theme.DarkBrownText
import com.hanoipetadoption.ui.theme.NeutralBackground
import com.hanoipetadoption.ui.theme.SafePawsTheme

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏠 Màn hình chính HPA",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkBrownText,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Chào mừng bạn đã đăng nhập thành công vào Hanoi Pet Adoption! 🐾",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DarkBrownText,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(36.dp))

        PrimaryButton(
            text = "Đăng xuất",
            onClick = {
                UserPreferences.setLoggedIn(context, false)
                onLogout()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SafePawsTheme {
        HomeScreen()
    }
}
