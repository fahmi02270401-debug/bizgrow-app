package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateBusinessAnalysis(
        statsSummary: String,
        promptUser: String = "Lakukan analisis keuangan, stok, dan performa bisnis ini. Berikan rekomendasi konkret dan taktis untuk meningkatkan profitabilitas."
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder!")
            return@withContext "Konfigurasi Kunci API Gemini tidak ditemukan. Harap masukkan API Key Anda di panel rahasia AI Studio agar fitur Asisten Bisnis AI dapat berfungsi dengan baik."
        }

        // Construct payload manually for speed and maximum resilience
        val systemInstruction = "Anda adalah Asisten AI ERP & Analis Keuangan Bisnis SaaS profesional. Selalu tanggap dan analisis data yang diberikan dengan cermat. Berikan masukan taktis, ramah, logis, terperinci, dan gunakan Bahasa Indonesia yang profesional namun mudah dipahami oleh pemilik bisnis UMKM."
        
        val payload = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Berikut adalah ringkasan data performa bisnis saat ini:\n$statsSummary\n\nInstruksi Tambahan dari Pengguna: $promptUser"
                    }
                  ]
                }
              ],
              "systemInstruction": {
                "parts": [
                  {
                    "text": "$systemInstruction"
                  }
                ]
              },
              "generationConfig": {
                "temperature": 0.7
              }
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = payload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "API error: ${response.code} - $bodyString")
                    return@withContext "Gagal terhubung dengan server AI Gemini (Kode: ${response.code}). Pastikan API Key yang Anda masukkan valid."
                }

                if (bodyString.isNullOrEmpty()) {
                    return@withContext "Respon dari AI Gemini kosong."
                }

                // Extract response text using a simple manual JSON path to avoid complex adapter issues
                val text = extractTextFromResponse(bodyString)
                if (text.isNotEmpty()) {
                    text
                } else {
                    "Tidak dapat mengekstrak teks analisis dari respon AI."
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating content", e)
            "Terjadi kesalahan saat menghubungi Asisten AI: ${e.localizedMessage}. Silakan periksa koneksi internet Anda."
        }
    }

    private fun extractTextFromResponse(json: String): String {
        try {
            // Locate the "text" block inside candidates[0].content.parts[0].text
            val candidatesIndex = json.indexOf("\"candidates\"")
            if (candidatesIndex == -1) return ""
            
            val textIndex = json.indexOf("\"text\"", candidatesIndex)
            if (textIndex == -1) return ""
            
            val colonIndex = json.indexOf(":", textIndex)
            if (colonIndex == -1) return ""
            
            val startQuote = json.indexOf("\"", colonIndex)
            if (startQuote == -1) return ""
            
            // Loop and capture string, handling escaped quotes
            val result = StringBuilder()
            var i = startQuote + 1
            while (i < json.length) {
                val char = json[i]
                if (char == '\"') {
                    // Check if previous char is backslash (escaped quote)
                    if (json[i - 1] == '\\') {
                        result.append(char)
                    } else {
                        break // End of string
                    }
                } else if (char == '\\') {
                    // Handle escape characters (like \n)
                    if (i + 1 < json.length) {
                        val nextChar = json[i + 1]
                        when (nextChar) {
                            'n' -> result.append("\n")
                            't' -> result.append("\t")
                            '\"' -> result.append("\"")
                            '\\' -> result.append("\\")
                            else -> result.append(nextChar)
                        }
                        i++
                    }
                } else {
                    result.append(char)
                }
                i++
            }
            return result.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed manual JSON extraction", e)
            return ""
        }
    }
}
