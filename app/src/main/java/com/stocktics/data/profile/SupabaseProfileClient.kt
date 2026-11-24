package com.stocktics.data.profile

import android.content.Context
import android.util.Log
import com.stocktics.data.session.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SupabaseProfileClient {
    private const val TAG = "SupabaseProfileClient"
    private val SUPABASE_URL = com.stocktics.BuildConfig.SUPABASE_URL
    private val SUPABASE_KEY = com.stocktics.BuildConfig.SUPABASE_SECRET_KEY

    /**
     * Fetches user profile from Supabase and syncs the display name/nickname into local session.
     */
    suspend fun fetchUserProfile(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val userId = UserSessionManager.getLoggedInUserId(context)
            if (userId.isBlank()) return@withContext null

            val encUserId = URLEncoder.encode(userId, "UTF-8")
            val url = URL("$SUPABASE_URL/rest/v1/user_profiles?user_id=eq.$encUserId&select=display_name")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                connectTimeout = 4000
                readTimeout = 4000
            }

            if (conn.responseCode in 200..299) {
                val body = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    val displayName = obj.optString("display_name", "").trim()
                    if (displayName.isNotBlank() && !displayName.contains("@")) {
                        UserSessionManager.saveUserNickname(context, displayName)
                        return@withContext displayName
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile: ${e.message}")
        }
        return@withContext null
    }

    /**
     * Updates user nickname both locally in session cache and remotely in Supabase user_profiles.
     */
    suspend fun updateUserNickname(context: Context, newNickname: String): Boolean = withContext(Dispatchers.IO) {
        val cleanName = newNickname.trim()
        if (cleanName.isBlank()) return@withContext false

        // 1. Immediately update local storage so UI is instantaneous
        UserSessionManager.saveUserNickname(context, cleanName)

        val userId = UserSessionManager.getLoggedInUserId(context)
        if (userId.isBlank()) return@withContext true

        try {
            // 2. Patch existing row in Supabase
            val encUserId = URLEncoder.encode(userId, "UTF-8")
            val patchUrl = URL("$SUPABASE_URL/rest/v1/user_profiles?user_id=eq.$encUserId")
            val patchConn = (patchUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=representation")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val payload = JSONObject().apply {
                put("display_name", cleanName)
            }

            OutputStreamWriter(patchConn.outputStream, "UTF-8").use {
                it.write(payload.toString())
                it.flush()
            }

            val patchCode = patchConn.responseCode
            if (patchCode in 200..299) {
                val responseBody = BufferedReader(InputStreamReader(patchConn.inputStream, "UTF-8")).use { it.readText() }
                if (JSONArray(responseBody).length() > 0) {
                    Log.d(TAG, "Successfully updated display_name in Supabase: $cleanName")
                    return@withContext true
                }
            }

            // 3. If no rows affected (e.g. initial profile), perform Upsert (POST with resolution)
            val postUrl = URL("$SUPABASE_URL/rest/v1/user_profiles")
            val postConn = (postUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                setRequestProperty("User-Agent", "StockticsApp/1.0")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val upsertPayload = JSONObject().apply {
                put("user_id", userId)
                put("display_name", cleanName)
            }

            OutputStreamWriter(postConn.outputStream, "UTF-8").use {
                it.write(upsertPayload.toString())
                it.flush()
            }

            val postCode = postConn.responseCode
            Log.d(TAG, "Upsert profile response code: $postCode")
            return@withContext postCode in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user nickname in Supabase: ${e.message}")
            return@withContext false
        }
    }
}
