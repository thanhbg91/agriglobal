package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val priceUsd: Double,
    val availableQuantity: Double,
    val unit: String, // e.g., "Kg", "Tấn", "Bao"
    val origin: String, // e.g., "Việt Nam", "Nhật Bản", "Thái Lan"
    val description: String,
    val imageUrl: String,
    val sellerName: String,
    val sellerContact: String,
    val isUserListing: Boolean = false,
    val isFavorited: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
