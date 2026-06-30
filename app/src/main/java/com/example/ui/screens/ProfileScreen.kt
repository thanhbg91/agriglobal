package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MarketViewModel,
    onProductClick: (Product) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userListings by viewModel.userListings.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    val backendBaseUrl by viewModel.backendBaseUrl.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isCheckingConnection by viewModel.isCheckingConnection.collectAsState()

    var showServerSettingsDialog by remember { mutableStateOf(false) }
    var tempUrlInput by remember { mutableStateOf(backendBaseUrl) }

    LaunchedEffect(showServerSettingsDialog) {
        if (showServerSettingsDialog) {
            tempUrlInput = backendBaseUrl
        }
    }

    var activeTab by remember { mutableStateOf("listings") } // "listings", "favorites", "sales"

    // Filter sales orders (requests received from buyers)
    val salesOrders = cartItems.filter { it.status == "Đã gửi Yêu cầu" || it.status == "Đang vận chuyển" }

    // Dialog State
    var selectedOrderToShip by remember { mutableStateOf<CartItem?>(null) }
    var carrierSelection by remember { mutableStateOf("DHL Express") }
    var trackingInput by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Prepopulated Carriers
    val carriers = listOf("DHL Express", "Maersk Line", "Vietnam Post", "Cosco Shipping", "FedEx", "Ocean Network Express")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Không Gian Doanh Nghiệp", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showServerSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình Server API"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
        ) {
            // Profile Card Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val localContext = androidx.compose.ui.platform.LocalContext.current
                        val username = remember { com.example.api.AuthManager.getStoredUsername(localContext) }
                        val userEmail = remember { com.example.api.AuthManager.getStoredEmail(localContext) }
                        val avatarInitials = remember(username) {
                            val cleanName = username.trim()
                            if (cleanName.contains(" ")) {
                                val parts = cleanName.split(" ").filter { it.isNotBlank() }
                                if (parts.size >= 2) {
                                    (parts[0].take(1) + parts[1].take(1)).uppercase()
                                } else {
                                    cleanName.take(2).uppercase()
                                }
                            } else if (cleanName.length >= 2) {
                                cleanName.take(2).uppercase()
                            } else {
                                "AG"
                            }
                        }

                        // Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatarInitials,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (userEmail.isNotBlank()) "Tài khoản: $userEmail" else "Hội viên xuất nhập khẩu nông nghiệp số",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Xác minh",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Đã thông quan hải quan điện tử",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Compact Logout button
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Đăng xuất",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Tab Toggler (High Density 3-Tab row)
            TabRow(
                selectedTabIndex = when (activeTab) {
                    "listings" -> 0
                    "favorites" -> 1
                    else -> 2
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == "listings",
                    onClick = { activeTab = "listings" },
                    modifier = Modifier.testTag("my_listings_tab")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Sản Phẩm (${userListings.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Tab(
                    selected = activeTab == "favorites",
                    onClick = { activeTab = "favorites" },
                    modifier = Modifier.testTag("favorites_tab")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Yêu Thích (${favorites.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Tab(
                    selected = activeTab == "sales",
                    onClick = { activeTab = "sales" },
                    modifier = Modifier.testTag("sales_tab")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Đơn Bán (${salesOrders.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Display list depending on selected tab
            when (activeTab) {
                "listings" -> {
                    if (userListings.isEmpty()) {
                        EmptyStateView("🏪", "Bạn chưa đăng bán nông sản nào", "Hãy nhấp vào nút 'Đăng Tin' ở góc màn hình Chợ để thêm nguồn cung nông nghiệp của bạn.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(userListings) { product ->
                                ProductRowItem(product, viewModel, onProductClick)
                            }
                        }
                    }
                }
                "favorites" -> {
                    if (favorites.isEmpty()) {
                        EmptyStateView("❤️", "Chưa có nông sản yêu thích nào", "Lướt Chợ nông sản và thả tim các loại nông sản tiềm năng để lưu giữ liên kết ở đây nhé.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favorites) { product ->
                                ProductRowItem(product, viewModel, onProductClick)
                            }
                        }
                    }
                }
                "sales" -> {
                    if (salesOrders.isEmpty()) {
                        EmptyStateView("📦", "Chưa có đơn hàng thương thảo nào", "Khi khách hàng quốc tế gửi đề xuất đàm phán hợp đồng nông sản cho các nguồn cung của bạn, các giao dịch sẽ hiển thị ở đây để bạn xác nhận vận đơn và chuẩn bị chứng nhận hải quan.")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(salesOrders) { item ->
                                SellerOrderRowItem(
                                    item = item,
                                    viewModel = viewModel,
                                    onShipClick = {
                                        selectedOrderToShip = item
                                        carrierSelection = "DHL Express"
                                        // Auto-generate a realistic tracking number prefix to save time and look awesome
                                        val randId = (100000..999999).random()
                                        trackingInput = when (carrierSelection) {
                                            "Maersk Line" -> "MSK-VN-$randId"
                                            "DHL Express" -> "DHL-EXP-$randId"
                                            "Vietnam Post" -> "VNPOST-INT-$randId"
                                            else -> "EXP-SHIP-$randId"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Seller Input Shipping Info Dialog
        if (selectedOrderToShip != null) {
            val order = selectedOrderToShip!!
            AlertDialog(
                onDismissRequest = { selectedOrderToShip = null },
                title = { Text("Thông Tin Vận Chuyển 🚢", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Vui lòng nhập vận đơn cho đơn hàng nông sản:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${order.productName} (${order.quantity} ${order.unit})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Carrier Dropdown Label
                        Text("Đơn vị vận chuyển:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        // Vertical list of carrier buttons for a responsive, compact UI
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val topCarriers = listOf("DHL Express", "Maersk Line", "Vietnam Post")
                            topCarriers.forEach { carrier ->
                                FilterChip(
                                    selected = carrierSelection == carrier,
                                    onClick = {
                                        carrierSelection = carrier
                                        val randId = (100000..999999).random()
                                        trackingInput = when (carrier) {
                                            "Maersk Line" -> "MSK-VN-$randId"
                                            "DHL Express" -> "DHL-EXP-$randId"
                                            "Vietnam Post" -> "VNPOST-INT-$randId"
                                            else -> "EXP-SHIP-$randId"
                                        }
                                    },
                                    label = { Text(carrier, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Tracking Number TextField
                        OutlinedTextField(
                            value = trackingInput,
                            onValueChange = { trackingInput = it },
                            label = { Text("Mã vận đơn (Tracking Code)", fontSize = 11.sp) },
                            placeholder = { Text("VD: MSK-VN-239482") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tracking_input_field")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "* Hệ thống sẽ tự động đồng bộ hành trình thực thông qua cổng kết nối logistics điện tử của đơn vị vận tải.",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (trackingInput.isNotBlank()) {
                                viewModel.sellerConfirmAndShip(
                                    item = order,
                                    provider = carrierSelection,
                                    trackingNum = trackingInput.trim()
                                )
                                selectedOrderToShip = null
                                showSuccessToast = true
                            }
                        },
                        enabled = trackingInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Xác nhận & Giao hàng", fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedOrderToShip = null }) {
                        Text("Hủy", fontSize = 12.sp)
                    }
                }
            )
        }

        // Simple Success Toast dialog
        if (showSuccessToast) {
            AlertDialog(
                onDismissRequest = { showSuccessToast = false },
                title = { Text("Bốc Dỡ Giao Hàng Thành Công! 🎉", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = { Text("Vận đơn quốc tế đã được ghi nhận. Hệ thống đã kích hoạt cổng thông báo kiểm dịch hải quan và đồng bộ tiến độ hành trình tự động cho người mua hàng.", fontSize = 13.sp) },
                confirmButton = {
                    Button(onClick = { showSuccessToast = false }) {
                        Text("Đồng ý", fontSize = 12.sp)
                    }
                }
            )
        }

        // Server Settings and Sync Dialog
        if (showServerSettingsDialog) {
            AlertDialog(
                onDismissRequest = { 
                    if (!isSyncing && !isCheckingConnection) {
                        showServerSettingsDialog = false 
                        viewModel.clearSyncStatus()
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đồng Bộ Cloud API", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nhập URL của Backend API để đồng bộ đăng tin sản phẩm và lịch sử giao dịch nông sản cục bộ lên máy chủ cơ sở dữ liệu (PostgreSQL, MySQL, MongoDB).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = tempUrlInput,
                            onValueChange = { tempUrlInput = it },
                            label = { Text("Địa chỉ API Server URL", fontSize = 11.sp) },
                            placeholder = { Text("http://10.0.2.2:5000") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSyncing && !isCheckingConnection
                        )

                        // Info details
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("💡 Hướng dẫn địa chỉ:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("• Android Emulator: http://10.0.2.2:5000", fontSize = 9.sp)
                                Text("• Thiết bị thật: Nhập IP máy tính chạy API (ví dụ: http://192.168.1.15:5000)", fontSize = 9.sp)
                            }
                        }

                        // Connection Checker Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.updateBackendUrl(tempUrlInput.trim())
                                    viewModel.checkBackendConnection() 
                                },
                                enabled = tempUrlInput.isNotBlank() && !isCheckingConnection && !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isCheckingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kiểm tra kết nối", fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = connectionStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (connectionStatus.contains("Lỗi") || connectionStatus.contains("Không thể")) Color.Red else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Cloud Synchronization Section
                        Button(
                            onClick = { 
                                viewModel.updateBackendUrl(tempUrlInput.trim())
                                viewModel.syncAllToCloudBackend() 
                            },
                            enabled = tempUrlInput.isNotBlank() && !isCheckingConnection && !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang tải dữ liệu lên...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ĐỒNG BỘ TOÀN BỘ ĐÁM MÂY", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            }
                        }

                        // Sync Status Progress Message
                        syncStatus?.let { statusMsg ->
                            Text(
                                text = statusMsg,
                                fontSize = 11.sp,
                                color = if (statusMsg.contains("thành công")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ).padding(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            showServerSettingsDialog = false 
                            viewModel.clearSyncStatus()
                        },
                        enabled = !isSyncing && !isCheckingConnection
                    ) {
                        Text("Đóng", fontSize = 12.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun ProductRowItem(
    product: Product,
    viewModel: MarketViewModel,
    onProductClick: (Product) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Xuất xứ: ${product.origin}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${viewModel.formatPrice(product.priceUsd)} / ${product.unit}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SellerOrderRowItem(
    item: CartItem,
    viewModel: MarketViewModel,
    onShipClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_order_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val isShipped = item.status == "Đang vận chuyển"
            // Header: buyer, commodity name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Đối tác đề xuất: Nhà mua hàng quốc tế",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isShipped) MaterialTheme.colorScheme.primaryContainer else Color(0xFFFFECEB),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isShipped) "Đã giao vận" else "Yêu cầu mới",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isShipped) MaterialTheme.colorScheme.primary else Color(0xFFC51162)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quantities & totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Khối lượng: ${item.quantity} ${item.unit}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Trị giá: " + viewModel.formatPrice(item.priceUsd * item.quantity),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Interactive Actions
            if (!isShipped) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onShipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .testTag("seller_ship_button_${item.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xác nhận & Giao vận (Incoterms)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Đơn vận: ${item.shippingProvider} - ${item.trackingNumber}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tiến độ: ${item.shippingStatus}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: String, title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
