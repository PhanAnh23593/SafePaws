package com.hanoipetadoption.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hanoipetadoption.features.auth.ui.ChangePasswordScreen
import com.hanoipetadoption.features.auth.ui.ForgotPasswordScreen
import com.hanoipetadoption.features.auth.ui.LoginScreen
import com.hanoipetadoption.features.auth.ui.OtpVerificationScreen
import com.hanoipetadoption.features.auth.ui.ResetPasswordScreen
import com.hanoipetadoption.features.auth.ui.SignUpScreen
import com.hanoipetadoption.features.home.ui.HomeScreen
import com.hanoipetadoption.features.splash.ui.SplashScreen

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val OTP_VERIFICATION = "otp_verification"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val CHANGE_PASSWORD = "change_password"
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
                onNavigateToOtp = { email ->
                    navController.navigate("${NavRoutes.OTP_VERIFICATION}/$email")
                }
            )
        }

        // 3.1 Màn hình Xác thực OTP Đăng ký
        composable(
            route = "${NavRoutes.OTP_VERIFICATION}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpVerificationScreen(
                email = email,
                onBackClick = {
                    navController.popBackStack()
                },
                onVerificationSuccess = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
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

        // 5. Màn hình Tạo mật khẩu mới (Reset Password)
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

        // 5.1 Màn hình Đổi mật khẩu mới (Change Password)
        composable(NavRoutes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavRoutes.FORGOT_PASSWORD)
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
                },
                onChangePassword = {
                    navController.navigate(NavRoutes.CHANGE_PASSWORD)
                }
            )
        }
    }
}
