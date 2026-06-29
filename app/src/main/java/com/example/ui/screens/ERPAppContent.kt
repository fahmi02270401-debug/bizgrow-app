package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ERPAppContent(
    viewModel: BusinessViewModel,
    modifier: Modifier = Modifier
) {
    val activeScreen by viewModel.activeScreen.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    // Responsive: Navigation drawer for larger width, standard scaffold for compact.
    // For our app, we'll use a responsive layout with a beautiful ModalNavigationDrawer.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Brand Logo/Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "ERP Logo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ERP Bisnis SaaS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v1.0.0 Pro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp))

                // Navigation Items
                val navItems = listOf(
                    NavigationItem("Dasbor", Icons.Default.Dashboard, Screen.Dashboard, "nav_dashboard"),
                    NavigationItem("Stok Barang", Icons.Default.Inventory, Screen.Products, "nav_products"),
                    NavigationItem("Faktur / Penjualan", Icons.Default.ReceiptLong, Screen.Sales, "nav_sales"),
                    NavigationItem("Pengeluaran", Icons.Default.Payments, Screen.Expenses, "nav_expenses"),
                    NavigationItem("Pelanggan (CRM)", Icons.Default.People, Screen.Customers, "nav_customers"),
                    NavigationItem("Asisten AI Gemini", Icons.Default.Psychology, Screen.AIAssistant, "nav_ai")
                )

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontWeight = FontWeight.SemiBold) },
                        selected = activeScreen == item.screen,
                        onClick = {
                            viewModel.navigateTo(item.screen)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag(item.testTag),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                
                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Lisensi Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "fahmirahman022704@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (activeScreen) {
                                Screen.Dashboard -> "Dasbor Analisis"
                                Screen.Products -> "Manajemen Stok"
                                Screen.Sales -> "Faktur & Penjualan"
                                Screen.Expenses -> "Pelacak Pengeluaran"
                                Screen.Customers -> "Hubungan Pelanggan (CRM)"
                                Screen.AIAssistant -> "Asisten Bisnis Gemini AI"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Navigasi")
                        }
                    },
                    actions = {
                        // Show AI Quick access button in other screens
                        if (activeScreen != Screen.AIAssistant) {
                            IconButton(
                                onClick = { viewModel.navigateTo(Screen.AIAssistant) },
                                modifier = Modifier.testTag("action_ai_shortcut")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Tanya AI",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                // Responsive bottom bar for simple accessibility on compact phones
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dasbor", fontSize = 11.sp) },
                        selected = activeScreen == Screen.Dashboard,
                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                        modifier = Modifier.testTag("bottom_nav_dashboard")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Inventory, contentDescription = "Stok") },
                        label = { Text("Stok", fontSize = 11.sp) },
                        selected = activeScreen == Screen.Products,
                        onClick = { viewModel.navigateTo(Screen.Products) },
                        modifier = Modifier.testTag("bottom_nav_products")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Faktur") },
                        label = { Text("Faktur", fontSize = 11.sp) },
                        selected = activeScreen == Screen.Sales,
                        onClick = { viewModel.navigateTo(Screen.Sales) },
                        modifier = Modifier.testTag("bottom_nav_sales")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI") },
                        label = { Text("Gemini AI", fontSize = 11.sp) },
                        selected = activeScreen == Screen.AIAssistant,
                        onClick = { viewModel.navigateTo(Screen.AIAssistant) },
                        modifier = Modifier.testTag("bottom_nav_ai")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Main screen transitions
                AnimatedContent(
                    targetState = activeScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(180))
                    },
                    label = "screen_transitions"
                ) { targetScreen ->
                    when (targetScreen) {
                        Screen.Dashboard -> DashboardScreen(viewModel, products, invoices, expenses, customers)
                        Screen.Products -> ProductsScreen(viewModel, products)
                        Screen.Sales -> SalesScreen(viewModel, invoices, products, customers)
                        Screen.Expenses -> ExpensesScreen(viewModel, expenses)
                        Screen.Customers -> CustomersScreen(viewModel, customers)
                        Screen.AIAssistant -> AIAssistantScreen(viewModel)
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
    val testTag: String
)

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: BusinessViewModel,
    products: List<Product>,
    invoices: List<Invoice>,
    expenses: List<Expense>,
    customers: List<Customer>
) {
    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)

    // Calculate core financial totals
    val totalRevenue = invoices.sumOf { it.totalHarga }
    val paidRevenue = invoices.filter { it.status == "Lunas" }.sumOf { it.totalHarga }
    val unpaidRevenue = invoices.filter { it.status == "Belum Lunas" }.sumOf { it.totalHarga }
    val totalExpenses = expenses.sumOf { it.jumlah }
    val netProfit = totalRevenue - totalExpenses

    val lowStockCount = products.count { it.stok <= it.stokMinimum }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick welcome card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selamat Datang Kembali!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Berikut ringkasan performa bisnis ritel & operasional SaaS Anda saat ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Powered",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Summary Statistics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Omset Penjualan",
                value = currencyFormatter.format(totalRevenue),
                icon = Icons.Default.TrendingUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Laba Bersih",
                value = currencyFormatter.format(netProfit),
                icon = Icons.Default.Savings,
                color = if (netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Pengeluaran",
                value = currencyFormatter.format(totalExpenses),
                icon = Icons.Default.Payments,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Piutang Penjualan",
                value = currencyFormatter.format(unpaidRevenue),
                icon = Icons.Default.HourglassEmpty,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }

        // Low Stock Notifications if any
        if (lowStockCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.Products) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ada $lowStockCount Barang Hampir Habis!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Ketuk di sini untuk melihat produk dan merencanakan pemesanan ulang (restock).",
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Detail",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Financial Canvas Visualization: Visualizing Revenue vs Expenses Bar chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Visual Perbandingan Keuangan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Let's draw a nice interactive custom progress comparative bar
                val maxVal = maxOf(totalRevenue, totalExpenses, 1.0)
                val revRatio = (totalRevenue / maxVal).toFloat()
                val expRatio = (totalExpenses / maxVal).toFloat()

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Revenue bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Pendapatan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormatter.format(totalRevenue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(revRatio)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    // Expenses bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Pengeluaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormatter.format(totalExpenses), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(expRatio)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pendapatan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pengeluaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Quick AI Insights banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Asisten Rekomendasi AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Dapatkan analisis mendalam, peramalan stok barang, dan perbaikan margin keuntungan menggunakan kecerdasan buatan Gemini AI.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { viewModel.navigateTo(Screen.AIAssistant) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("dashboard_ask_ai_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Tanya AI Sekarang", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 2. PRODUCTS SCREEN (INVENTORY)
// ==========================================
@Composable
fun ProductsScreen(
    viewModel: BusinessViewModel,
    products: List<Product>
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    
    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)

    LaunchedEffect(searchQuery) {
        viewModel.productSearchQuery.value = searchQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("products_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search and Add layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari produk / kategori...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("product_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }

        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada produk ditemukan.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    val isLowStock = product.stok <= product.stokMinimum
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingProduct = product },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = if (isLowStock) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.nama,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = product.kategori,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Stock Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isLowStock) MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.primaryContainer
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Stok: ${product.stok}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.deskripsi,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Harga Beli: ${currencyFormatter.format(product.hargaBeli)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Harga Jual: ${currencyFormatter.format(product.hargaJual)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { editingProduct = product },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProduct(product) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
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

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "Tambah Produk Baru",
            onDismiss = { showAddDialog = false },
            onSubmit = { nama, deskripsi, hBeli, hJual, stok, stokMin, kategori ->
                viewModel.addProduct(nama, deskripsi, hBeli, hJual, stok, stokMin, kategori)
                showAddDialog = false
            }
        )
    }

    // Edit Product Dialog
    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Edit Produk",
            product = product,
            onDismiss = { editingProduct = null },
            onSubmit = { nama, deskripsi, hBeli, hJual, stok, stokMin, kategori ->
                viewModel.updateProduct(product.copy(
                    nama = nama,
                    deskripsi = deskripsi,
                    hargaBeli = hBeli,
                    hargaJual = hJual,
                    stok = stok,
                    stokMinimum = stokMin,
                    kategori = kategori
                ))
                editingProduct = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    title: String,
    product: Product? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, Double, Int, Int, String) -> Unit
) {
    var nama by remember { mutableStateOf(product?.nama ?: "") }
    var deskripsi by remember { mutableStateOf(product?.deskripsi ?: "") }
    var hargaBeli by remember { mutableStateOf(product?.hargaBeli?.toString() ?: "") }
    var hargaJual by remember { mutableStateOf(product?.hargaJual?.toString() ?: "") }
    var stok by remember { mutableStateOf(product?.stok?.toString() ?: "") }
    var stokMin by remember { mutableStateOf(product?.stokMinimum?.toString() ?: "") }
    var kategori by remember { mutableStateOf(product?.kategori ?: "Elektronik") }

    val categories = listOf("Elektronik", "Alat Tulis", "Furnitur", "Konsumsi", "Fashion", "Lainnya")
    var catExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("product_form_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Produk") },
                    modifier = Modifier.fillMaxWidth().testTag("form_product_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hargaBeli,
                        onValueChange = { hargaBeli = it },
                        label = { Text("Harga Beli (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hargaJual,
                        onValueChange = { hargaJual = it },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stok,
                        onValueChange = { stok = it },
                        label = { Text("Stok") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stokMin,
                        onValueChange = { stokMin = it },
                        label = { Text("Stok Minimal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = {},
                        label = { Text("Kategori") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { catExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    kategori = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hBeli = hargaBeli.toDoubleOrNull() ?: 0.0
                            val hJual = hargaJual.toDoubleOrNull() ?: 0.0
                            val stk = stok.toIntOrNull() ?: 0
                            val sMin = stokMin.toIntOrNull() ?: 0
                            if (nama.isNotEmpty()) {
                                onSubmit(nama, deskripsi, hBeli, hJual, stk, sMin, kategori)
                            }
                        },
                        modifier = Modifier.testTag("submit_product_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. SALES / INVOICES SCREEN
// ==========================================
@Composable
fun SalesScreen(
    viewModel: BusinessViewModel,
    invoices: List<Invoice>,
    products: List<Product>,
    customers: List<Customer>
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedInvoiceForDetail by remember { mutableStateOf<Invoice?>(null) }
    
    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", idLocale)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("sales_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row to create Invoice
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat Faktur Terbit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(
                onClick = {
                    viewModel.clearCart()
                    showCreateDialog = true
                },
                modifier = Modifier.testTag("create_invoice_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buat Faktur")
            }
        }

        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada transaksi faktur yang tercatat.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(invoices) { invoice ->
                    val isPaid = invoice.status == "Lunas"
                    val isOverdue = System.currentTimeMillis() > invoice.jatuhTempo && !isPaid

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedInvoiceForDetail = invoice },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = invoice.nomorFaktur,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(invoice.tanggal)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Status Tag
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isPaid -> Color(0xFFD1FAE5)
                                                isOverdue -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFFEF3C7)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isPaid) "Lunas" else if (isOverdue) "Jatuh Tempo" else "Belum Lunas",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isPaid -> Color(0xFF065F46)
                                            isOverdue -> Color(0xFF991B1B)
                                            else -> Color(0xFF92400E)
                                        }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Pelanggan: ${invoice.customerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Faktur",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyFormatter.format(invoice.totalHarga),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Quick toggle status button
                                    Button(
                                        onClick = {
                                            val newStatus = if (isPaid) "Belum Lunas" else "Lunas"
                                            viewModel.updateInvoiceStatus(invoice, newStatus)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPaid) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.height(36.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text(if (isPaid) "Ubah Belum Lunas" else "Tandai Lunas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    IconButton(
                                        onClick = { viewModel.deleteInvoice(invoice) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Detail Dialog
    selectedInvoiceForDetail?.let { invoice ->
        InvoiceDetailDialog(
            invoice = invoice,
            viewModel = viewModel,
            onDismiss = { selectedInvoiceForDetail = null }
        )
    }

    // Create Invoice Full Dialog
    if (showCreateDialog) {
        CreateInvoiceDialog(
            viewModel = viewModel,
            products = products,
            customers = customers,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailDialog(
    invoice: Invoice,
    viewModel: BusinessViewModel,
    onDismiss: () -> Unit
) {
    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", idLocale)
    val dueDateFormatter = SimpleDateFormat("dd MMMM yyyy", idLocale)

    // Collect elements associated with this invoice
    var items by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
    LaunchedEffect(invoice.id) {
        viewModel.getInvoiceItems(invoice.id).collectLatest {
            items = it
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("invoice_detail_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rincian Faktur",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Invoice Header metadata
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = invoice.nomorFaktur,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dibuat: ${dateFormatter.format(Date(invoice.tanggal))}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Jatuh Tempo: ${dueDateFormatter.format(Date(invoice.jatuhTempo))}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Status: ${invoice.status}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.status == "Lunas") Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }

                // Customer Snapshot Info
                Column {
                    Text("Pelanggan & Perusahaan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        invoice.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                // Items list header
                Text("Daftar Item", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.namaProduk, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${item.jumlah} x ${currencyFormatter.format(item.hargaSatuan)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            currencyFormatter.format(item.totalHarga),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        currencyFormatter.format(invoice.totalHarga),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (invoice.catatan.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text("Catatan Faktur:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text(invoice.catatan, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceDialog(
    viewModel: BusinessViewModel,
    products: List<Product>,
    customers: List<Customer>,
    onDismiss: () -> Unit
) {
    val selectedCustomer by viewModel.selectedCustomerForInvoice.collectAsState()
    val notes by viewModel.invoiceNotes.collectAsState()
    val dueDays by viewModel.invoiceDueDateDays.collectAsState()
    val cart = viewModel.invoiceCart

    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)

    var customerDropdownExpanded by remember { mutableStateOf(false) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("create_invoice_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Buat Faktur Penjualan Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 1. SELECT CUSTOMER
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCustomer?.let { "${it.nama} (${it.perusahaan})" } ?: "Pilih Pelanggan...",
                        onValueChange = {},
                        label = { Text("Pelanggan (Wajib)") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { customerDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("select_customer_dropdown")
                    )
                    DropdownMenu(
                        expanded = customerDropdownExpanded,
                        onDismissRequest = { customerDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (customers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Belum ada pelanggan terdaftar.") },
                                onClick = { customerDropdownExpanded = false }
                            )
                        } else {
                            customers.forEach { customer ->
                                DropdownMenuItem(
                                    text = { Text("${customer.nama} - ${customer.perusahaan}") },
                                    onClick = {
                                        viewModel.selectCustomerForInvoice(customer)
                                        customerDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. ADD PRODUCT TO INVOICE CART
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { productDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambahkan Item Produk")
                    }
                    DropdownMenu(
                        expanded = productDropdownExpanded,
                        onDismissRequest = { productDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text("${product.nama} (Stok: ${product.stok} | Jual: ${currencyFormatter.format(product.hargaJual)})") },
                                onClick = {
                                    if (product.stok > 0) {
                                        viewModel.addProductToCart(product, 1)
                                    }
                                    productDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 3. CART DISPLAY
                if (cart.isNotEmpty()) {
                    Text("Daftar Belanja Faktur", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    cart.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.nama, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(item.product.hargaJual), style = MaterialTheme.typography.bodySmall)
                            }
                            
                            // Quantity Controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (item.quantity > 1) {
                                            viewModel.addProductToCart(item.product, -1)
                                        } else {
                                            viewModel.removeProductFromCart(item.product)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurang")
                                }
                                Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = {
                                        if (item.quantity < item.product.stok) {
                                            viewModel.addProductToCart(item.product, 1)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah")
                                }
                            }
                        }
                    }

                    // Cart Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontWeight = FontWeight.Bold)
                        Text(currencyFormatter.format(viewModel.getCartTotal()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // 4. TERMS AND NOTES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dueDays.toString(),
                        onValueChange = { viewModel.updateInvoiceDueDateDays(it.toIntOrNull() ?: 14) },
                        label = { Text("Jatuh Tempo (Hari)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.updateInvoiceNotes(it) },
                    label = { Text("Catatan Faktur / Syarat Pembayaran") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.checkoutInvoice {
                                onDismiss()
                            }
                        },
                        enabled = selectedCustomer != null && cart.isNotEmpty(),
                        modifier = Modifier.testTag("submit_checkout_invoice_btn")
                    ) {
                        Text("Terbitkan Faktur")
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. EXPENSES SCREEN (FINANCIAL TRACKER)
// ==========================================
@Composable
fun ExpensesScreen(
    viewModel: BusinessViewModel,
    expenses: List<Expense>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val idLocale = Locale("in", "ID")
    val currencyFormatter = NumberFormat.getCurrencyInstance(idLocale)
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", idLocale)

    LaunchedEffect(searchQuery) {
        viewModel.expenseSearchQuery.value = searchQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("expenses_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top search & FAB row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari pengeluaran...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("expense_search_input"),
                singleLine = true
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengeluaran")
            }
        }

        // Summary box of search filter expenses
        val totalFilteredExpense = expenses.sumOf { it.jumlah }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Pengeluaran", fontWeight = FontWeight.Bold)
                Text(
                    currencyFormatter.format(totalFilteredExpense),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada catatan pengeluaran.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = expense.deskripsi,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = expense.kategori,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(expense.tanggal)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormatter.format(expense.jumlah),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteExpense(expense) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        ExpenseAddDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { deskripsi, jumlah, kategori ->
                viewModel.addExpense(deskripsi, jumlah, kategori)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseAddDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String) -> Unit
) {
    var deskripsi by remember { mutableStateOf("") }
    var jumlah by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Operasional") }

    val categories = listOf("Sewa", "Gaji", "Operasional", "Pemasaran", "Lainnya")
    var catExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("expense_form_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah Pengeluaran Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi Pengeluaran") },
                    modifier = Modifier.fillMaxWidth().testTag("form_expense_desc"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { jumlah = it },
                    label = { Text("Jumlah (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("form_expense_amount"),
                    singleLine = true
                )

                // Category Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = kategori,
                        onValueChange = {},
                        label = { Text("Kategori") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { catExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    kategori = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val jml = jumlah.toDoubleOrNull() ?: 0.0
                            if (deskripsi.isNotEmpty() && jml > 0) {
                                onSubmit(deskripsi, jml, kategori)
                            }
                        },
                        modifier = Modifier.testTag("submit_expense_btn")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CUSTOMERS SCREEN (CRM)
// ==========================================
@Composable
fun CustomersScreen(
    viewModel: BusinessViewModel,
    customers: List<Customer>
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(searchQuery) {
        viewModel.customerSearchQuery.value = searchQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("customers_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari pelanggan / perusahaan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("customer_search_input"),
                singleLine = true
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pelanggan")
            }
        }

        if (customers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Kosong",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada pelanggan terdaftar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customers) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingCustomer = customer },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = customer.nama,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = customer.perusahaan,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row {
                                    IconButton(onClick = { editingCustomer = customer }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteCustomer(customer) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(customer.telepon, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(customer.email, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add customer Dialog
    if (showAddDialog) {
        CustomerFormDialog(
            title = "Tambah Pelanggan Baru",
            onDismiss = { showAddDialog = false },
            onSubmit = { nama, telp, email, perusahaan ->
                viewModel.addCustomer(nama, telp, email, perusahaan)
                showAddDialog = false
            }
        )
    }

    // Edit customer Dialog
    editingCustomer?.let { customer ->
        CustomerFormDialog(
            title = "Edit Rincian Pelanggan",
            customer = customer,
            onDismiss = { editingCustomer = null },
            onSubmit = { nama, telp, email, perusahaan ->
                viewModel.updateCustomer(customer.copy(
                    nama = nama,
                    telepon = telp,
                    email = email,
                    perusahaan = perusahaan
                ))
                editingCustomer = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormDialog(
    title: String,
    customer: Customer? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var nama by remember { mutableStateOf(customer?.nama ?: "") }
    var telepon by remember { mutableStateOf(customer?.telepon ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var perusahaan by remember { mutableStateOf(customer?.perusahaan ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("customer_form_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth().testTag("form_customer_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = perusahaan,
                    onValueChange = { perusahaan = it },
                    label = { Text("Perusahaan / Bisnis") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = telepon,
                    onValueChange = { telepon = it },
                    label = { Text("Nomor Telepon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Alamat Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotEmpty()) {
                                onSubmit(nama, telepon, email, perusahaan)
                            }
                        },
                        modifier = Modifier.testTag("submit_customer_btn")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Icon helper function
@Composable
fun Icon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Box(modifier = Modifier.size(size)) {
        Icon(imageVector = imageVector, contentDescription = contentDescription, tint = tint)
    }
}

// ==========================================
// 6. ASISTEN AI GEMINI SCREEN
// ==========================================
@Composable
fun AIAssistantScreen(
    viewModel: BusinessViewModel
) {
    val aiReportState by viewModel.aiReportState.collectAsState()
    val isChatbotLoading by viewModel.isChatbotLoading.collectAsState()
    val chatMessages = viewModel.chatbotMessages

    var activeTab by remember { mutableStateOf(0) } // 0: Financial Report Analyzer, 1: Interactive AI CRM Chatbot
    var chatInputText by remember { mutableStateOf("") }
    
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Scroll chatbot to bottom when a new message is received
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ai_assistant_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Segment Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Analisis Bisnis", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Tanya AI Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ChatBubble, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            // Tab 0: Comprehensive Business Analyzer
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 28.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analis Keuangan & Stok Otomatis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ketuk tombol di bawah untuk meminta AI Gemini menganalisis seluruh data inventaris toko, sisa kas, pengeluaran operasional, dan faktur penjualan. AI akan merumuskan perbaikan strategi secara instan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.generateAIBusinessReport() },
                            modifier = Modifier.fillMaxWidth().testTag("trigger_ai_report_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hasilkan Laporan Analisis")
                        }
                    }
                }

                when (val state = aiReportState) {
                    is AIReportState.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada laporan yang dihasilkan. Tekan tombol di atas.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is AIReportState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Gemini sedang menganalisis performa keuangan bisnis Anda...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    is AIReportState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hasil Analisis Bisnis (Gemini AI)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.generateAIBusinessReport() }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                // Report content supporting formatting structure simply
                                Text(
                                    text = state.report,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                    is AIReportState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: CRM/ERP Interactive Chatbot
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Ajukan pertanyaan seperti: \"Bagaimana laba bersih saya dibandingkan pengeluaran?\" atau \"Kapan jatuh tempo faktur berikutnya?\" AI mengetahui database ERP secara langsung.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chat Messages Bubble List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chatMessages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, size = 48.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Mulai diskusi bisnis Anda. Sapa AI dengan: \"Halo!\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(chatMessages) { msg ->
                            val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                            val containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = alignment
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                                bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(containerColor)
                                        .padding(12.dp)
                                        .widthIn(max = 280.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    if (isChatbotLoading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini sedang berpikir...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Input bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInputText,
                        onValueChange = { chatInputText = it },
                        placeholder = { Text("Tanya asisten bisnis...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        singleLine = true,
                        trailingIcon = {
                            if (chatInputText.isNotEmpty()) {
                                IconButton(onClick = { chatInputText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    
                    IconButton(
                        onClick = {
                            if (chatInputText.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(chatInputText)
                                chatInputText = ""
                            }
                        },
                        enabled = chatInputText.trim().isNotEmpty() && !isChatbotLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (chatInputText.trim().isNotEmpty()) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("send_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Kirim",
                            tint = if (chatInputText.trim().isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
