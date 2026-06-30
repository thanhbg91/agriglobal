package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: MarketViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Cà phê & Ca cao") }
    var priceUsd by remember { mutableStateOf("") }
    var availableQuantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Tấn") }
    var origin by remember { mutableStateOf("Việt Nam") }
    var basicDescription by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var sellerContact by remember { mutableStateOf("") }

    val aiDescription by viewModel.aiDescription.collectAsState()
    val isDescriptionEnhancing by viewModel.isDescriptionEnhancing.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Cà phê & Ca cao", "Trái cây", "Ngũ cốc", "Gia vị & Đặc sản", "Rau củ")
    val units = listOf("Tấn", "Kg", "Bao", "Hộp", "Thùng")

    // Update description field when AI finishes generating
    LaunchedEffect(aiDescription) {
        if (aiDescription.isNotEmpty()) {
            basicDescription = aiDescription
            viewModel.clearAiDescription()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng Tin Nông Sản") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Tips
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Đăng tin nguồn cung nông sản của bạn lên thị trường toàn cầu. Sử dụng trợ lý Gemini AI để viết mô tả chuyên nghiệp nhất!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Thông tin nông sản",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )

                // Product Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên sản phẩm nông sản *") },
                    placeholder = { Text("Ví dụ: Hạt ca cao Đắk Lắk hữu cơ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_product_name_input"),
                    singleLine = true
                )

                // Row for Category & Origin
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category drop-down
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Danh mục") },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn danh mục")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_category_selector")
                        )

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.fillMaxWidth(0.45f)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Origin Country
                    OutlinedTextField(
                        value = origin,
                        onValueChange = { origin = it },
                        label = { Text("Xuất xứ *") },
                        placeholder = { Text("Việt Nam") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_origin_input"),
                        singleLine = true
                    )
                }

                // Row for Price & Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Base Price in USD
                    OutlinedTextField(
                        value = priceUsd,
                        onValueChange = { priceUsd = it },
                        label = { Text("Giá (USD) *") },
                        placeholder = { Text("1.5") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "USD") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_price_input"),
                        singleLine = true
                    )

                    // Unit Picker Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Đơn vị tính") },
                            trailingIcon = {
                                IconButton(onClick = { showUnitMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn đơn vị")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_unit_selector")
                        )

                        DropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        showUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Available Quantity
                OutlinedTextField(
                    value = availableQuantity,
                    onValueChange = { availableQuantity = it },
                    label = { Text("Sản lượng cung ứng sẵn có *") },
                    placeholder = { Text("10.0") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_quantity_input"),
                    singleLine = true
                )

                // Description Field with Gemini Enhancer Button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mô tả nông sản *",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        // Optimize with Gemini Button
                        Button(
                            onClick = {
                                val priceVal = priceUsd.toDoubleOrNull() ?: 0.0
                                if (name.isEmpty() || origin.isEmpty() || priceVal <= 0) {
                                    errorMsg = "Vui lòng nhập Tên, Xuất xứ và Giá để AI viết mô tả chính xác."
                                } else {
                                    viewModel.enhanceDescription(
                                        name = name,
                                        category = category,
                                        origin = origin,
                                        price = priceVal,
                                        unit = unit,
                                        basic = basicDescription
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("ai_enhance_button")
                        ) {
                            if (isDescriptionEnhancing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Đang Soạn...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI viết mô tả", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Viết Mô Tả", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = basicDescription,
                        onValueChange = { basicDescription = it },
                        placeholder = { Text("Hãy nhập mô tả sơ lược, hoặc nhấn 'AI Viết Mô Tả' phía trên để trợ lý Gemini AI tự động soạn thảo bài viết thương mại chuyên nghiệp đạt tiêu chuẩn xuất khẩu quốc tế.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("add_desc_input"),
                        maxLines = 10
                    )
                }

                Text(
                    text = "Thông tin liên hệ giao thương",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )

                // Seller Name
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Tên doanh nghiệp / Hợp tác xã *") },
                    placeholder = { Text("Hợp tác xã nông sản Organic Việt Nam") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_seller_name_input"),
                    singleLine = true
                )

                // Contact Detail
                OutlinedTextField(
                    value = sellerContact,
                    onValueChange = { sellerContact = it },
                    label = { Text("Email hoặc Số điện thoại liên hệ *") },
                    placeholder = { Text("trade@vietnamorganics.vn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_seller_contact_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Error Msg
                AnimatedVisibility(visible = errorMsg != null) {
                    errorMsg?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Create Listing Submit Button
                Button(
                    onClick = {
                        val priceVal = priceUsd.toDoubleOrNull()
                        val qtyVal = availableQuantity.toDoubleOrNull()

                        if (name.trim().isEmpty() || origin.trim().isEmpty() || sellerName.trim().isEmpty() || sellerContact.trim().isEmpty() || basicDescription.trim().isEmpty()) {
                            errorMsg = "Vui lòng điền đầy đủ các thông tin đánh dấu (*)."
                        } else if (priceVal == null || priceVal <= 0) {
                            errorMsg = "Giá bán phải là số hợp lệ lớn hơn 0 USD."
                        } else if (qtyVal == null || qtyVal <= 0) {
                            errorMsg = "Sản lượng sẵn có phải là số hợp lệ lớn hơn 0."
                        } else {
                            viewModel.createListing(
                                name = name,
                                category = category,
                                priceUsd = priceVal,
                                quantity = qtyVal,
                                unit = unit,
                                origin = origin,
                                description = basicDescription,
                                sellerName = sellerName,
                                sellerContact = sellerContact
                            )
                            errorMsg = null
                            successMsg = "Đăng tin nông sản thành công! Tin của bạn đã hiển thị trên thị trường."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_listing_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Đăng tin")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ĐĂNG TIN GIAO THƯƠNG NGAY",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            // Success dialog overlay
            AnimatedVisibility(visible = successMsg != null) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Thành Công 🎉", fontWeight = FontWeight.Bold) },
                    text = { Text(successMsg ?: "") },
                    confirmButton = {
                        Button(
                            onClick = {
                                successMsg = null
                                onBackClick() // Go back to Home
                            }
                        ) {
                            Text("Hoàn Thành")
                        }
                    }
                )
            }
        }
    }
}
