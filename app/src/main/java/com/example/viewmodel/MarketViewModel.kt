package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    // All available listings
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Favorites
    val favorites: StateFlow<List<Product>> = repository.favoriteProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User listings
    val userListings: StateFlow<List<Product>> = repository.userListings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cart items
    val cartItems: StateFlow<List<CartItem>> = repository.allCartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Global Currency state: VND, USD, EUR, JPY
    private val _selectedCurrency = MutableStateFlow("VND")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    // Loading states for AI shipping updates by transaction id
    private val _isShippingLoading = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val isShippingLoading: StateFlow<Map<Int, Boolean>> = _isShippingLoading.asStateFlow()

    // Exchange Rates (Base 1 USD)
    val exchangeRates = mapOf(
        "USD" to 1.0,
        "VND" to 25400.0,
        "EUR" to 0.92,
        "JPY" to 158.0
    )

    // Currency symbols
    val currencySymbols = mapOf(
        "USD" to "$",
        "VND" to "₫",
        "EUR" to "€",
        "JPY" to "¥"
    )

    // --- Cloud Backend API Sync ---
    private val _backendBaseUrl = MutableStateFlow("")
    val backendBaseUrl: StateFlow<String> = _backendBaseUrl.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Chưa kiểm tra")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _isCheckingConnection = MutableStateFlow(false)
    val isCheckingConnection: StateFlow<Boolean> = _isCheckingConnection.asStateFlow()

    init {
        // Run database pre-population
        viewModelScope.launch {
            repository.prepopulateDatabaseIfNeeded()
        }
        // Load stored URL
        _backendBaseUrl.value = com.example.api.BackendClient.getStoredBaseUrl(application)
    }

    fun updateBackendUrl(url: String) {
        _backendBaseUrl.value = url
        com.example.api.BackendClient.saveBaseUrl(getApplication(), url)
        _connectionStatus.value = "Chưa kiểm tra"
    }

    fun checkBackendConnection() {
        viewModelScope.launch {
            _isCheckingConnection.value = true
            _connectionStatus.value = "Đang kết nối..."
            val result = com.example.api.BackendClient.testConnection(_backendBaseUrl.value)
            _connectionStatus.value = result.second
            _isCheckingConnection.value = false
        }
    }

    fun syncAllToCloudBackend() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "Bắt đầu đồng bộ..."
            
            val url = _backendBaseUrl.value
            val currentListings = userListings.value
            val currentOrders = cartItems.value
            
            var successCount = 0
            var failCount = 0

            // 1. Sync User Listings (Products)
            currentListings.forEach { product ->
                _syncStatus.value = "Đang đồng bộ sản phẩm: ${product.name}..."
                val res = com.example.api.BackendClient.uploadProduct(url, product)
                if (res.first) successCount++ else failCount++
            }

            // 2. Sync Trade Contracts (CartItems)
            currentOrders.forEach { item ->
                _syncStatus.value = "Đang đồng bộ giao dịch: ${item.productName}..."
                val res = com.example.api.BackendClient.uploadOrder(url, item)
                if (res.first) successCount++ else failCount++
            }

            _isSyncing.value = false
            if (failCount == 0) {
                _syncStatus.value = "Đồng bộ đám mây hoàn tất thành công! Tổng cộng: $successCount bản ghi."
            } else {
                _syncStatus.value = "Đồng bộ hoàn tất với lỗi. Thành công: $successCount, Thất bại: $failCount."
            }
        }
    }

    fun clearSyncStatus() {
        _syncStatus.value = null
    }

    fun selectCurrency(currency: String) {
        if (exchangeRates.containsKey(currency)) {
            _selectedCurrency.value = currency
        }
    }

    /**
     * Converts a USD price to the current active currency and formats with the currency symbol.
     */
    fun formatPrice(priceUsd: Double): String {
        val currency = _selectedCurrency.value
        val rate = exchangeRates[currency] ?: 1.0
        val converted = priceUsd * rate
        val symbol = currencySymbols[currency] ?: ""

        return when (currency) {
            "VND" -> {
                // Round VND to thousandths or hundreds
                val formatted = String.format("%,.0f", converted)
                "$formatted $symbol"
            }
            "JPY" -> {
                val formatted = String.format("%,.0f", converted)
                "$symbol$formatted"
            }
            else -> {
                val formatted = String.format("%,.2f", converted)
                "$symbol$formatted"
            }
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }

    // --- Product detail translation state ---
    private val _translationResult = MutableStateFlow<String?>(null)
    val translationResult: StateFlow<String?> = _translationResult.asStateFlow()

    private val _isTranslationLoading = MutableStateFlow(false)
    val isTranslationLoading: StateFlow<Boolean> = _isTranslationLoading.asStateFlow()

    fun translateProductDescription(description: String, targetLang: String) {
        _isTranslationLoading.value = true
        _translationResult.value = null
        viewModelScope.launch {
            val result = GeminiClient.translateDescription(description, targetLang)
            _translationResult.value = result
            _isTranslationLoading.value = false
        }
    }

    fun clearTranslation() {
        _translationResult.value = null
    }

    // --- Add Product AI description state ---
    private val _aiDescription = MutableStateFlow("")
    val aiDescription: StateFlow<String> = _aiDescription.asStateFlow()

    private val _isDescriptionEnhancing = MutableStateFlow(false)
    val isDescriptionEnhancing: StateFlow<Boolean> = _isDescriptionEnhancing.asStateFlow()

    fun enhanceDescription(name: String, category: String, origin: String, price: Double, unit: String, basic: String) {
        _isDescriptionEnhancing.value = true
        viewModelScope.launch {
            val result = GeminiClient.enhanceProductDescription(name, category, origin, price, unit, basic)
            _aiDescription.value = result
            _isDescriptionEnhancing.value = false
        }
    }

    fun clearAiDescription() {
        _aiDescription.value = ""
    }

    fun createListing(
        name: String,
        category: String,
        priceUsd: Double,
        quantity: Double,
        unit: String,
        origin: String,
        description: String,
        sellerName: String,
        sellerContact: String
    ) {
        viewModelScope.launch {
            // Pick a beautiful fallback unsplash image for the custom category
            val image = when (category) {
                "Trái cây" -> "https://images.unsplash.com/photo-1619546813926-a78fa6372cd2?w=600&auto=format&fit=crop&q=60"
                "Ngũ cốc" -> "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600&auto=format&fit=crop&q=60"
                "Rau củ" -> "https://images.unsplash.com/photo-1597362925123-77861d3fbac7?w=600&auto=format&fit=crop&q=60"
                "Cà phê & Ca cao" -> "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop&q=60"
                else -> "https://images.unsplash.com/photo-1464226184884-fa280b87c3a9?w=600&auto=format&fit=crop&q=60" // Farms/Agriculture
            }

            val product = Product(
                name = name,
                category = category,
                priceUsd = priceUsd,
                availableQuantity = quantity,
                unit = unit,
                origin = origin,
                description = description,
                imageUrl = image,
                sellerName = sellerName,
                sellerContact = sellerContact,
                isUserListing = true
            )
            repository.insertProduct(product)
        }
    }

    // --- Order / Cart Drafting ---
    fun addProductToCart(product: Product, orderQuantity: Double) {
        viewModelScope.launch {
            val item = CartItem(
                productId = product.id,
                productName = product.name,
                quantity = orderQuantity,
                priceUsd = product.priceUsd,
                unit = product.unit,
                imageUrl = product.imageUrl,
                sellerName = product.sellerName,
                status = "Chờ giao thương" // Pending Trade Transaction
            )
            repository.addCartItem(item)
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(item)
        }
    }

    fun submitTradeTransaction(items: List<CartItem>) {
        viewModelScope.launch {
            // Update cart items to "Sent" status as a simulation
            items.forEach { item ->
                repository.addCartItem(item.copy(status = "Đã gửi Yêu cầu")) // Request Sent
            }
        }
    }

    /**
     * Seller confirms the trade request and inputs shipping details.
     */
    fun sellerConfirmAndShip(item: CartItem, provider: String, trackingNum: String) {
        viewModelScope.launch {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            val currentTimeString = formatter.format(java.util.Date())
            val initialEvent = "$currentTimeString - [Hợp tác xã / Doanh nghiệp bán] Xác nhận đơn hàng thành công. Đang đóng gói bao bì tiêu chuẩn xuất khẩu nông sản."
            
            val updated = item.copy(
                status = "Đang vận chuyển",
                shippingProvider = provider,
                trackingNumber = trackingNum,
                shippingStatus = "Đang chuẩn bị hàng",
                shippingHistory = initialEvent
            )
            repository.addCartItem(updated)
        }
    }

    /**
     * Fetch simulated live updates from logistics provider using Gemini.
     */
    fun refreshTrackingWithAi(item: CartItem) {
        if (item.trackingNumber.isNullOrEmpty() || item.shippingProvider.isNullOrEmpty()) return

        // Set loading
        _isShippingLoading.value = _isShippingLoading.value + (item.id to true)

        viewModelScope.launch {
            try {
                // Find original product to get origin details if possible
                val product = repository.getProductById(item.productId)
                val origin = product?.origin ?: "Việt Nam"

                val result = GeminiClient.fetchCarrierTrackingUpdate(
                    productName = item.productName,
                    origin = origin,
                    carrier = item.shippingProvider,
                    trackingNumber = item.trackingNumber,
                    currentStatus = item.shippingStatus,
                    existingHistory = item.shippingHistory
                )

                // Parse the response JSON
                val parsed = parseTrackingJson(result)
                if (parsed != null) {
                    val newStatus = parsed.first
                    val newEvent = parsed.second
                    
                    val currentHistory = item.shippingHistory ?: ""
                    val updatedHistory = if (currentHistory.isEmpty()) {
                        newEvent
                    } else {
                        "$currentHistory|$newEvent"
                    }

                    val updated = item.copy(
                        shippingStatus = newStatus,
                        shippingHistory = updatedHistory
                    )
                    repository.addCartItem(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Clear loading
                _isShippingLoading.value = _isShippingLoading.value + (item.id to false)
            }
        }
    }

    /**
     * Helper to safely extract status and newEvent fields from Gemini's JSON response
     */
    private fun parseTrackingJson(jsonStr: String): Pair<String, String>? {
        try {
            val cleaned = jsonStr.replace("```json", "").replace("```", "").trim()
            val statusKey = "\"status\":"
            val newEventKey = "\"newEvent\":"

            val statusIdx = cleaned.indexOf(statusKey)
            val newEventIdx = cleaned.indexOf(newEventKey)

            if (statusIdx == -1 || newEventIdx == -1) return null

            // extract status
            val statusStart = cleaned.indexOf("\"", statusIdx + statusKey.length)
            val statusEnd = cleaned.indexOf("\"", statusStart + 1)
            val status = cleaned.substring(statusStart + 1, statusEnd).trim()

            // extract newEvent
            val newEventStart = cleaned.indexOf("\"", newEventIdx + newEventKey.length)
            var newEventEnd = newEventStart + 1
            while (newEventEnd < cleaned.length) {
                if (cleaned[newEventEnd] == '\"' && cleaned[newEventEnd - 1] != '\\') {
                    break
                }
                newEventEnd++
            }
            val newEvent = cleaned.substring(newEventStart + 1, newEventEnd)
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .trim()

            return Pair(status, newEvent)
        } catch (e: Exception) {
            return null
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // --- AI Consulting Chat State ---
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            text = "Xin chào! Tôi là cố vấn giao thương AgriGlobal AI. Tôi sẵn sàng giải đáp tất cả thắc mắc của bạn về tiêu chuẩn chất lượng nông sản xuất khẩu (VietGAP, GlobalGAP, USDA), giấy tờ kiểm dịch thực vật, thủ tục hải quan quốc tế (Incoterms FOB/CIF) và xu hướng thị trường nông sản toàn cầu. Bạn cần tư vấn điều gì hôm nay?",
            isUser = false
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendConsultingQuestion(question: String) {
        if (question.trim().isEmpty()) return

        val userMessage = ChatMessage(text = question, isUser = true)
        _chatMessages.value = _chatMessages.value + userMessage
        _isChatLoading.value = true

        viewModelScope.launch {
            val historyPairs = _chatMessages.value.dropLast(1).map { Pair(it.text, it.isUser) }
            val answer = GeminiClient.getConsultingResponse(historyPairs, question)
            
            _chatMessages.value = _chatMessages.value + ChatMessage(text = answer, isUser = false)
            _isChatLoading.value = false
        }
    }

    fun resetConsultingChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Xin chào! Tôi là cố vấn giao thương AgriGlobal AI. Tôi sẵn sàng giải đáp tất cả thắc mắc của bạn về tiêu chuẩn chất lượng nông sản xuất khẩu (VietGAP, GlobalGAP, USDA), giấy tờ kiểm dịch thực vật, thủ tục hải quan quốc tế (Incoterms FOB/CIF) và xu hướng thị trường nông sản toàn cầu. Bạn cần tư vấn điều gì hôm nay?",
                isUser = false
            )
        )
    }

    // --- Regulatory Assistant State ---
    private val _selectedCommodity = MutableStateFlow("Sầu riêng")
    val selectedCommodity: StateFlow<String> = _selectedCommodity.asStateFlow()

    private val _selectedMarket = MutableStateFlow("Trung Quốc")
    val selectedMarket: StateFlow<String> = _selectedMarket.asStateFlow()

    private val _checkedTasks = MutableStateFlow<Set<String>>(emptySet())
    val checkedTasks: StateFlow<Set<String>> = _checkedTasks.asStateFlow()

    private val _loadedStepAdvice = MutableStateFlow<Map<String, String>>(emptyMap())
    val loadedStepAdvice: StateFlow<Map<String, String>> = _loadedStepAdvice.asStateFlow()

    private val _isAdviceLoading = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isAdviceLoading: StateFlow<Map<String, Boolean>> = _isAdviceLoading.asStateFlow()

    fun updateCommodity(commodity: String) {
        _selectedCommodity.value = commodity
        clearCheckedTasks()
        clearRegulationsAnalysis()
    }

    fun updateMarket(market: String) {
        _selectedMarket.value = market
        clearCheckedTasks()
        clearRegulationsAnalysis()
    }

    fun toggleTask(taskId: String) {
        val current = _checkedTasks.value.toMutableSet()
        if (current.contains(taskId)) {
            current.remove(taskId)
        } else {
            current.add(taskId)
        }
        _checkedTasks.value = current
    }

    fun clearCheckedTasks() {
        _checkedTasks.value = emptySet()
        _loadedStepAdvice.value = emptyMap()
        _isAdviceLoading.value = emptyMap()
    }

    fun fetchStepAdvice(stepTitle: String, stepTasks: List<String>) {
        if (_loadedStepAdvice.value.containsKey(stepTitle)) return
        if (_isAdviceLoading.value[stepTitle] == true) return

        _isAdviceLoading.value = _isAdviceLoading.value + (stepTitle to true)

        viewModelScope.launch {
            val commodity = _selectedCommodity.value
            val market = _selectedMarket.value
            
            val prompt = """
                Bạn là chuyên gia tư vấn pháp lý xuất nhập khẩu nông sản quốc tế của AgriGlobal.
                Hãy đưa ra lời khuyên/lưu ý kỹ thuật vô cùng ngắn gọn, trực diện, cụ thể và thực tế cho bước sau:
                - Nông sản xuất khẩu: $commodity
                - Thị trường đích: $market
                - Bước quy trình: $stepTitle
                - Các đầu việc trong bước: ${stepTasks.joinToString(", ")}

                Yêu cầu câu trả lời:
                1. Ngôn ngữ: Tiếng Việt.
                2. Cực kỳ cụ thể cho loại nông sản $commodity khi nhập khẩu vào $market (ví dụ: nhiệt độ container lạnh, nồng độ methyl bromide, quy định kiểm dịch của GACC, FDA, EUDR, v.v.). Không đưa ra lời khuyên chung chung.
                3. Viết dưới dạng 2-3 gạch đầu dòng ngắn, súc tích (khoảng 80-120 từ tổng cộng). Không lời dẫn hay kết bài, vào thẳng nội dung lời khuyên.
            """.trimIndent()

            val advice = GeminiClient.generateContent(prompt)
            _loadedStepAdvice.value = _loadedStepAdvice.value + (stepTitle to advice)
            _isAdviceLoading.value = _isAdviceLoading.value + (stepTitle to false)
        }
    }

    // Full custom report text
    private val _regAnalysisText = MutableStateFlow("")
    val regAnalysisText: StateFlow<String> = _regAnalysisText.asStateFlow()

    private val _isRegAnalysisLoading = MutableStateFlow(false)
    val isRegAnalysisLoading: StateFlow<Boolean> = _isRegAnalysisLoading.asStateFlow()

    fun fetchRegulationsAnalysis() {
        if (_isRegAnalysisLoading.value) return
        _isRegAnalysisLoading.value = true
        _regAnalysisText.value = ""
        viewModelScope.launch {
            val analysis = GeminiClient.getRegulationsChecklistAnalysis(
                _selectedCommodity.value,
                _selectedMarket.value
            )
            _regAnalysisText.value = analysis
            _isRegAnalysisLoading.value = false
        }
    }

    fun clearRegulationsAnalysis() {
        _regAnalysisText.value = ""
    }

    // Factory Class Provider
    class Factory(
        private val application: Application,
        private val repository: ProductRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
                return MarketViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
