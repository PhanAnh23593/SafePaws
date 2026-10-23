package com.hanoipetadoption.core.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý lưu trữ trạng thái đăng nhập người dùng
 */
object UserPreferences {
    private const val PREF_NAME = "hpa_user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Kiểm tra người dùng đã đăng nhập hay chưa
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Cập nhật trạng thái đăng nhập
     */
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
}
