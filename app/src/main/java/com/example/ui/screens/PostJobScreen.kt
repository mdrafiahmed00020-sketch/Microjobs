package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Wallet
import com.example.ui.theme.*
import com.example.ui.viewmodel.MicroJobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    viewModel: MicroJobViewModel,
    onNavigateBack: () -> Unit
) {
    val wallet by viewModel.wallet.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("YouTube Task") }
    var paymentAmount by remember { mutableStateOf("") }
    var limitOfWorkers by remember { mutableStateOf("") }
    var requiredProof by remember { mutableStateOf("") }

    val categories = listOf("YouTube Task", "App Store Review", "Website Visit", "Facebook Task")
    val focusManager = LocalFocusManager.current

    // Calculations
    val calculatedPayment = paymentAmount.toDoubleOrNull() ?: 0.0
    val calculatedLimit = limitOfWorkers.toIntOrNull() ?: 0
    val totalCost = calculatedPayment * calculatedLimit
    val satisfiesBalance = (wallet?.depositBalance ?: 0.0) >= totalCost

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("নতুন কাজ খুলুন (Post A Job)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            // Employer Deposit Balance header tracker
            DepositMiniBadge(wallet = wallet)

            // Input Fields Form Scroller
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Form Card Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("কাজের শিরোনাম (যেমন: চ্যানেল সাবস্ক্রাইব করুন)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("job_title_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Description Instructions
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("কাজের নিখুঁত বিবরণ এবং ধাপসমূহ", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("job_desc_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Category Chips Inline
                        Text("ক্যাটাগরি নির্বাচন করুন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                val labelBengali = when(cat) {
                                    "YouTube Task" -> "ইউটিউব"
                                    "App Store Review" -> "অ্যাপ রিভিউ"
                                    "Website Visit" -> "ওয়েবসাইট ভিজিট"
                                    "Facebook Task" -> "ফেসবুক"
                                    else -> cat
                                }
                                val selected = category == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { category = cat },
                                    label = { Text(labelBengali) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary,
                                        selectedLabelColor = TextDark,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }

                        // Payment & Count Side-by-Side
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = paymentAmount,
                                onValueChange = { paymentAmount = it },
                                label = { Text("৳ জনপ্রতি পেমেন্ট (৳১-৳৫০)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("payment_amt_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = limitOfWorkers,
                                onValueChange = { limitOfWorkers = it },
                                label = { Text("কর্মী সংখ্যা (মিন. ৫জন)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).testTag("limit_input"),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Required Proof Text
                        OutlinedTextField(
                            value = requiredProof,
                            onValueChange = { requiredProof = it },
                            label = { Text("প্রমাণ হিসেবে কর্মীকে কী দিতে হবে?", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("proof_instr_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Calculated Cost Estimator Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (satisfiesBalance) BlueTertiary.copy(alpha = 0.08f) else ErrorRed.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Cost Calculation",
                                tint = if (satisfiesBalance) BlueTertiary else ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "হিসাব নিকাশ (Cost Breakdown):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (satisfiesBalance) BlueTertiary else ErrorRed
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "বিজ্ঞাপন খরচ (পেমেন্ট x কর্মী)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "৳${String.format("%.2f", calculatedPayment)} x $calculatedLimit = ৳${String.format("%.2f", totalCost)} BDT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "ডিপোজিট ব্যালেন্স স্ট্যাটাস", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val statusText = if (satisfiesBalance) "পর্যাপ্ত টাকা আছে" else "অপর্যাপ্ত ব্যালেন্স (টাকা ভরুন)"
                            val statusColor = if (satisfiesBalance) MoneyGreen else ErrorRed
                            Text(text = statusText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                }

                // Submit Button
                val inputIsValid = title.trim().isNotEmpty() &&
                        description.trim().isNotEmpty() &&
                        calculatedPayment > 0.0 &&
                        calculatedLimit >= 5 &&
                        requiredProof.trim().isNotEmpty()

                Button(
                    onClick = {
                        if (inputIsValid && satisfiesBalance) {
                            viewModel.postJob(
                                title = title,
                                description = description,
                                category = category,
                                paymentAmount = calculatedPayment,
                                requiredProof = requiredProof,
                                limit = calculatedLimit
                            ) { success ->
                                if (success) {
                                    focusManager.clearFocus()
                                    onNavigateBack()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("pub_job_btn"),
                    enabled = inputIsValid && satisfiesBalance,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = TextDark
                    )
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "Publish Campaign")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("জবটি অনলাইনে পাবলিশ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DepositMiniBadge(wallet: Wallet?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "আপনার বর্তমান ডিপোজিট ব্যালেন্স:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "৳${String.format("%.2f", wallet?.depositBalance ?: 0.0)} BDT",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BlueTertiary
            )
        }
    }
}
