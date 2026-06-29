package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BusinessViewModel(application: Application, private val repository: BusinessRepository) : AndroidViewModel(application) {

    // Current Active Screen state
    private val _activeScreen = MutableStateFlow(Screen.Dashboard)
    val activeScreen: StateFlow<Screen> = _activeScreen.asStateFlow()

    // Navigation function
    fun navigateTo(screen: Screen) {
        _activeScreen.value = screen
    }

    // Search queries
    val productSearchQuery = MutableStateFlow("")
    val customerSearchQuery = MutableStateFlow("")
    val expenseSearchQuery = MutableStateFlow("")

    // Room DB Flows
    val products: StateFlow<List<Product>> = productSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getAllProducts() else repository.searchProducts(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = customerSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getAllCustomers() else repository.searchCustomers(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = expenseSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) repository.getAllExpenses() else repository.searchExpenses(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Invoice Cart (For creating new invoices)
    val invoiceCart = mutableStateListOf<CartItem>()
    private val _selectedCustomerForInvoice = MutableStateFlow<Customer?>(null)
    val selectedCustomerForInvoice: StateFlow<Customer?> = _selectedCustomerForInvoice.asStateFlow()
    
    private val _invoiceNotes = MutableStateFlow("")
    val invoiceNotes: StateFlow<String> = _invoiceNotes.asStateFlow()

    private val _invoiceDueDateDays = MutableStateFlow(14) // default 14 days payment terms
    val invoiceDueDateDays: StateFlow<Int> = _invoiceDueDateDays.asStateFlow()

    // AI Analysis State
    private val _aiReportState = MutableStateFlow<AIReportState>(AIReportState.Idle)
    val aiReportState: StateFlow<AIReportState> = _aiReportState.asStateFlow()

    // Interactive chatbot states
    val chatbotMessages = mutableStateListOf<ChatMessage>()
    private val _isChatbotLoading = MutableStateFlow(false)
    val isChatbotLoading: StateFlow<Boolean> = _isChatbotLoading.asStateFlow()

    init {
        // Prepopulate database if empty and load
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Product Actions
    fun addProduct(nama: String, deskripsi: String, hargaBeli: Double, hargaJual: Double, stok: Int, stokMinimum: Int, kategori: String) {
        viewModelScope.launch {
            repository.insertProduct(Product(
                nama = nama,
                deskripsi = deskripsi,
                hargaBeli = hargaBeli,
                hargaJual = hargaJual,
                stok = stok,
                stokMinimum = stokMinimum,
                kategori = kategori
            ))
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // Customer Actions
    fun addCustomer(nama: String, telepon: String, email: String, perusahaan: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(
                nama = nama,
                telepon = telepon,
                email = email,
                perusahaan = perusahaan
            ))
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Expense Actions
    fun addExpense(deskripsi: String, jumlah: Double, kategori: String) {
        viewModelScope.launch {
            repository.insertExpense(Expense(
                deskripsi = deskripsi,
                jumlah = jumlah,
                kategori = kategori,
                tanggal = System.currentTimeMillis()
            ))
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Invoice Cart Management
    fun selectCustomerForInvoice(customer: Customer?) {
        _selectedCustomerForInvoice.value = customer
    }

    fun updateInvoiceNotes(notes: String) {
        _invoiceNotes.value = notes
    }

    fun updateInvoiceDueDateDays(days: Int) {
        _invoiceDueDateDays.value = days
    }

    fun addProductToCart(product: Product, quantity: Int) {
        val existing = invoiceCart.find { it.product.id == product.id }
        if (existing != null) {
            val idx = invoiceCart.indexOf(existing)
            invoiceCart[idx] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            invoiceCart.add(CartItem(product, quantity))
        }
    }

    fun removeProductFromCart(product: Product) {
        invoiceCart.removeAll { it.product.id == product.id }
    }

    fun clearCart() {
        invoiceCart.clear()
        _selectedCustomerForInvoice.value = null
        _invoiceNotes.value = ""
        _invoiceDueDateDays.value = 14
    }

    fun getCartTotal(): Double {
        return invoiceCart.sumOf { it.product.hargaJual * it.quantity }
    }

    // Submit invoice to DB
    fun checkoutInvoice(onSuccess: () -> Unit) {
        val customer = _selectedCustomerForInvoice.value ?: return
        if (invoiceCart.isEmpty()) return

        viewModelScope.launch {
            val total = getCartTotal()
            val formatter = SimpleDateFormat("yyyyMMdd/HHmmss", Locale.getDefault())
            val invoiceNo = "INV/" + formatter.format(Date())

            val dueDateMillis = System.currentTimeMillis() + (_invoiceDueDateDays.value * 24 * 60 * 60 * 1000L)

            val invoice = Invoice(
                customerId = customer.id,
                customerName = "${customer.nama} (${customer.perusahaan})",
                nomorFaktur = invoiceNo,
                tanggal = System.currentTimeMillis(),
                totalHarga = total,
                status = "Belum Lunas", // Always starts unpaid
                jatuhTempo = dueDateMillis,
                catatan = _invoiceNotes.value
            )

            val items = invoiceCart.map { cart ->
                InvoiceItem(
                    invoiceId = 0, // Set dynamically by repo transaction
                    productId = cart.product.id,
                    namaProduk = cart.product.nama,
                    jumlah = cart.quantity,
                    hargaSatuan = cart.product.hargaJual,
                    totalHarga = cart.product.hargaJual * cart.quantity
                )
            }

            repository.createInvoice(invoice, items)
            clearCart()
            onSuccess()
        }
    }

    fun updateInvoiceStatus(invoice: Invoice, newStatus: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(invoice, newStatus)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    fun getInvoiceItems(invoiceId: Int): Flow<List<InvoiceItem>> {
        return repository.getInvoiceItems(invoiceId)
    }

    // Compile Business Stats for AI Prompt
    private fun compileStatsSummary(): String {
        val currentProducts = products.value
        val currentInvoices = invoices.value
        val currentExpenses = expenses.value
        val currentCustomers = customers.value

        val totalRevenue = currentInvoices.sumOf { it.totalHarga }
        val paidRevenue = currentInvoices.filter { it.status == "Lunas" }.sumOf { it.totalHarga }
        val unpaidRevenue = currentInvoices.filter { it.status == "Belum Lunas" }.sumOf { it.totalHarga }
        val totalExpenses = currentExpenses.sumOf { it.jumlah }
        val netProfit = totalRevenue - totalExpenses

        val lowStockProducts = currentProducts.filter { it.stok <= it.stokMinimum }
        val lowStockText = if (lowStockProducts.isEmpty()) "Tidak ada (Semua aman)" else lowStockProducts.joinToString("\n") { 
            "- ${it.nama}: Sisa stok ${it.stok} (Min: ${it.stokMinimum})"
        }

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        return """
            --- RINGKASAN FINANSIAL ---
            - Total Pendapatan Kotor (Omset): ${currencyFormat.format(totalRevenue)}
            - Pendapatan Terbayar (Lunas): ${currencyFormat.format(paidRevenue)}
            - Piutang Usaha (Belum Lunas): ${currencyFormat.format(unpaidRevenue)}
            - Total Pengeluaran Operasional: ${currencyFormat.format(totalExpenses)}
            - Estimasi Laba Bersih: ${currencyFormat.format(netProfit)}

            --- OPERASIONAL & INVENTARIS ---
            - Jumlah Pelanggan Terdaftar: ${currentCustomers.size} orang
            - Jumlah Produk Aktif: ${currentProducts.size} item
            - Transaksi Faktur Terbit: ${currentInvoices.size} transaksi
            - Produk Butuh Restock Segera (Stok Rendah):
            $lowStockText

            --- PRODUK TERLARIS & DETAIL INVENTARIS ---
            ${currentProducts.joinToString("\n") { "- ${it.nama}: Stok ${it.stok} | Harga Jual ${currencyFormat.format(it.hargaJual)} | Kategori: ${it.kategori}" }}
        """.trimIndent()
    }

    // AI Report Analyzer trigger
    fun generateAIBusinessReport() {
        _aiReportState.value = AIReportState.Loading
        viewModelScope.launch {
            try {
                val stats = compileStatsSummary()
                val response = GeminiService.generateBusinessAnalysis(stats)
                _aiReportState.value = AIReportState.Success(response)
            } catch (e: Exception) {
                _aiReportState.value = AIReportState.Error(e.localizedMessage ?: "Terjadi kesalahan tidak dikenal.")
            }
        }
    }

    // Chatbot send message
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        
        chatbotMessages.add(ChatMessage(text, isUser = true))
        _isChatbotLoading.value = true

        viewModelScope.launch {
            try {
                val stats = compileStatsSummary()
                val contextPrompt = "Pertanyaan pemilik bisnis: \"$text\"\n\nJawablah dengan merujuk pada data bisnis riil di atas jika relevan, berikan solusi ramah dan konkret."
                val response = GeminiService.generateBusinessAnalysis(stats, contextPrompt)
                
                chatbotMessages.add(ChatMessage(response, isUser = false))
            } catch (e: Exception) {
                chatbotMessages.add(ChatMessage("Maaf, terjadi kesalahan saat memproses jawaban: ${e.localizedMessage}", isUser = false))
            } finally {
                _isChatbotLoading.value = false
            }
        }
    }
}

// Helper state classes
enum class Screen {
    Dashboard, Products, Sales, Expenses, Customers, AIAssistant
}

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class AIReportState {
    object Idle : AIReportState()
    object Loading : AIReportState()
    data class Success(val report: String) : AIReportState()
    data class Error(val message: String) : AIReportState()
}

// Factory Pattern
class ViewModelFactory(private val application: Application, private val repository: BusinessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusinessViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
