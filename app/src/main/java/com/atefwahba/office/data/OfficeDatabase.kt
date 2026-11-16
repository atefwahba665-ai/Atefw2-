package com.atefwahba.office.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val nationalId: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "عام",
    val salePricePiasters: Long,
    val costPricePiasters: Long = 0,
    val stockQuantity: Int = 0,
    val isAvailable: Boolean = true
)

@Entity(tableName = "transactions")
data class ServiceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val serviceName: String,
    val amountPiasters: Long,
    val costPiasters: Long = 0,
    val status: String = "جديدة",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amountPiasters: Long,
    val paymentType: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface OfficeDao {
    @Query("SELECT * FROM customers ORDER BY id DESC") fun observeCustomers(): Flow<List<Customer>>
    @Query("SELECT * FROM products ORDER BY id DESC") fun observeProducts(): Flow<List<Product>>
    @Query("SELECT * FROM transactions ORDER BY id DESC") fun observeTransactions(): Flow<List<ServiceTransaction>>
    @Query("SELECT * FROM payments ORDER BY id DESC") fun observePayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addCustomer(customer: Customer)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addProduct(product: Product)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addTransaction(transaction: ServiceTransaction)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addPayment(payment: Payment)

    @Query("SELECT COUNT(*) FROM customers") fun customerCount(): Flow<Int>
    @Query("SELECT COALESCE(SUM(amountPiasters), 0) FROM transactions") fun totalSalesPiasters(): Flow<Long>
}

@Database(
    entities = [Customer::class, Product::class, ServiceTransaction::class, Payment::class],
    version = 1,
    exportSchema = false
)
abstract class OfficeDatabase : RoomDatabase() {
    abstract fun officeDao(): OfficeDao
}
