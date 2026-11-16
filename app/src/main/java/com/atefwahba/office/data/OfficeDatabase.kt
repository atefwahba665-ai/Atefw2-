package com.atefwahba.office.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

/** A service catalog managed in-app; adding a service does not require a code change. */
@Entity(tableName = "service_catalog")
data class ServiceCatalogItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultPricePiasters: Long = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val address: String = "",
    val commissionRateBasisPoints: Int = 90,
    val isActive: Boolean = true,
    val hiredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installment_contracts")
data class InstallmentContract(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val title: String,
    val principalPiasters: Long,
    val downPaymentPiasters: Long,
    val interestRateBasisPoints: Int,
    val taxRateBasisPoints: Int,
    val taxBasis: String,
    val totalPiasters: Long,
    val installmentCount: Int,
    val installmentPiasters: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contractId: Long,
    val dueAt: Long,
    val amountPiasters: Long,
    val paidPiasters: Long = 0,
    val paidAt: Long? = null
)

/** Direction is IN or OUT. Each ledger row is a single source-of-truth cash movement. */
@Entity(tableName = "cash_entries")
data class CashEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val direction: String,
    val category: String,
    val amountPiasters: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "commissions")
data class Commission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val sourceTransactionId: Long,
    val rateBasisPoints: Int,
    val amountPiasters: Long,
    val status: String = "مستحقة",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketing_entries")
data class MarketingEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val entryType: String,
    val amountPiasters: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val description: String,
    val status: String = "جديد",
    val feePiasters: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "office_settings")
data class OfficeSetting(
    @PrimaryKey val settingKey: String,
    val settingValue: String
)

@Dao
interface OfficeDao {
    @Query("SELECT * FROM customers ORDER BY id DESC") fun observeCustomers(): Flow<List<Customer>>
    @Query("SELECT * FROM products ORDER BY id DESC") fun observeProducts(): Flow<List<Product>>
    @Query("SELECT * FROM transactions ORDER BY id DESC") fun observeTransactions(): Flow<List<ServiceTransaction>>
    @Query("SELECT * FROM payments ORDER BY id DESC") fun observePayments(): Flow<List<Payment>>
    @Query("SELECT * FROM cash_entries ORDER BY id DESC") fun observeCashEntries(): Flow<List<CashEntry>>
    @Query("SELECT * FROM service_catalog WHERE isActive = 1 ORDER BY name") fun observeServiceCatalog(): Flow<List<ServiceCatalogItem>>
    @Query("SELECT * FROM employees ORDER BY id DESC") fun observeEmployees(): Flow<List<Employee>>
    @Query("SELECT * FROM installment_contracts ORDER BY id DESC") fun observeContracts(): Flow<List<InstallmentContract>>
    @Query("SELECT * FROM installments WHERE paidPiasters < amountPiasters ORDER BY dueAt") fun observeUnpaidInstallments(): Flow<List<Installment>>
    @Query("SELECT * FROM commissions ORDER BY id DESC") fun observeCommissions(): Flow<List<Commission>>
    @Query("SELECT * FROM marketing_entries ORDER BY id DESC") fun observeMarketingEntries(): Flow<List<MarketingEntry>>
    @Query("SELECT * FROM delivery_orders ORDER BY id DESC") fun observeDeliveryOrders(): Flow<List<DeliveryOrder>>

    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addCustomer(customer: Customer)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addProduct(product: Product)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addTransaction(transaction: ServiceTransaction): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addPayment(payment: Payment)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addCashEntry(entry: CashEntry)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addServiceCatalogItem(item: ServiceCatalogItem)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addEmployee(employee: Employee)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addContract(contract: InstallmentContract): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addInstallments(installments: List<Installment>)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addCommission(commission: Commission)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addMarketingEntry(entry: MarketingEntry)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun addDeliveryOrder(order: DeliveryOrder)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSetting(setting: OfficeSetting)

    @Query("SELECT COUNT(*) FROM customers") fun customerCount(): Flow<Int>
    @Query("SELECT COALESCE(SUM(amountPiasters), 0) FROM transactions") fun totalSalesPiasters(): Flow<Long>
    @Query("SELECT COALESCE(SUM(CASE WHEN direction = 'IN' THEN amountPiasters ELSE -amountPiasters END), 0) FROM cash_entries") fun cashBalancePiasters(): Flow<Long>
    @Query("SELECT COALESCE(SUM(amountPiasters - paidPiasters), 0) FROM installments") fun unpaidInstallmentsPiasters(): Flow<Long>
    @Query("SELECT * FROM office_settings WHERE settingKey = :key LIMIT 1") fun observeSetting(key: String): Flow<OfficeSetting?>
}

@Database(
    entities = [Customer::class, Product::class, ServiceTransaction::class, Payment::class,
        ServiceCatalogItem::class, Employee::class, InstallmentContract::class, Installment::class,
        CashEntry::class, Commission::class, MarketingEntry::class, DeliveryOrder::class, OfficeSetting::class],
    version = 2,
    exportSchema = false
)
abstract class OfficeDatabase : RoomDatabase() {
    abstract fun officeDao(): OfficeDao

    companion object {
        /** Additive migration preserves existing customer, product, transaction and payment records. */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS service_catalog (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, defaultPricePiasters INTEGER NOT NULL, isActive INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS employees (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, fullName TEXT NOT NULL, phone TEXT NOT NULL, address TEXT NOT NULL, commissionRateBasisPoints INTEGER NOT NULL, isActive INTEGER NOT NULL, hiredAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS installment_contracts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, customerId INTEGER NOT NULL, title TEXT NOT NULL, principalPiasters INTEGER NOT NULL, downPaymentPiasters INTEGER NOT NULL, interestRateBasisPoints INTEGER NOT NULL, taxRateBasisPoints INTEGER NOT NULL, taxBasis TEXT NOT NULL, totalPiasters INTEGER NOT NULL, installmentCount INTEGER NOT NULL, installmentPiasters INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS installments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, contractId INTEGER NOT NULL, dueAt INTEGER NOT NULL, amountPiasters INTEGER NOT NULL, paidPiasters INTEGER NOT NULL, paidAt INTEGER)")
                db.execSQL("CREATE TABLE IF NOT EXISTS cash_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, receiptNumber TEXT NOT NULL, direction TEXT NOT NULL, category TEXT NOT NULL, amountPiasters INTEGER NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS commissions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, employeeId INTEGER NOT NULL, sourceTransactionId INTEGER NOT NULL, rateBasisPoints INTEGER NOT NULL, amountPiasters INTEGER NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS marketing_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, employeeId INTEGER NOT NULL, entryType TEXT NOT NULL, amountPiasters INTEGER NOT NULL, note TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS delivery_orders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, customerId INTEGER, description TEXT NOT NULL, status TEXT NOT NULL, feePiasters INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS office_settings (settingKey TEXT NOT NULL PRIMARY KEY, settingValue TEXT NOT NULL)")
                db.execSQL("INSERT OR IGNORE INTO office_settings (settingKey, settingValue) VALUES ('installment_interest_percent', '10'), ('sales_tax_percent', '5'), ('commission_percent', '0.9'), ('tax_basis', 'قبل الفائدة')")
            }
        }
    }
}
