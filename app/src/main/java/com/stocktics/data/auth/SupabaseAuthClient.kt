package com.stocktics.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val email: String, val userId: String, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object SupabaseAuthClient {
    private val SUPABASE_URL = com.stocktics.BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = com.stocktics.BuildConfig.SUPABASE_PUBLISHABLE_KEY

    suspend fun signUp(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$SUPABASE_URL/auth/v1/signup")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val user = json.optJSONObject("user") ?: json
                val userId = user.optString("id", "")
                val userEmail = user.optString("email", email)
                val token = json.optString("access_token", "")
                AuthResult.Success(email = userEmail, userId = userId, token = token)
            } else {
                val errorMsg = parseErrorMessage(responseBody)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Network error connecting to Supabase")
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$SUPABASE_URL/auth/v1/token?grant_type=password")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseBody = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(responseBody)
                val user = json.optJSONObject("user")
                val userId = user?.optString("id", "") ?: ""
                val userEmail = user?.optString("email", email) ?: email
                val token = json.optString("access_token", "")
                AuthResult.Success(email = userEmail, userId = userId, token = token)
            } else {
                val errorMsg = parseErrorMessage(responseBody)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Network error connecting to Supabase")
        }
    }

    private fun parseErrorMessage(response: String): String {
        return try {
            val json = JSONObject(response)
            json.optString("msg", json.optString("error_description", json.optString("message", "Authentication failed.")))
        } catch (e: Exception) {
            "Authentication failed: $response"
        }
    }
}
