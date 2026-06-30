package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Data classes for request and response
    data class TextPart(val text: String)
    data class Content(val parts: List<TextPart>)
    data class GenerateRequest(val contents: List<Content>)

    /**
     * Calls Gemini API to generate content from a given prompt.
     */
    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "LỖI: Chưa cấu hình khóa API Gemini trong mục Secrets."
        }

        val requestObj = GenerateRequest(
            contents = listOf(Content(parts = listOf(TextPart(prompt))))
        )

        val adapter = moshi.adapter(GenerateRequest::class.java)
        val jsonRequest = adapter.toJson(requestObj)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed code: ${response.code}, body: $errBody")
                    return@withContext "Lỗi API (${response.code}): Hãy đảm bảo rằng API Key hợp lệ."
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext "Không nhận được phản hồi từ máy chủ."
                }

                return@withContext parseResponseText(responseBody)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error calling Gemini", e)
            return@withContext "Lỗi kết nối: ${e.localizedMessage ?: "Vui lòng kiểm tra mạng."}"
        }
    }

    /**
     * Quick manual parser to extract the text content from the Gemini JSON response
     * to avoid complicated nested adapter boilerplate.
     */
    private fun parseResponseText(json: String): String {
        return try {
            val key = "\"text\""
            var index = json.indexOf(key)
            if (index == -1) return "Không thể phân tích phản hồi AI."
            
            val builder = StringBuilder()
            while (index != -1) {
                val start = json.indexOf(":", index)
                if (start != -1) {
                    var quoteStart = json.indexOf("\"", start)
                    while (quoteStart != -1 && quoteStart < json.length) {
                        // Check if it's the correct start of value and not escaped
                        if (json[quoteStart - 1] != '\\') {
                            break
                        }
                        quoteStart = json.indexOf("\"", quoteStart + 1)
                    }
                    
                    if (quoteStart != -1) {
                        var quoteEnd = quoteStart + 1
                        while (quoteEnd < json.length) {
                            if (json[quoteEnd] == '\"' && json[quoteEnd - 1] != '\\') {
                                break
                            }
                            quoteEnd++
                        }
                        if (quoteEnd < json.length) {
                            val rawText = json.substring(quoteStart + 1, quoteEnd)
                            // Unescape basic json characters like \n, \t, \"
                            val unescaped = rawText
                                .replace("\\n", "\n")
                                .replace("\\t", "\t")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                            builder.append(unescaped)
                            builder.append("\n")
                        }
                    }
                }
                index = json.indexOf(key, index + key.length)
            }
            val result = builder.toString().trim()
            if (result.isNotEmpty()) result else "Hết nội dung phản hồi."
        } catch (e: Exception) {
            Log.e(TAG, "Parsing error", e)
            "Lỗi phân tích phản hồi AI."
        }
    }

    // High level helper: Generate product description
    suspend fun enhanceProductDescription(
        name: String,
        category: String,
        origin: String,
        price: Double,
        unit: String,
        basicDesc: String
    ): String {
        val prompt = """
            Bạn là một chuyên gia viết nội dung quảng bá nông sản xuất khẩu quốc tế.
            Hãy viết một mô tả sản phẩm hấp dẫn, chuyên nghiệp và đầy đủ thông tin dựa trên dữ liệu sau:
            - Tên sản phẩm: $name
            - Danh mục: $category
            - Xuất xứ: $origin
            - Giá dự kiến: $price USD trên mỗi $unit
            - Mô tả thô sơ: $basicDesc

            Yêu cầu bài viết mô tả sản phẩm:
            1. Ngôn ngữ: Tiếng Việt, ngắn gọn trong khoảng 150-200 từ.
            2. Có cấu trúc rõ ràng: Giới thiệu chung, Đặc điểm nổi bật (mùi vị, chất lượng, quy chuẩn), và Tiêu chuẩn Xuất khẩu / Đóng gói.
            3. Hãy làm nổi bật lợi thế xuất khẩu của xuất xứ $origin.
            Chỉ trả về văn bản mô tả trực tiếp, không có lời dẫn giới thiệu ngoài lề của bạn.
        """.trimIndent()
        return generateContent(prompt)
    }

    // High level helper: Translate description
    suspend fun translateDescription(description: String, targetLang: String): String {
        val prompt = """
            Hãy dịch đoạn mô tả sản phẩm nông sản sau đây sang ngôn ngữ: $targetLang.
            Nội dung cần dịch:
            "$description"

            Yêu cầu:
            - Giữ nguyên các thuật ngữ kỹ thuật thương mại nông nghiệp (ví dụ: kích cỡ sàng, độ ẩm, tiêu chuẩn xuất khẩu).
            - Bản dịch mượt mà, tự nhiên, thích hợp với phong cách thương mại điện tử chuyên nghiệp toàn cầu.
            - Chỉ trả về bản dịch trực tiếp, không thêm lời dẫn của bạn.
        """.trimIndent()
        return generateContent(prompt)
    }

    // High level helper: Generate Customs & Trade Consulting Response
    suspend fun getConsultingResponse(history: List<Pair<String, Boolean>>, question: String): String {
        val historyStr = history.joinToString("\n") { (text, isUser) ->
            if (isUser) "Người dùng: $text" else "Cố vấn AgriGlobal: $text"
        }
        val prompt = """
            Bạn là AgriGlobal AI Advisor, một cố vấn thương mại, thủ tục hải quan và logistics chuyên về nông sản toàn cầu.
            Nhiệm vụ của bạn là hỗ trợ các nông dân, hợp tác xã và doanh nghiệp xuất nhập khẩu nông sản vượt qua các rào cản kỹ thuật để giao thương quốc tế.
            
            Kiến thức của bạn bao gồm:
            - Các tiêu chuẩn chất lượng (VietGAP, GlobalGAP, USDA Organic, EU Organic).
            - Chứng nhận kiểm dịch thực vật (Phytosanitary Certificate), nguồn gốc xuất xứ (C/O).
            - Các điều kiện thương mại quốc tế Incoterms (FOB, CIF, CFR, FCA).
            - Thủ tục thông quan nông sản tại các thị trường khó tính như Mỹ, EU, Nhật Bản, Trung Quốc.
            - Xu hướng giá cả thị trường nông sản thế giới.

            Lịch sử cuộc hội thoại:
            $historyStr
            
            Câu hỏi mới từ người dùng:
            "$question"

            Hãy trả lời bằng Tiếng Việt, súc tích, chuyên nghiệp, cấu trúc rõ ràng bằng bullet-points nếu cần. Nếu câu hỏi không liên quan đến thương mại nông nghiệp, hải quan hoặc logistics nông sản, hãy nhắc nhở người dùng một cách lịch sự rằng bạn chuyên hỗ trợ về lĩnh vực này.
        """.trimIndent()
        return generateContent(prompt)
    }

    /**
     * Simulates fetching live tracking updates from the logistics carrier using Gemini.
     */
    suspend fun fetchCarrierTrackingUpdate(
        productName: String,
        origin: String,
        carrier: String,
        trackingNumber: String,
        currentStatus: String,
        existingHistory: String?
    ): String {
        val prompt = """
            Bạn đóng vai trò là Cổng API tích hợp tự động của Đơn vị vận chuyển quốc tế '$carrier' (Hệ thống tracking vận đơn số '$trackingNumber').
            Hãy mô phỏng việc kiểm tra hành trình thực tế của lô hàng nông sản sau:
            - Tên sản phẩm nông sản: $productName
            - Nguồn gốc: $origin
            - Đơn vị vận chuyển: $carrier
            - Mã vận đơn: $trackingNumber
            - Trạng thái hiện tại: $currentStatus
            - Nhật ký hành trình cũ: ${existingHistory ?: "Chưa có hành trình"}

            Hãy đưa ra bước cập nhật tiếp theo cho hành trình của lô hàng nông sản này một cách chân thực nhất dựa theo quy chuẩn logistics quốc tế (Ví dụ: bốc xếp, kiểm dịch thực vật tại cảng đi, thông quan hải quan xuất khẩu, vận chuyển trên biển/hàng không, thông quan nhập khẩu tại nước đến, giao hàng nội địa).

            Định dạng câu trả lời của bạn phải là JSON duy nhất với cấu trúc chính xác sau:
            {
              "status": "MỘT_TRONG_CÁC_TRẠNG_THÁI_SAU: Đang chuẩn bị hàng, Đang vận chuyển, Đang kiểm dịch/thông quan, Đã giao hàng",
              "newEvent": "Thời gian thực tế (Định dạng YYYY-MM-DD HH:MM) - Sự kiện logistics chân thực (Ví dụ: [Cảng Cát Lái] Kiểm dịch thực vật Phytosanitary thành công, bốc dỡ container lên tàu Maersk.)"
            }

            Chú ý quan trọng:
            1. Trạng thái 'status' phải là một trong bốn chuỗi tiếng Việt chuẩn xác trên: "Đang chuẩn bị hàng", "Đang vận chuyển", "Đang kiểm dịch/thông quan", "Đã giao hàng". Không được chế từ khác.
            2. Nếu trạng thái trước đó đã là 'Đã giao hàng', hãy giữ nguyên trạng thái và thêm một dòng thông báo lô hàng đã được ký nhận thành công.
            3. Trả về đúng định dạng JSON trực tiếp, không có nhãn markdown ```json ... ```, không có văn bản giải thích.
        """.trimIndent()
        return generateContent(prompt)
    }

    /**
     * Generates a detailed compliance and regulations analysis for a given agricultural product and destination market.
     */
    suspend fun getRegulationsChecklistAnalysis(product: String, destination: String): String {
        val prompt = """
            Bạn là AgriGlobal Trade Regulations Expert, chuyên gia hàng đầu về thủ tục xuất nhập khẩu, kiểm dịch thực vật và rào cản kỹ thuật thương mại (SPS/TBT) đối với nông sản xuất khẩu từ Việt Nam.
            
            Hãy cung cấp một phân tích chuyên sâu về quy định nhập khẩu và hướng dẫn tuân thủ cho nông sản sau:
            - Sản phẩm nông sản xuất khẩu: $product
            - Thị trường nhập khẩu mục tiêu (Nước đến): $destination
            - Quốc gia xuất khẩu (Nguồn gốc): Việt Nam
            
            Yêu cầu bài viết tư vấn chi tiết và chuyên nghiệp bằng Tiếng Việt với cấu trúc chuẩn sau:
            
            1. 📊 THUẾ QUAN & Hạn ngạch (Tariffs & Quota):
               - Nêu cụ thể mức thuế suất thông thường và mức thuế suất ưu đãi (nếu có các Hiệp định thương mại tự do như EVFTA, RCEP, CPTPP, ACFTA).
               - Quy định về hạn ngạch thuế quan nếu áp dụng.
               
            2. 🔬 RÀO CẢN KỸ THUẬT & KIỂM DỊCH (Phytosanitary & SPS Requirements):
               - Các sinh vật gây hại thuộc đối tượng kiểm dịch thực vật mà nước nhập khẩu quan tâm đặc biệt.
               - Tiêu chuẩn dư lượng thuốc bảo vệ thực vật (MRL) tối đa được phép.
               - Các yêu cầu bắt buộc về xử lý trước khi xuất khẩu (ví dụ: chiếu xạ, xử lý hơi nước nóng VHT, xông trùng methyl bromide).
               
            3. 📋 Yêu cầu Đóng gói, Nhãn mác (Packaging & Labeling):
               - Quy cách bao bì, mã số cơ sở đóng gói.
               - Yêu cầu thông tin nhãn mác (Ngôn ngữ nhãn, thông tin truy xuất nguồn gốc bắt buộc).
               
            4. ⚠️ Đánh giá rủi ro & Khuyến nghị (Risk Assessment & Advice):
               - Các rủi ro thường gặp khiến lô hàng bị trả về hoặc tiêu hủy (ví dụ: phát hiện nấm mốc, rệp sáp, hồ sơ sai sót).
               - Lời khuyên thiết thực dành cho doanh nghiệp để thông quan thuận lợi nhất.

            Hãy trình bày thật đẹp, rõ ràng, cấu trúc mạch lạc bằng Markdown, sử dụng bullet points sắc sảo, font chữ đậm nhạt phân biệt để dễ đọc trên màn hình điện thoại di động.
        """.trimIndent()
        return generateContent(prompt)
    }
}
