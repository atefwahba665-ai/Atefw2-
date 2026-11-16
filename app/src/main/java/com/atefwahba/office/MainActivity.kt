package com.atefwahba.office

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.atefwahba.office.data.Customer
import com.atefwahba.office.data.OfficeDatabase
import com.atefwahba.office.data.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val Ink = Color(0xFF10131B)
private val Royal = Color(0xFF173B75)
private val Gold = Color(0xFFD6AE55)
private val Muted = Color(0xFF9BA6B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(applicationContext, OfficeDatabase::class.java, "atef_wahba_office.db").build()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Gold, secondary = Royal, background = Ink, surface = Color(0xFF191E29),
                onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
            )) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OfficeApp(database)
                }
            }
        }
    }
}

private enum class Section(val title: String) {
    Home("الرئيسية"), Customers("العملاء"), Bazaar("البازار"), Services("الخدمات"), Finance("المالية")
}

@Composable
private fun OfficeApp(database: OfficeDatabase) {
    var section by rememberSaveable { mutableStateOf(Section.Home) }
    var showAdd by remember { mutableStateOf(false) }
    val dao = database.officeDao()
    val customers by dao.observeCustomers().collectAsState(initial = emptyList())
    val products by dao.observeProducts().collectAsState(initial = emptyList())
    val transactions by dao.observeTransactions().collectAsState(initial = emptyList())
    val payments by dao.observePayments().collectAsState(initial = emptyList())
    val customerCount by dao.customerCount().collectAsState(initial = 0)
    val sales by dao.totalSalesPiasters().collectAsState(initial = 0L)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF191E29)) {
                Section.values().forEach { item ->
                    val icon = when (item) {
                        Section.Home -> Icons.Default.Home
                        Section.Customers -> Icons.Default.Groups
                        Section.Bazaar -> Icons.Default.Storefront
                        Section.Services -> Icons.Default.Work
                        Section.Finance -> Icons.Default.AccountBalanceWallet
                    }
                    NavigationBarItem(selected = section == item, onClick = { section = item },
                        icon = { Icon(icon, contentDescription = item.title) }, label = { Text(item.title) })
                }
            }
        },
        floatingActionButton = {
            if (section == Section.Customers || section == Section.Bazaar) {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = Gold, contentColor = Ink) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
            Header()
            when (section) {
                Section.Home -> Dashboard(customerCount, sales, transactions.size, payments.size)
                Section.Customers -> CustomerList(customers)
                Section.Bazaar -> ProductList(products)
                Section.Services -> SimpleList("المعاملات والخدمات", transactions.map { "${it.serviceName} • ${money(it.amountPiasters)} • ${it.status}" })
                Section.Finance -> SimpleList("المدفوعات المسجلة", payments.map { "${it.paymentType} • ${money(it.amountPiasters)}" })
            }
        }
    }
    if (showAdd) {
        when (section) {
            Section.Customers -> AddCustomerDialog(onDismiss = { showAdd = false }) { customer ->
                scope.launch { dao.addCustomer(customer) }; showAdd = false
            }
            Section.Bazaar -> AddProductDialog(onDismiss = { showAdd = false }) { product ->
                scope.launch { dao.addProduct(product) }; showAdd = false
            }
            else -> Unit
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(18.dp), color = Royal, modifier = Modifier.size(58.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("AW", color = Gold, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
            }
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text("مكتب عاطف وهبة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("للخدمات العامة", color = Gold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun Dashboard(customerCount: Int, sales: Long, transactionCount: Int, paymentCount: Int) {
    Text("لوحة التحكم", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("ملخص نشاط المكتب", color = Muted, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("إجمالي العملاء", customerCount.toString(), Modifier.weight(1f))
        MetricCard("إجمالي المبيعات", money(sales), Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("المعاملات", transactionCount.toString(), Modifier.weight(1f))
        MetricCard("المدفوعات", paymentCount.toString(), Modifier.weight(1f))
    }
    Spacer(Modifier.height(18.dp))
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF191E29)), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("مرحبًا بك", color = Gold, fontWeight = FontWeight.Bold)
            Text("ابدأ بإضافة العملاء والمنتجات. تُحفظ البيانات محليًا على هذا الجهاز.", color = Muted, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF191E29))) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = Gold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun CustomerList(customers: List<Customer>) {
    Text("إدارة العملاء", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (customers.isEmpty()) EmptyState("لا يوجد عملاء بعد", "اضغط + لإضافة أول عميل")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 14.dp)) {
        items(customers, key = { it.id }) { customer ->
            ListCard(customer.fullName, customer.phone, "كود العميل: ${customer.id.toString().padStart(5, '0')}")
        }
    }
}

@Composable
private fun ProductList(products: List<Product>) {
    Text("البازار الإلكتروني", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("إدارة المنتجات والمخزون", color = Muted, modifier = Modifier.padding(top = 4.dp))
    if (products.isEmpty()) EmptyState("لا توجد منتجات بعد", "أضف المنتجات لبدء إدارة البازار")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 14.dp)) {
        items(products, key = { it.id }) { product ->
            ListCard(product.name, "${product.category} • المخزون: ${product.stockQuantity}", money(product.salePricePiasters))
        }
    }
}

@Composable
private fun SimpleList(title: String, rows: List<String>) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (rows.isEmpty()) EmptyState("لا توجد سجلات بعد", "ستظهر السجلات هنا عند إضافتها")
    else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 14.dp)) {
        items(rows) { ListCard(it, "", "") }
    }
}

@Composable
private fun ListCard(title: String, subtitle: String, trailing: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF191E29)), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                if (subtitle.isNotBlank()) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
            if (trailing.isNotBlank()) Text(trailing, color = Gold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 54.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Gold, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, modifier = Modifier.padding(top = 6.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomerDialog(onDismiss: () -> Unit, onSave: (Customer) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة عميل") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("الاسم بالكامل *") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("رقم الهاتف *") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(nationalId, { nationalId = it }, label = { Text("الرقم القومي (اختياري)") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("العنوان") }, singleLine = true)
            }
        }, confirmButton = { TextButton(enabled = name.isNotBlank() && phone.isNotBlank(), onClick = { onSave(Customer(fullName = name.trim(), phone = phone.trim(), nationalId = nationalId.trim(), address = address.trim())) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } })
}

@Composable
private fun AddProductDialog(onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("0") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("إضافة منتج") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم المنتج *") }, singleLine = true)
                OutlinedTextField(category, { category = it }, label = { Text("القسم") }, singleLine = true)
                OutlinedTextField(price, { price = it }, label = { Text("سعر البيع بالجنيه *") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(cost, { cost = it }, label = { Text("تكلفة الشراء بالجنيه") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(quantity, { quantity = it }, label = { Text("الكمية") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }, confirmButton = {
            val salePiasters = price.toPiasters()
            TextButton(enabled = name.isNotBlank() && salePiasters > 0, onClick = {
                onSave(Product(name = name.trim(), category = category.ifBlank { "عام" }, salePricePiasters = salePiasters,
                    costPricePiasters = cost.toPiasters(), stockQuantity = quantity.toIntOrNull()?.coerceAtLeast(0) ?: 0))
            }) { Text("حفظ") }
        }, dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } })
}

private fun String.toPiasters(): Long = trim().replace(',', '.').toBigDecimalOrNull()?.movePointRight(2)?.setScale(0, java.math.RoundingMode.HALF_UP)?.toLong() ?: 0L
private fun money(piasters: Long): String = "${NumberFormat.getNumberInstance(Locale("ar", "EG")).format(piasters / 100.0)} ج.م"
