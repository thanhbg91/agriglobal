package com.example.api

import android.content.Context
import android.util.Log
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object BackendClient {
    private const val TAG = "BackendClient"
    private const val PREFS_NAME = "agriglobal_backend_prefs"
    private const val KEY_BASE_URL = "backend_base_url"
    private const val DEFAULT_URL = "http://10.0.2.2:5000" // Default loopback for Android Emulator

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    /**
     * Retrieves the stored Backend API base URL from SharedPreferences.
     */
    fun getStoredBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    /**
     * Saves the Backend API base URL into SharedPreferences.
     */
    fun saveBaseUrl(context: Context, url: String) {
        val formattedUrl = if (url.endsWith("/")) url.substring(0, url.length - 1) else url
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
    }

    /**
     * Tests connection to the Backend API.
     * Returns a Pair of (Success status, Server message).
     */
    suspend fun testConnection(baseUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/status"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val message = if (bodyStr.contains("databaseType")) {
                        // Simple parse of status endpoint response
                        val dbType = getJsonField(bodyStr, "databaseType")
                        "Đã kết nối! Loại DB: $dbType"
                    } else {
                        "Đã kết nối thành công!"
                    }
                    Pair(true, message)
                } else {
                    Pair(false, "Lỗi phản hồi từ máy chủ: ${response.code}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to backend: $url", e)
            Pair(false, "Không thể kết nối: ${e.localizedMessage ?: "Vui lòng kiểm tra địa chỉ URL và mạng."}")
        }
    }

    /**
     * Uploads a user agriculture product listing to the backend server database.
     */
    suspend fun uploadProduct(baseUrl: String, product: Product): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/products"
        
        // Manual JSON body or Moshi
        val productMap = mapOf(
            "id" to product.id,
            "name" to product.name,
            "category" to product.category,
            "priceUsd" to product.priceUsd,
            "availableQuantity" to product.availableQuantity,
            "unit" to product.unit,
            "origin" to product.origin,
            "description" to product.description,
            "imageUrl" to product.imageUrl,
            "sellerName" to product.sellerName,
            "sellerContact" to product.sellerContact,
            "isUserListing" to product.isUserListing,
            "timestamp" to product.timestamp
        )

        val jsonAdapter = moshi.adapter(Map::class.java)
        val jsonBody = jsonAdapter.toJson(productMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "Đồng bộ thành công sản phẩm: ${product.name}")
                } else {
                    val err = response.body?.string() ?: ""
                    Pair(false, "Lỗi ${response.code}: ${getJsonField(err, "error") ?: "Không thể đồng bộ."}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload product to backend: $url", e)
            Pair(false, "Lỗi đồng bộ sản phẩm: ${e.localizedMessage}")
        }
    }

    /**
     * Uploads a trade contract transaction (CartItem) to the backend server database.
     */
    suspend fun uploadOrder(baseUrl: String, item: CartItem): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/api/orders"

        val orderMap = mapOf(
            "id" to item.id,
            "productId" to item.productId,
            "productName" to item.productName,
            "quantity" to item.quantity,
            "priceUsd" to item.priceUsd,
            "unit" to item.unit,
            "imageUrl" to item.imageUrl,
            "sellerName" to item.sellerName,
            "status" to item.status,
            "trackingNumber" to (item.trackingNumber ?: ""),
            "shippingProvider" to (item.shippingProvider ?: ""),
            "shippingStatus" to item.shippingStatus,
            "shippingHistory" to (item.shippingHistory ?: ""),
            "timestamp" to item.timestamp
        )

        val jsonAdapter = moshi.adapter(Map::class.java)
        val jsonBody = jsonAdapter.toJson(orderMap)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "Đồng bộ giao dịch thành công cho sản phẩm: ${item.productName}")
                } else {
                    val err = response.body?.string() ?: ""
                    Pair(false, "Lỗi ${response.code}: ${getJsonField(err, "error") ?: "Không thể đồng bộ."}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload order to backend: $url", e)
            Pair(false, "Lỗi đồng bộ giao dịch: ${e.localizedMessage}")
        }
    }

    /**
     * Helper utility to extract simple field values from flat JSON responses without extra model boilerplate.
     */
    private fun getJsonField(json: String, fieldName: String): String? {
        return try {
            val key = "\"$fieldName\""
            val index = json.indexOf(key)
            if (index == -1) return null
            
            val start = json.indexOf(":", index)
            if (start == -1) return null
            
            val quoteStart = json.indexOf("\"", start)
            if (quoteStart == -1 || quoteStart > start + 5) {
                // Number or boolean
                val end = json.indexOfAny(charArrayOf(',', '}'), start)
                if (end != -1) {
                    return json.substring(start + 1, end).trim().replace("\"", "")
                }
                return null
            }
            
            val quoteEnd = json.indexOf("\"", quoteStart + 1)
            if (quoteEnd != -1) {
                return json.substring(quoteStart + 1, quoteEnd)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
