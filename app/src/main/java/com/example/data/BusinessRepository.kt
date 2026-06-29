package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BusinessRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val invoiceDao = db.invoiceDao()
    private val invoiceItemDao = db.invoiceItemDao()
    private val expenseDao = db.expenseDao()

    // Products
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    // Customers
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Invoices
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    suspend fun getInvoiceById(id: Int): Invoice? = invoiceDao.getInvoiceById(id)
    fun getInvoiceItems(invoiceId: Int): Flow<List<InvoiceItem>> = invoiceItemDao.getItemsForInvoice(invoiceId)
    suspend fun getInvoiceItemsSync(invoiceId: Int): List<InvoiceItem> = invoiceItemDao.getItemsForInvoiceSync(invoiceId)

    // Create Invoice with items and update stock
    suspend fun createInvoice(invoice: Invoice, items: List<InvoiceItem>) = withContext(Dispatchers.IO) {
        val invoiceId = invoiceDao.insertInvoice(invoice).toInt()
        val itemsWithInvoiceId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceItemDao.insertInvoiceItems(itemsWithInvoiceId)
        
        // Subtract stock
        for (item in itemsWithInvoiceId) {
            productDao.reduceStock(item.productId, item.jumlah)
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) = withContext(Dispatchers.IO) {
        // Restore stock when an invoice is deleted
        val items = invoiceItemDao.getItemsForInvoiceSync(invoice.id)
        for (item in items) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                productDao.updateStock(item.productId, product.stok + item.jumlah)
            }
        }
        // Delete items and the invoice
        invoiceItemDao.deleteItemsForInvoice(invoice.id)
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun updateInvoiceStatus(invoice: Invoice, newStatus: String) = withContext(Dispatchers.IO) {
        invoiceDao.updateInvoice(invoice.copy(status = newStatus))
    }

    // Expenses
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.searchExpenses(query)
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // Prepopulate Indonesian mock data if database is empty
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val existingProducts = productDao.getAllProducts().firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            // 1. Insert Products
            val p1Id = productDao.insertProduct(Product(
                nama = "Laptop ThinkPad L14 Gen 3",
                deskripsi = "Laptop bisnis dengan RAM 16GB, SSD 512GB, dan prosesor Intel Core i5.",
                hargaBeli = 8500000.0,
                hargaJual = 11999000.0,
                stok = 14,
                stokMinimum = 3,
                kategori = "Elektronik"
            )).toInt()

            val p2Id = productDao.insertProduct(Product(
                nama = "Kertas HVS A4 Sinar Dunia 80gr",
                deskripsi = "Satu dus berisi 5 rim kertas putih kualitas tinggi.",
                hargaBeli = 210000.0,
                hargaJual = 265000.0,
                stok = 120,
                stokMinimum = 15,
                kategori = "Alat Tulis"
            )).toInt()

            val p3Id = productDao.insertProduct(Product(
                nama = "Kursi Kantor Ergonomis OfficeOne",
                deskripsi = "Kursi jaring dengan sandaran kepala dan penyangga pinggang yang dapat diatur.",
                hargaBeli = 750000.0,
                hargaJual = 1150000.0,
                stok = 9,
                stokMinimum = 2,
                kategori = "Furnitur"
            )).toInt()

            val p4Id = productDao.insertProduct(Product(
                nama = "Mouse Wireless Silent Logitech M221",
                deskripsi = "Mouse tanpa kabel dengan teknologi hening suara klik.",
                hargaBeli = 115000.0,
                hargaJual = 185000.0,
                stok = 3, // Low stock! Less than minimum
                stokMinimum = 10,
                kategori = "Elektronik"
            )).toInt()

            val p5Id = productDao.insertProduct(Product(
                nama = "Monitor IPS LG 24 Inch Full HD",
                deskripsi = "Monitor visual tajam dengan perlindungan mata flicker safe.",
                hargaBeli = 1200000.0,
                hargaJual = 1650000.0,
                stok = 8,
                stokMinimum = 2,
                kategori = "Elektronik"
            )).toInt()

            // 2. Insert Customers
            val c1Id = customerDao.insertCustomer(Customer(
                nama = "Fahmi Rahman",
                telepon = "081234567890",
                email = "fahmi@teknoindonesia.com",
                perusahaan = "PT Tekno Indonesia"
            )).toInt()

            val c2Id = customerDao.insertCustomer(Customer(
                nama = "Budi Santoso",
                telepon = "082187654321",
                email = "budi@indomart.co.id",
                perusahaan = "CV Indo Ritel Abadi"
            )).toInt()

            val c3Id = customerDao.insertCustomer(Customer(
                nama = "Siti Aminah",
                telepon = "085711223344",
                email = "siti.aminah@bumidesa.id",
                perusahaan = "Koperasi Bumi Desa Mandiri"
            )).toInt()

            // 3. Insert Expenses
            expenseDao.insertExpense(Expense(
                deskripsi = "Sewa Ruko Kantor Bulanan",
                jumlah = 3500000.0,
                kategori = "Sewa",
                tanggal = System.currentTimeMillis() - 25 * 24 * 60 * 60 * 1000L // 25 days ago
            ))

            expenseDao.insertExpense(Expense(
                deskripsi = "Gaji Karyawan & Staff Toko",
                jumlah = 6000000.0,
                kategori = "Gaji",
                tanggal = System.currentTimeMillis() - 20 * 24 * 60 * 60 * 1000L // 20 days ago
            ))

            expenseDao.insertExpense(Expense(
                deskripsi = "Biaya Listrik, Air, & Wifi",
                jumlah = 1150000.0,
                kategori = "Operasional",
                tanggal = System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L // 15 days ago
            ))

            expenseDao.insertExpense(Expense(
                deskripsi = "Iklan Facebook & Google Ads",
                jumlah = 1500000.0,
                kategori = "Pemasaran",
                tanggal = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L // 5 days ago
            ))

            // 4. Insert Invoices (Lunas & Belum Lunas)
            // Invoice 1: Lunas
            val inv1 = Invoice(
                customerId = c1Id,
                customerName = "Fahmi Rahman (PT Tekno Indonesia)",
                nomorFaktur = "INV/2026/001",
                tanggal = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L, // 10 days ago
                totalHarga = 12554000.0,
                status = "Lunas",
                jatuhTempo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L,
                catatan = "Pembayaran lunas via transfer bank Mandiri."
            )
            val inv1Id = invoiceDao.insertInvoice(inv1).toInt()
            invoiceItemDao.insertInvoiceItems(listOf(
                InvoiceItem(
                    invoiceId = inv1Id,
                    productId = p1Id,
                    namaProduk = "Laptop ThinkPad L14 Gen 3",
                    jumlah = 1,
                    hargaSatuan = 11999000.0,
                    totalHarga = 11999000.0
                ),
                InvoiceItem(
                    invoiceId = inv1Id,
                    productId = p4Id,
                    namaProduk = "Mouse Wireless Silent Logitech M221",
                    jumlah = 3,
                    hargaSatuan = 185000.0,
                    totalHarga = 555000.0
                )
            ))

            // Invoice 2: Belum Lunas
            val inv2 = Invoice(
                customerId = c2Id,
                customerName = "Budi Santoso (CV Indo Ritel Abadi)",
                nomorFaktur = "INV/2026/002",
                tanggal = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L, // 4 days ago
                totalHarga = 3450000.0,
                status = "Belum Lunas",
                jatuhTempo = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L, // 10 days in future
                catatan = "Pembayaran termin 14 hari."
            )
            val inv2Id = invoiceDao.insertInvoice(inv2).toInt()
            invoiceItemDao.insertInvoiceItems(listOf(
                InvoiceItem(
                    invoiceId = inv2Id,
                    productId = p3Id,
                    namaProduk = "Kursi Kantor Ergonomis OfficeOne",
                    jumlah = 3,
                    hargaSatuan = 1150000.0,
                    totalHarga = 3450000.0
                )
            ))
        }
    }
}
