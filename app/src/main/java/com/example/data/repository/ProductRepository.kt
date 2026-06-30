package com.example.data.repository

import com.example.data.dao.ProductDao
import com.example.data.model.CartItem
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val favoriteProducts: Flow<List<Product>> = productDao.getFavoriteProducts()
    val userListings: Flow<List<Product>> = productDao.getUserListings()
    val allCartItems: Flow<List<CartItem>> = productDao.getAllCartItems()

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun toggleFavorite(productId: Int) {
        val product = productDao.getProductById(productId)
        if (product != null) {
            productDao.updateProduct(product.copy(isFavorited = !product.isFavorited))
        }
    }

    suspend fun addCartItem(cartItem: CartItem): Long {
        return productDao.insertCartItem(cartItem)
    }

    suspend fun deleteCartItem(cartItem: CartItem) {
        productDao.deleteCartItem(cartItem)
    }

    suspend fun clearCart() {
        productDao.clearCart()
    }

    suspend fun prepopulateDatabaseIfNeeded() {
        val count = productDao.getProductCount()
        if (count == 0) {
            val defaults = listOf(
                Product(
                    name = "Cà phê Robusta Đắk Lắk S18",
                    category = "Cà phê & Ca cao",
                    priceUsd = 2.40,
                    availableQuantity = 25.0,
                    unit = "Tấn",
                    origin = "Việt Nam",
                    description = "Hạt cà phê Robusta chất lượng cao, sàng 18 chế biến ướt xuất xứ từ thủ phủ cà phê Buôn Ma Thuột, Đắk Lắk. Độ ẩm < 12.5%, tỷ lệ hạt đen vỡ < 1%. Thích hợp xuất khẩu châu Âu.",
                    imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Hợp tác xã Nông nghiệp Ea Kmat",
                    sellerContact = "robusta.daklak@eakmat-coop.vn"
                ),
                Product(
                    name = "Táo Fuji Aomori",
                    category = "Trái cây",
                    priceUsd = 3.80,
                    availableQuantity = 8.5,
                    unit = "Tấn",
                    origin = "Nhật Bản",
                    description = "Táo Fuji đỏ mọng nhập khẩu trực tiếp từ tỉnh Aomori, Nhật Bản. Trái to tròn, giòn ngọt đậm đà, thu hoạch hữu cơ nghiêm ngặt đạt chuẩn xuất khẩu toàn cầu.",
                    imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Aomori Apple Growers Association",
                    sellerContact = "export@aomori-apples.jp"
                ),
                Product(
                    name = "Gạo Thơm Jasmine Thái Lan",
                    category = "Ngũ cốc",
                    priceUsd = 0.85,
                    availableQuantity = 50.0,
                    unit = "Tấn",
                    origin = "Thái Lan",
                    description = "Gạo nhài Hom Mali thượng hạng vùng Issan, Thái Lan. Hạt gạo dài, dẻo, thơm hương hoa nhài tự nhiên khi nấu chín. Đạt tiêu chuẩn chất lượng xuất khẩu gạo quốc tế.",
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Siam Rice Trading Co., Ltd",
                    sellerContact = "contact@siamrice.co.th"
                ),
                Product(
                    name = "Hạt Ca cao Hữu cơ Bahia",
                    category = "Cà phê & Ca cao",
                    priceUsd = 4.50,
                    availableQuantity = 12.0,
                    unit = "Tấn",
                    origin = "Brazil",
                    description = "Hạt ca cao hữu cơ lên men tự nhiên đạt chuẩn USDA từ bang Bahia, Brazil. Hương vị đậm đà, béo ngậy, thích hợp cho các nhà sản xuất sô cô la thủ công cao cấp toàn cầu.",
                    imageUrl = "https://images.unsplash.com/photo-1587132137056-bfbf0166836e?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Bahia Organics Ltda",
                    sellerContact = "trade@bahiaorganics.com.br"
                ),
                Product(
                    name = "Tiêu đen Malabar Thượng hạng",
                    category = "Gia vị & Đặc sản",
                    priceUsd = 3.20,
                    availableQuantity = 15.0,
                    unit = "Tấn",
                    origin = "Ấn Độ",
                    description = "Hạt tiêu đen Malabar trứ danh từ vùng Kerala, Ấn Độ. Hạt tiêu to mẩy, hương vị cay nồng đặc trưng, tỷ lệ tạp chất cực thấp, sấy khô tự nhiên không hóa chất bảo quản.",
                    imageUrl = "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Kerala Spice Growers Group",
                    sellerContact = "export@keralaspices.org"
                ),
                Product(
                    name = "Sầu riêng Ri6 Miền Tây Loại 1",
                    category = "Trái cây",
                    priceUsd = 6.50,
                    availableQuantity = 10.0,
                    unit = "Tấn",
                    origin = "Việt Nam",
                    description = "Sầu riêng Ri6 đặc sản miền Tây Nam Bộ, cơm vàng, hạt lép, vị béo ngọt lịm đặc trưng. Cấp đông muối dập chân không xuất khẩu Mỹ, Trung Quốc và Úc.",
                    imageUrl = "https://images.unsplash.com/photo-1595855759920-86582396756a?w=600&auto=format&fit=crop&q=60",
                    sellerName = "VinaFruit Export Corp",
                    sellerContact = "sales@vinafruit-export.com"
                ),
                Product(
                    name = "Lúa mì Hữu cơ Kansas",
                    category = "Ngũ cốc",
                    priceUsd = 0.40,
                    availableQuantity = 120.0,
                    unit = "Tấn",
                    origin = "Hoa Kỳ",
                    description = "Lúa mì cứng đỏ mùa đông đạt chứng nhận hữu cơ quốc tế thu hoạch tại Kansas, Mỹ. Hàm lượng protein cao (13.5%), hoàn hảo cho bột mì làm bánh mì chất lượng cao.",
                    imageUrl = "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600&auto=format&fit=crop&q=60",
                    sellerName = "Midwest Organic Grains",
                    sellerContact = "orders@midwestgrains.com"
                )
            )
            productDao.insertProducts(defaults)
        }
    }
}
