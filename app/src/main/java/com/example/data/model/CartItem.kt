package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val productName: String,
    val quantity: Double,
    val priceUsd: Double,
    val unit: String,
    val imageUrl: String,
    val sellerName: String,
    val status: String = "Draft", // e.g., "Draft", "Sent", "Confirmed"
    val timestamp: Long = System.currentTimeMillis(),
    val trackingNumber: String? = null,
    val shippingProvider: String? = null,
    val shippingStatus: String = "Chưa vận chuyển", // e.g., "Chưa vận chuyển", "Đang chuẩn bị hàng", "Đang vận chuyển", "Đang kiểm dịch/thông quan", "Đã giao hàng"
    val shippingHistory: String? = null // Separated by '|' to store date and status updates
)
