package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserTransaction
import com.example.data.Wallet
import com.example.ui.theme.*
import com.example.ui.viewmodel.MicroJobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: MicroJobViewModel,
    onNavigateBack: () -> Unit
) {
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = ড্যাশবোর্ড ও লেনদেন, 1 = ডিপোজিট (Add BDT), 2 = উইথড্র (Cashout)

    // Form inputs
    var depositAmount by remember { mutableStateOf("") }
    var depositMethod by remember { mutableStateOf("bKash") }
    var depositTrxId by remember { mutableStateOf("") }

    var withdrawAmount by remember { mutableStateOf("") }
    var withdrawMethod by remember { mutableStateOf("bKash") }
    var withdrawPhone by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("মাই ওয়ালেট ও লেনদেন", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Money balance overview segment (two separate balances in a beautiful brush card)
            WalletBalanceCard(wallet = wallet)

            // Dynamic Option Chips (Dashboard, Deposit Money, Withdraw Money)
            WalletActionSelector(
                activeIndex = activeSubTab,
                onActiveChange = { activeSubTab = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Area
            when (activeSubTab) {
                0 -> {
                    // Transaction History List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "সাম্প্রতিক লেনদেন সমূহ (Transactions)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        if (transactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো লেনদেনের তথ্য পাওয়া যায়নি!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(transactions) { tx ->
                                    TransactionRow(tx = tx)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Deposit money panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ডিপোজিট করুন (ডিপোজিট ব্যালেন্স বিজ্ঞাপন দিতে ব্যবহৃত হবে)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Select Payment Network
                                Text("পেমেন্ট মেথড নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val methods = listOf("bKash", "Nagad", "Rocket")
                                    methods.forEach { method ->
                                        val isSel = depositMethod == method
                                        val color = when(method) {
                                            "bKash" -> BkashColor
                                            "Nagad" -> NagadColor
                                            else -> BlueTertiary
                                        }
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clickable { depositMethod = method },
                                            border = BorderStroke(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) color else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = method,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSel) color else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Payment Instruction Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BlueTertiary.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "নিয়মাবলী:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BlueTertiary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "১. আমাদের পার্সোনাল নম্বর (০১৭০০-০০০০০০) এ উক্ত মেথডে সেন্ডমানি বা ক্যাশইন করুন।\n" +
                                                    "২. ট্রানজেকশন সফল হলে নিচের বক্সে কাঙ্ক্ষিত পরিমাণ এবং TrxID দিয়ে সাবমিট করুন। পেমেন্ট ২ মিনিটে যোগ হবে।",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Outlined Inputs
                                OutlinedTextField(
                                    value = depositAmount,
                                    onValueChange = { depositAmount = it },
                                    label = { Text("টাকার পরিমাণ (Minimum ৳১০)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("dep_amount_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = depositTrxId,
                                    onValueChange = { depositTrxId = it },
                                    label = { Text("TrxID (যেমন: 8N47HJK89)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("dep_trx_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        val amt = depositAmount.toDoubleOrNull()
                                        if (amt != null && amt >= 10.0 && depositTrxId.trim().isNotEmpty()) {
                                            viewModel.depositMoney(amt, depositMethod, depositTrxId)
                                            // Reset inputs
                                            depositAmount = ""
                                            depositTrxId = ""
                                            focusManager.clearFocus()
                                            activeSubTab = 0 // back to history
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dep_submit_btn"),
                                    enabled = depositAmount.isNotEmpty() && depositTrxId.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ডিপোজিট নিশ্চিত করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Withdraw money panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "আর্নিং ব্যালেন্স উত্তোলন (উইথড্র করুন সরাসরি বিকাশ/নগদে)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyGreen
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Select Payee Method
                                Text("মেথড নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val methods = listOf("bKash", "Nagad", "Rocket")
                                    methods.forEach { method ->
                                        val isSel = withdrawMethod == method
                                        val color = when(method) {
                                            "bKash" -> BkashColor
                                            "Nagad" -> NagadColor
                                            else -> BlueTertiary
                                        }
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .clickable { withdrawMethod = method },
                                            border = BorderStroke(
                                                width = if (isSel) 2.dp else 1.dp,
                                                color = if (isSel) color else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSel) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = method,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSel) color else MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Withdraw Minimum Instructions
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ErrorRed.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "ন্যূনতম উইথড্র: ৳৫০.০০ BDT। উইথড্র পেন্ডিং সাবমিট করার পর অফিসিয়াল ভেরিফিকেশনের ২ ঘণ্টার মধ্য টাকা আপনার নম্বরে ট্রান্সফার করা হবে।",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Outlined inputs
                                OutlinedTextField(
                                    value = withdrawAmount,
                                    onValueChange = { withdrawAmount = it },
                                    label = { Text("টাকার পরিমাণ (মিন. ৳৫০)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("with_amount_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = withdrawPhone,
                                    onValueChange = { withdrawPhone = it },
                                    label = { Text("রিসিভার নম্বর (যেমন: ০১৭********)", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("with_phone_input"),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        val amt = withdrawAmount.toDoubleOrNull()
                                        if (amt != null && amt >= 50.0 && withdrawPhone.trim().isNotEmpty()) {
                                            viewModel.withdrawMoney(amt, withdrawMethod, withdrawPhone)
                                            withdrawAmount = ""
                                            withdrawPhone = ""
                                            focusManager.clearFocus()
                                            activeSubTab = 0 // back to history
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("with_sel_btn"),
                                    enabled = withdrawAmount.isNotEmpty() && withdrawPhone.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextDark),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("উত্তোলন সাবমিট করুন (Proceed)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletBalanceCard(wallet: Wallet?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(DarkSurface, DarkSurfaceElevated)
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "আমার ব্যালেন্স (My Wallet Balance)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Earned Balance Section
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Earnings",
                            tint = MoneyGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "আর্নিং ব্যালেন্স (৳)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳${String.format("%.2f", wallet?.earnedBalance ?: 0.0)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MoneyGreen
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Deposit Balance Section
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddBusiness,
                            contentDescription = "Employer Deposit Balance",
                            tint = BlueTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ডিপোজিট ব্যালেন্স (৳)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳${String.format("%.2f", wallet?.depositBalance ?: 0.0)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun WalletActionSelector(activeIndex: Int, onActiveChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            Triple(0, "লেনদেন হিস্ট্রি", Icons.Default.History),
            Triple(1, "টাকা যোগ করুন", Icons.Default.AddCard),
            Triple(2, "উত্তোলন", Icons.Default.Payments)
        )

        options.forEach { (index, title, icon) ->
            val isActive = activeIndex == index
            val color = if (isActive) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isActive) TextDark else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
                    .clickable { onActiveChange(index) }
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isActive) TextDark else EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRow(tx: UserTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when(tx.type) {
                "DEPOSIT" -> Icons.Default.AddCard to BlueTertiary
                "EARNED" -> Icons.Default.Payments to MoneyGreen
                "WITHDRAW" -> Icons.Default.Output to ErrorRed
                else -> Icons.Default.Campaign to EmeraldSecondary
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tx.type,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val labelBangali = when(tx.type) {
                    "DEPOSIT" -> "ডিপোজিট সফল"
                    "EARNED" -> "কাজ থেকে আয়"
                    "WITHDRAW" -> "টাকা উত্তোলন"
                    else -> "বিজ্ঞাপন ক্যাম্পেইন"
                }
                Text(
                    text = labelBangali,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tx.accountInfo,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                val sign = when(tx.type) {
                    "DEPOSIT", "EARNED" -> "+"
                    "WITHDRAW", "POST_JOB" -> "-"
                    else -> ""
                }
                val color = when(tx.type) {
                    "DEPOSIT", "EARNED" -> MoneyGreen
                    else -> ErrorRed
                }
                Text(
                    text = "$sign৳${String.format("%.2f", tx.amount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                Box(
                    modifier = Modifier
                        .background(
                            color = if (tx.status == "COMPLETED") MoneyGreen.copy(alpha = 0.15f) else BlueTertiary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (tx.status == "COMPLETED") "সফল" else "পেন্ডিং",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.status == "COMPLETED") MoneyGreen else BlueTertiary
                    )
                }
            }
        }
    }
}
