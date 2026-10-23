package com.hanoipetadoption.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hanoipetadoption.features.auth.ui.ForgotPasswordScreen
import com.hanoipetadoption.features.auth.ui.LoginScreen
import com.hanoipetadoption.features.auth.ui.ResetPasswordScreen
import com.hanoipetadoption.features.auth.ui.SignUpScreen
import com.hanoipetadoption.features.home.ui.HomeScreen
import com.hanoipetadoption.features.splash.ui.SplashScreen

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val HOME = "home"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        // 1. Màn hình Splash Screen
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Màn hình Đăng nhập
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
                },
                onNavigateToSignUp = {
                    navController.navigate(NavRoutes.SIGN_UP)
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Màn hình Đăng ký tài khoản
        composable(NavRoutes.SIGN_UP) {
            SignUpScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToOtp = { _ ->
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
                }
            )
        }

        // 4. Màn hình Quên mật khẩu
        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToResetPassword = {
                    navController.navigate(NavRoutes.RESET_PASSWORD)
                }
            )
        }

        // 5. Màn hình Tạo mật khẩu mới
        composable(NavRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 6. Màn hình Trang chủ
        composable(NavRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
