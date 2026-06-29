package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val deskripsi: String,
    val hargaBeli: Double,
    val hargaJual: Double,
    val stok: Int,
    val stokMinimum: Int,
    val kategori: String
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val telepon: String,
    val email: String,
    val perusahaan: String,
    val tanggalGabung: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String, // Store snapshot of name for historic view
    val nomorFaktur: String,
    val tanggal: Long = System.currentTimeMillis(),
    val totalHarga: Double,
    val status: String, // "Lunas", "Belum Lunas", "Jatuh Tempo"
    val jatuhTempo: Long,
    val catatan: String
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val namaProduk: String,
    val jumlah: Int,
    val hargaSatuan: Double,
    val totalHarga: Double
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deskripsi: String,
    val jumlah: Double,
    val kategori: String, // "Sewa", "Gaji", "Operasional", "Pemasaran", "Lainnya"
    val tanggal: Long = System.currentTimeMillis()
)
