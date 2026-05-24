package com.example.voca.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences("VocaSession", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun setLogin(isLoggedIn: Boolean) {
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun setUserId(id: Int) {
        editor.putInt("userId", id)
        editor.apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("userId", -1)
    }

    fun setUserName(name: String) {
        editor.putString("userName", name)
        editor.apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("userName", "")
    }

    fun setProfileStatus(status: String) {
        editor.putString("profileStatus", status)
        editor.apply()
    }

    fun getProfileStatus(): String? {
        return sharedPreferences.getString("profileStatus", "Anggota Baru")
    }

    fun setProfileImage(imageUri: String) {
        editor.putString("profileImage", imageUri)
        editor.apply()
    }

    fun getProfileImage(): String? {
        return sharedPreferences.getString("profileImage", "")
    }

    // Budget Target methods
    fun setBudgetTarget(amount: Float) {
        val userId = getUserId()
        editor.putFloat("budgetTarget_$userId", amount)
        editor.apply()
    }

    fun getBudgetTarget(): Float {
        val userId = getUserId()
        return sharedPreferences.getFloat("budgetTarget_$userId", 0f)
    }

    fun setBudgetType(type: String) {
        val userId = getUserId()
        editor.putString("budgetType_$userId", type)
        editor.apply()
    }

    fun getBudgetType(): String {
        val userId = getUserId()
        return sharedPreferences.getString("budgetType_$userId", "Bulanan") ?: "Bulanan"
    }

    // New Preferences
    fun setCurrency(currency: String) {
        editor.putString("currency", currency)
        editor.apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString("currency", "IDR") ?: "IDR"
    }

    fun setLanguage(language: String) {
        editor.putString("language", language)
        editor.apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("language", "in") ?: "in"
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("userEmail", "")
    }

    fun setUserEmail(email: String) {
        editor.putString("userEmail", email)
        editor.apply()
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}