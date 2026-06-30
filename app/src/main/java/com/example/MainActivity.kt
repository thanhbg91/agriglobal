package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.repository.ProductRepository
import com.example.ui.screens.AddProductScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.ConsultingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.AuthScreen
import com.example.api.AuthManager
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MarketViewModel

sealed class Screen {
    object Market : Screen()
    data class ProductDetail(val productId: Int) : Screen()
    object AddProduct : Screen()
    object Consulting : Screen()
    object Cart : Screen()
    object Profile : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize database, repository, and view model using local compose context
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context)
                val repository = ProductRepository(database.productDao())
                val app = context.applicationContext as android.app.Application
                val marketViewModel: MarketViewModel = viewModel(
                    factory = MarketViewModel.Factory(app, repository)
                )

                var isUserLoggedIn by remember { mutableStateOf(AuthManager.isUserLoggedIn(context)) }

                if (!isUserLoggedIn) {
                    AuthScreen(
                        onAuthSuccess = {
                            isUserLoggedIn = true
                        }
                    )
                } else {
                    // Navigation backstack
                    val backstack = remember { mutableStateListOf<Screen>(Screen.Market) }
                    val currentScreen = backstack.last()

                    // Dynamic Hardware Back Button Press Handler
                    BackHandler(enabled = backstack.size > 1) {
                        backstack.removeAt(backstack.size - 1)
                    }

                    Scaffold(
                        bottomBar = {
                            // Display bottom navigation only on primary root screens
                            if (currentScreen == Screen.Market || 
                                currentScreen == Screen.Consulting || 
                                currentScreen == Screen.Cart || 
                                currentScreen == Screen.Profile
                            ) {
                                NavigationBar {
                                    // Market Tab
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Market,
                                        onClick = {
                                            if (currentScreen != Screen.Market) {
                                                backstack.clear()
                                                backstack.add(Screen.Market)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Chợ") },
                                        label = { Text("Chợ Nông Sản", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_market")
                                    )

                                    // Consulting Chat Tab
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Consulting,
                                        onClick = {
                                            if (currentScreen != Screen.Consulting) {
                                                backstack.clear()
                                                backstack.add(Screen.Consulting)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "Cố vấn AI") },
                                        label = { Text("Cố Vấn AI", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_consulting")
                                    )

                                    // Cart Tab
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Cart,
                                        onClick = {
                                            if (currentScreen != Screen.Cart) {
                                                backstack.clear()
                                                backstack.add(Screen.Cart)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Đàm phán") },
                                        label = { Text("Đàm Phán", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_cart")
                                    )

                                    // Profile Tab
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Profile,
                                        onClick = {
                                            if (currentScreen != Screen.Profile) {
                                                backstack.clear()
                                                backstack.add(Screen.Profile)
                                            }
                                        },
                                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Của tôi") },
                                        label = { Text("Của Tôi", fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_tab_profile")
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                is Screen.Market -> {
                                    HomeScreen(
                                        viewModel = marketViewModel,
                                        onProductClick = { product ->
                                            backstack.add(Screen.ProductDetail(product.id))
                                        },
                                        onAddProductClick = {
                                            backstack.add(Screen.AddProduct)
                                        }
                                    )
                                }
                                is Screen.ProductDetail -> {
                                    ProductDetailScreen(
                                        productId = currentScreen.productId,
                                        viewModel = marketViewModel,
                                        onBackClick = {
                                            backstack.removeAt(backstack.size - 1)
                                        }
                                    )
                                }
                                is Screen.AddProduct -> {
                                    AddProductScreen(
                                        viewModel = marketViewModel,
                                        onBackClick = {
                                            backstack.removeAt(backstack.size - 1)
                                        }
                                    )
                                }
                                is Screen.Consulting -> {
                                    ConsultingScreen(
                                        viewModel = marketViewModel
                                    )
                                }
                                is Screen.Cart -> {
                                    CartScreen(
                                        viewModel = marketViewModel
                                    )
                                }
                                is Screen.Profile -> {
                                    ProfileScreen(
                                        viewModel = marketViewModel,
                                        onProductClick = { product ->
                                            backstack.add(Screen.ProductDetail(product.id))
                                        },
                                        onLogoutClick = {
                                            AuthManager.logout(context)
                                            isUserLoggedIn = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
