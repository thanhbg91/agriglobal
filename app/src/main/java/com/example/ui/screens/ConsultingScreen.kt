package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MarketViewModel

// Data structure for the compliance items
data class ComplianceTask(
    val id: String,
    val title: String,
    val desc: String,
    val agency: String,
    val timeline: String
)

data class ComplianceStep(
    val title: String,
    val shortTitle: String,
    val icon: String,
    val tasks: List<ComplianceTask>
)

// Helper to construct dynamic steps based on agricultural commodity & destination
fun getComplianceStepsFor(commodity: String, market: String): List<ComplianceStep> {
    return listOf(
        ComplianceStep(
            title = "Bước 1: Chuẩn bị Vùng trồng & Cơ sở đóng gói",
            shortTitle = "Nguồn cung",
            icon = "🌱",
            tasks = listOf(
                ComplianceTask(
                    id = "puc",
                    title = "Đăng ký Mã số Vùng trồng (PUC)",
                    desc = "Vườn canh tác $commodity phải đăng ký kiểm tra dịch hại định kỳ và được Cục Bảo vệ thực vật Việt Nam cấp mã số định danh đáp ứng yêu cầu của thị trường $market.",
                    agency = "Cục Bảo vệ thực vật Việt Nam",
                    timeline = "30 - 45 ngày trước thu hoạch"
                ),
                ComplianceTask(
                    id = "phc",
                    title = "Đăng ký Mã số Cơ sở Đóng gói (PHC)",
                    desc = "Nhà xưởng đóng gói phải đáp ứng tiêu chuẩn vệ sinh dịch tễ, quy trình phân loại bốc xếp, vệ sinh côn trùng/rệp sáp bám trên vỏ quả và được cấp mã đóng gói xuất sang $market.",
                    agency = "Chi cục Trồng trọt & BVTV địa phương",
                    timeline = "Trước vụ thu hoạch xuất khẩu"
                ),
                ComplianceTask(
                    id = "gap",
                    title = "Áp dụng tiêu chuẩn canh tác thực hành tốt (GAP)",
                    desc = "Canh tác nông nghiệp theo quy trình tiêu chuẩn VietGAP, GlobalGAP hoặc hữu cơ tương đương nhằm quản lý và kiểm soát triệt để dư lượng hóa chất (MRLs).",
                    agency = "Hợp tác xã & Tổ chức chứng nhận độc lập",
                    timeline = "Trong suốt quá trình chăm sóc"
                )
            )
        ),
        ComplianceStep(
            title = "Bước 2: Kiểm nghiệm kỹ thuật & Giấy Kiểm dịch thực vật",
            shortTitle = "Kiểm dịch",
            icon = "🔬",
            tasks = listOf(
                ComplianceTask(
                    id = "mrl",
                    title = "Kiểm nghiệm dư lượng thuốc BVTV & Hoạt chất cấm",
                    desc = "Lấy mẫu ngẫu nhiên tại vườn gửi đi kiểm nghiệm dư lượng hóa chất trừ sâu đạt chuẩn dưới ngưỡng cho phép tối đa (MRL) của nước nhập khẩu $market.",
                    agency = "Trung tâm phân tích độc lập được chỉ định (Eurofins, Quatest)",
                    timeline = "7 - 10 ngày trước khi thu hoạch dứt điểm"
                ),
                ComplianceTask(
                    id = "treatment",
                    title = "Xử lý kiểm dịch kỹ thuật bắt buộc",
                    desc = "Lô hàng $commodity xuất khẩu đi $market bắt buộc phải trải qua công nghệ xử lý hơi nước nóng (VHT), chiếu xạ kiểm dịch (Irradiation) hoặc xông trùng tương thích.",
                    agency = "Cơ sở xử lý liên kết được cấp phép bởi Bộ Nông nghiệp",
                    timeline = "1 - 2 ngày trước đóng container bốc xếp"
                ),
                ComplianceTask(
                    id = "phyto",
                    title = "Cấp Chứng thư kiểm dịch thực vật gốc (Phyto Cert)",
                    desc = "Cán bộ kiểm dịch kiểm tra cảm quan thực tế lô hàng tại cảng xuất khẩu Việt Nam và cấp Giấy kiểm dịch gốc (Phytosanitary Certificate) hợp lệ sang $market.",
                    agency = "Chi cục Kiểm dịch thực vật Vùng",
                    timeline = "24 - 48 giờ trước thời gian bốc tàu chạy"
                )
            )
        ),
        ComplianceStep(
            title = "Bước 3: Tờ khai hải quan Việt Nam & Chứng nhận CO",
            shortTitle = "Hải quan đi",
            icon = "🚢",
            tasks = listOf(
                ComplianceTask(
                    id = "customs",
                    title = "Truyền tờ khai hải quan xuất khẩu",
                    desc = "Khai báo tờ khai thông quan xuất khẩu nông sản (mã loại hình kinh doanh B11) trên hệ thống điện tử VNACCS kèm Invoice, Packing List, hợp đồng.",
                    agency = "Hải quan cửa khẩu Việt Nam",
                    timeline = "1 - 2 ngày trước khi tàu khởi hành"
                ),
                ComplianceTask(
                    id = "co",
                    title = "Xin cấp Chứng nhận Xuất xứ nguồn gốc (C/O)",
                    desc = "Nộp hồ sơ xin cấp giấy chứng nhận xuất xứ tương ứng (ví dụ: Form E đi Trung Quốc, EUR.1 đi EU, Form B hoặc CPTPP) để bên mua hưởng ưu đãi thuế suất nhập khẩu.",
                    agency = "Bộ Công Thương hoặc Phòng Thương mại và Công nghiệp (VCCI)",
                    timeline = "Ngay khi có vận đơn đường biển gốc (B/L)"
                ),
                ComplianceTask(
                    id = "fumi",
                    title = "Khử trùng pallet gỗ & Hoàn tất vận đơn bốc dỡ",
                    desc = "Khử trùng pallet gỗ xếp hàng theo quy chuẩn quốc tế ISPM 15 lấy chứng từ và nhận vận đơn đường biển chính thức từ hãng tàu vận tải.",
                    agency = "Đơn vị khử trùng & Đơn vị logistics hàng hải",
                    timeline = "Trước khi bốc container lên tàu"
                )
            )
        ),
        ComplianceStep(
            title = "Bước 4: Khai báo trước & Thông quan tại Cảng Đích",
            shortTitle = "Thông quan đến",
            icon = "✈️",
            tasks = listOf(
                ComplianceTask(
                    id = "prior",
                    title = "Khai báo thông tin nhập khẩu trước (Prior Notice)",
                    desc = "Đăng ký khai báo trước thông tin chi tiết lô hàng qua cổng một cửa của nước nhập khẩu (ví dụ: Prior Notice FDA Hoa Kỳ, hệ thống TRACES NT của EU, hải quan Trung Quốc).",
                    agency = "Cục Hải quan & Vệ sinh thực phẩm nước nhập khẩu",
                    timeline = "2 - 24 giờ trước khi container cập cảng"
                ),
                ComplianceTask(
                    id = "inspect",
                    title = "Kiểm tra thực tế kiểm dịch tại biên giới đích",
                    desc = "Cơ quan kiểm dịch nước đích đối chiếu Giấy kiểm dịch thực vật gốc của Việt Nam và tiến hành lấy mẫu kiểm nghiệm ngẫu nhiên để phát hiện sâu hại.",
                    agency = "Cơ quan kiểm dịch an toàn thực phẩm nước đến",
                    timeline = "Ngay khi hàng hạ bãi cảng nhập khẩu"
                ),
                ComplianceTask(
                    id = "clear",
                    title = "Nộp thuế quan & Giải phóng lô hàng phân phối",
                    desc = "Áp dụng miễn giảm thuế nhập khẩu dựa trên C/O gốc đạt chuẩn, nộp thuế phí liên quan và thông quan kéo hàng về kho phân phối nội địa.",
                    agency = "Hải quan nước nhập khẩu",
                    timeline = "Ngay khi vượt qua bước kiểm tra kiểm dịch"
                )
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultingScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("chat") } // "chat", "regulations"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cố Vấn Thương Mại AI", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header Selection
            TabRow(
                selectedTabIndex = if (activeTab == "chat") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == "chat",
                    onClick = { activeTab = "chat" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Trò chuyện") },
                    text = { Text("Trò Chuyện AI", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_chat")
                )
                Tab(
                    selected = activeTab == "regulations",
                    onClick = { activeTab = "regulations" },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Thủ tục") },
                    text = { Text("Bản Đồ Thủ Tục", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_regulations")
                )
            }

            if (activeTab == "chat") {
                ChatTabContent(viewModel = viewModel)
            } else {
                RegulationsTabContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChatTabContent(viewModel: MarketViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val listState = rememberLazyListState()
    var questionInput by remember { mutableStateOf("") }

    val presetQuestions = listOf(
        "Mã số vùng trồng (PUC) xuất khẩu sầu riêng?",
        "Làm sao xuất khẩu sầu riêng sang Trung Quốc?",
        "Chứng nhận Phytosanitary là gì?",
        "Incoterms FOB và CIF khác gì nhau?",
        "Chứng nhận USDA Organic yêu cầu gì?"
    )

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Preset suggestions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gợi ý câu hỏi giao thương:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = { viewModel.resetConsultingChat() },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa lịch sử", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetQuestions) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .clickable { viewModel.sendConsultingQuestion(preset) }
                            .testTag("preset_${preset.take(10)}")
                    ) {
                        Text(
                            text = preset,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Chat History List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { msg ->
                ChatBubble(message = msg)
            }

            if (isChatLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Cố vấn AI đang phân tích rào cản...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                    }
                }
            }
        }

        // Send chat layout
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = { Text("Hỏi về thủ tục, hải quan, kiểm dịch, Incoterms...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                IconButton(
                    onClick = {
                        if (questionInput.trim().isNotEmpty()) {
                            viewModel.sendConsultingQuestion(questionInput)
                            questionInput = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_chat_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gửi", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun RegulationsTabContent(viewModel: MarketViewModel) {
    val selectedCommodity by viewModel.selectedCommodity.collectAsState()
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val checkedTasks by viewModel.checkedTasks.collectAsState()
    val loadedStepAdvice by viewModel.loadedStepAdvice.collectAsState()
    val isAdviceLoading by viewModel.isAdviceLoading.collectAsState()
    val regAnalysisText by viewModel.regAnalysisText.collectAsState()
    val isRegAnalysisLoading by viewModel.isRegAnalysisLoading.collectAsState()

    var activeStepIdx by remember { mutableStateOf(0) }
    var showCommodityDialog by remember { mutableStateOf(false) }
    var showMarketDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val complianceSteps = getComplianceStepsFor(selectedCommodity, selectedMarket)

    // Calculate active step completion progress
    val allDynamicTaskIds = remember(selectedCommodity, selectedMarket) {
        val list = mutableListOf<String>()
        complianceSteps.forEachIndexed { sIdx, step ->
            step.tasks.forEach { task ->
                list.add("${selectedCommodity}_${selectedMarket}_${sIdx}_${task.id}")
            }
        }
        list
    }

    val checkedCount = allDynamicTaskIds.count { checkedTasks.contains(it) }
    val totalTasks = allDynamicTaskIds.size
    val progressPercentage = if (totalTasks > 0) (checkedCount.toFloat() / totalTasks.toFloat()) * 100f else 0f

    val commoditiesList = listOf("Sầu riêng", "Gạo", "Cà phê", "Thanh long", "Hạt điều")
    val marketsList = listOf("Trung Quốc", "Hoa Kỳ", "Liên minh Châu Âu (EU)", "Nhật Bản", "Úc")

    if (showCommodityDialog) {
        AlertDialog(
            onDismissRequest = { showCommodityDialog = false },
            title = { Text("Chọn mặt hàng xuất khẩu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    commoditiesList.forEach { comm ->
                        val emojiName = when(comm) {
                            "Sầu riêng" -> "🥑 Sầu riêng tươi"
                            "Gạo" -> "🌾 Gạo Việt Nam"
                            "Cà phê" -> "☕ Hạt cà phê nhân"
                            "Thanh long" -> "🐉 Quả thanh long"
                            "Hạt điều" -> "🥜 Hạt điều xuất khẩu"
                            else -> comm
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCommodity == comm) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCommodity(comm)
                                    showCommodityDialog = false
                                }
                        ) {
                            Text(
                                emojiName,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCommodity == comm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCommodityDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showMarketDialog) {
        AlertDialog(
            onDismissRequest = { showMarketDialog = false },
            title = { Text("Chọn thị trường nhập khẩu", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    marketsList.forEach { mkt ->
                        val flagName = when(mkt) {
                            "Trung Quốc" -> "🇨🇳 Trung Quốc (GACC)"
                            "Hoa Kỳ" -> "🇺🇸 Hoa Kỳ (FDA & USDA)"
                            "Liên minh Châu Âu (EU)" -> "🇪🇺 Liên minh Châu Âu"
                            "Nhật Bản" -> "🇯🇵 Nhật Bản (MAFF)"
                            "Úc" -> "🇦🇺 Úc (DAFF)"
                            else -> mkt
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMarket == mkt) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateMarket(mkt)
                                    showMarketDialog = false
                                }
                        ) {
                            Text(
                                flagName,
                                modifier = Modifier.padding(14.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMarket == mkt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMarketDialog = false }) { Text("Hủy") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Selection Selectors Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "CẤU HÌNH LỘ TRÌNH XUẤT KHẨU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Commodity selector
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCommodityDialog = true }
                            .testTag("select_commodity"),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Sản phẩm:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = when(selectedCommodity) {
                                    "Sầu riêng" -> "🥑 Sầu riêng"
                                    "Gạo" -> "🌾 Gạo tẻ"
                                    "Cà phê" -> "☕ Cà phê"
                                    "Thanh long" -> "🐉 Thanh long"
                                    "Hạt điều" -> "🥜 Hạt điều"
                                    else -> selectedCommodity
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Market selector
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showMarketDialog = true }
                            .testTag("select_market"),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Thị trường:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = when(selectedMarket) {
                                    "Trung Quốc" -> "🇨🇳 Trung Quốc"
                                    "Hoa Kỳ" -> "🇺🇸 Hoa Kỳ"
                                    "Liên minh Châu Âu (EU)" -> "🇪🇺 Châu Âu"
                                    "Nhật Bản" -> "🇯🇵 Nhật Bản"
                                    "Úc" -> "🇦🇺 Úc"
                                    else -> selectedMarket
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Progress indicators card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tiến Độ Hoàn Thành Thủ Tục", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Đã tuân thủ $checkedCount/$totalTasks hạng mục (${progressPercentage.toInt()}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearCheckedTasks() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reset_checklist_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tải lại tiến độ", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressPercentage / 100f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        // Horizontal Step Capsules Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            complianceSteps.forEachIndexed { index, step ->
                val isActive = activeStepIdx == index
                val isStepCompleted = step.tasks.all { task ->
                    val uid = "${selectedCommodity}_${selectedMarket}_${index}_${task.id}"
                    checkedTasks.contains(uid)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeStepIdx = index }
                        .testTag("step_tab_$index")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = step.icon,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = step.shortTitle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isStepCompleted) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active step header
        val currentStep = complianceSteps[activeStepIdx]
        Text(
            text = currentStep.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Interactive Checklist tasks list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            currentStep.tasks.forEach { task ->
                val uniqueId = "${selectedCommodity}_${selectedMarket}_${activeStepIdx}_${task.id}"
                val isChecked = checkedTasks.contains(uniqueId)
                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleTask(uniqueId) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("checkbox_${task.id}")
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isExpanded = !isExpanded }
                            ) {
                                Text(
                                    text = task.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = task.desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }

                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 44.dp, top = 8.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Cơ quan cấp phép: ${task.agency}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Thời hạn đề xuất: ${task.timeline}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // On-Demand Step Technical Advice Card (Gemini)
        val stepTitle = currentStep.title
        val stepTasks = currentStep.tasks.map { it.title }
        val adviceText = loadedStepAdvice[stepTitle]
        val isLoadingAdvice = isAdviceLoading[stepTitle] == true

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = "AI Advice",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cố Vấn Kỹ Thuật AI Cho Bước Này",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (adviceText == null && !isLoadingAdvice) {
                    Button(
                        onClick = { viewModel.fetchStepAdvice(stepTitle, stepTasks) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fetch_step_advice_button")
                    ) {
                        Text("Phân Tích Cố Vấn Kỹ Thuật (Gemini AI)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isLoadingAdvice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AgriGlobal AI đang nghiên cứu rào cản kỹ thuật...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (adviceText != null) {
                    Text(
                        text = adviceText,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Button(
                        onClick = { viewModel.fetchStepAdvice(stepTitle, stepTasks) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary).let {
                            ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cập nhật lại phân tích kỹ thuật", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Full Custom Regulations Report (Gemini API)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📊",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "BÁO CÁO PHÂN TÍCH RỦI RO & THUẾ QUAN CHI TIẾT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Text(
                    text = "Xuất báo cáo pháp lý đầy đủ bao gồm thông tin chi tiết về thuế nhập khẩu trực tiếp, rào cản kiểm dịch sâu hại động thực vật, quy chuẩn bao bì, nhãn mác của nước nhập khẩu.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (regAnalysisText.isEmpty() && !isRegAnalysisLoading) {
                    Button(
                        onClick = { viewModel.fetchRegulationsAnalysis() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fetch_full_analysis_button")
                    ) {
                        Text("Yêu cầu Gemini Xuất Báo Cáo Hải Quan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                    }
                } else if (isRegAnalysisLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Gemini đang tổng hợp dữ liệu thuế quan toàn cầu...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = regAnalysisText,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.fetchRegulationsAnalysis() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.tertiary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tải lại báo cáo mới", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MarketViewModel.ChatMessage) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    } else {
        RoundedCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    }

    val bubbleBg = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Sender Identity Tag
            Text(
                text = if (message.isUser) "Doanh nghiệp của bạn" else "Cố vấn AgriGlobal AI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (message.isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Actual Text Card Bubble
            Surface(
                shape = bubbleShape,
                color = bubbleBg,
                shadowElevation = if (message.isUser) 0.5.dp else 1.dp,
                modifier = Modifier.testTag(if (message.isUser) "user_bubble" else "ai_bubble")
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}
