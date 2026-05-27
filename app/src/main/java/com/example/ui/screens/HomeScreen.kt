package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Job
import com.example.data.Submission
import com.example.data.Wallet
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.theme.*
import com.example.ui.viewmodel.MicroJobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MicroJobViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToPostJob: () -> Unit
) {
    val jobs by viewModel.jobs.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val submissions by viewModel.submissions.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = সব কাজ (All Jobs), 1 = আমার কাজ (My Submissions), 2 = পোস্ট করা কাজ (Employer View)
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedJobForDetail by remember { mutableStateOf<Job?>(null) }
    var selectedJobForSubmissionsReview by remember { mutableStateOf<Job?>(null) }

    val categories = listOf("All", "YouTube Task", "App Store Review", "Website Visit", "Facebook Task")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(EmeraldPrimary, BlueTertiary)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "MicroJob BD",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Modern styled Balance Badge
                    Card(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable(onClick = onNavigateToWallet),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "৳${String.format("%.2f", wallet?.earnedBalance ?: 0.0)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoneyGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToPostJob,
                containerColor = EmeraldPrimary,
                contentColor = TextDark,
                icon = { Icon(Icons.Default.Add, contentDescription = "Post Job") },
                text = { Text("নতুন কাজ পোস্টিং", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("post_job_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Welcome & Metrics Slider Board
            WelcomeMetricsBoard(wallet = wallet, onNavigateToWallet = onNavigateToWallet)

            // Segmented Tab Selectors for Workers vs. Employers
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) { index ->
                selectedTabIndex = index
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Job Feeding & Category Filters
                    CategoryScroller(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { selectedCategory = it }
                    )

                    val filteredJobs = jobs.filter {
                        selectedCategory == "All" || it.category == selectedCategory
                    }

                    if (filteredJobs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.WorkOutline,
                            title = "আফসোস! বর্তমানে কোনো কাজ নেই",
                            subtitle = "নতুন কাজ খোলার জন্য অপেক্ষা করুন অথবা নিজে একটি কাজ পোস্ট করুন।"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            items(filteredJobs, key = { it.id }) { job ->
                                JobCard(
                                    job = job,
                                    onClick = { selectedJobForDetail = job }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // My Completed Work (Submissions)
                    val mySubmissions = submissions.filter { it.workerEmail == "mdrafi2038@gmail.com" }

                    if (mySubmissions.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.AddTask,
                            title = "আপনি এখনো কোনো কাজ করেননি",
                            subtitle = "হোম পেজের কাজগুলো সম্পন্ন করে আজই টাকা আয় করা শুরু করুন।"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            items(mySubmissions, key = { it.id }) { sub ->
                                SubmissionCard(submission = sub)
                            }
                        }
                    }
                }
                2 -> {
                    // Employer's My Posted Jobs Tab
                    val myPostedJobs = jobs.filter { it.postedBy.contains("You") || it.postedBy.contains("Employer") }

                    if (myPostedJobs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Outlined.Campaign,
                            title = "আপনার কোনো পোস্ট করা কাজ নেই",
                            subtitle = "নিচের '+' বাটনে ক্লিক করে আজই আপনার কাস্টম জব ক্যাম্পেইন চালু করুন!"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.weight(1.0f)
                        ) {
                            items(myPostedJobs, key = { it.id }) { job ->
                                EmployerJobCard(
                                    job = job,
                                    submissionsCounts = submissions.count { it.jobId == job.id && it.status == "PENDING" },
                                    onClick = { selectedJobForSubmissionsReview = job }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Action Panel Dialog
    selectedJobForDetail?.let { job ->
        JobDetailsDialog(
            job = job,
            onDismiss = { selectedJobForDetail = null },
            onSubmitProof = { proof ->
                viewModel.submitProof(job.id, proof)
                selectedJobForDetail = null
            }
        )
    }

    // Employer's Submissions Approval Sheet Dialog
    selectedJobForSubmissionsReview?.let { job ->
        val jobSubmissions = submissions.filter { it.jobId == job.id }
        EmployerReviewSubmissionsDialog(
            job = job,
            pendingSubmissions = jobSubmissions,
            onDismiss = { selectedJobForSubmissionsReview = null },
            onApprove = { sub -> viewModel.approveWorkerSubmission(sub) },
            onReject = { sub -> viewModel.rejectWorkerSubmission(sub) }
        )
    }
}

@Composable
fun WelcomeMetricsBoard(wallet: Wallet?, onNavigateToWallet: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "হ্যালো, MD Rafi!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "সহজ কাজ সম্পূর্ণ করে ঘরে বসেই অনলাইন ইনকাম করুন",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onNavigateToWallet,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueTertiary)
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = "Earning Status", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("বিস্তারিত", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecondaryTabRow(selectedTabIndex: Int, onTabSelect: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = EmeraldPrimary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = EmeraldPrimary
            )
        }
    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { onTabSelect(0) },
            text = { Text("সব কাজ (Find Jobs)", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Find Jobs") }
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { onTabSelect(1) },
            text = { Text("আমার কাজ (My Works)", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = "My Work") }
        )
        Tab(
            selected = selectedTabIndex == 2,
            onClick = { onTabSelect(2) },
            text = { Text("পোস্ট করা (Employer)", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Campaign, contentDescription = "Posted Jobs") }
        )
    }
}

@Composable
fun CategoryScroller(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            val labelBengali = when(category) {
                "All" -> "নির্ধারিত সব"
                "YouTube Task" -> "ইউটিউব"
                "App Store Review" -> "অ্যাপ রিভিউ"
                "Website Visit" -> "ওয়েবসাইট ভিজিট"
                "Facebook Task" -> "ফেসবুক"
                else -> category
            }
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(category) },
                label = { Text(labelBengali) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = TextDark,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun JobCard(job: Job, onClick: () -> Unit) {
    val progress = job.currentCompletes.toFloat() / job.limitOfWorkers.toFloat()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("job_item_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip / Tag
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                // Reward Amount Panel BDT
                Text(
                    text = "৳${String.format("%.2f", job.paymentAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MoneyGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = job.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = job.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Limit Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "পোস্টদাতা: ${job.postedBy}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Text(
                    text = "কর্মী: ${job.currentCompletes}/${job.limitOfWorkers}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (progress >= 1.0f) ErrorRed else EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun SubmissionCard(submission: Submission) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = submission.jobTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+৳${String.format("%.2f", submission.jobPayment)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MoneyGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "সাবমিট করা প্রুফ: ${submission.proofText}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val date = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(submission.timestamp))
                Text(text = date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))

                // Custom Status Badging
                val containerColor = when (submission.status) {
                    "APPROVED" -> MoneyGreen.copy(alpha = 0.15f)
                    "PENDING" -> BlueTertiary.copy(alpha = 0.15f)
                    else -> ErrorRed.copy(alpha = 0.15f)
                }
                val textColor = when (submission.status) {
                    "APPROVED" -> MoneyGreen
                    "PENDING" -> BlueTertiary
                    else -> ErrorRed
                }
                val label = when (submission.status) {
                    "APPROVED" -> "অনুমোদিত"
                    "PENDING" -> "রিভিউ পেন্ডিং"
                    else -> "বাতিল"
                }

                Box(
                    modifier = Modifier
                        .background(containerColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun EmployerJobCard(job: Job, submissionsCounts: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = job.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "৳${String.format("%.2f", job.paymentAmount)} / কর্মী",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আবেদন সম্পন্ন: ${job.currentCompletes}/${job.limitOfWorkers}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (submissionsCounts > 0) {
                    Box(
                        modifier = Modifier
                            .background(ErrorRed, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$submissionsCounts নতুন প্রুফ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "কোনো পেন্ডিং প্রুফ নেই",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: ImageVector, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

// ---------------- DIALOGS ----------------- //

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsDialog(
    job: Job,
    onDismiss: () -> Unit,
    onSubmitProof: (String) -> Unit
) {
    var proofText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("job_detail_pane"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কাজটির বিবরণী",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = job.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "৳${String.format("%.2f", job.paymentAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MoneyGreen
                    )
                    Text(
                        text = "• Category: ${job.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                Text(
                    text = "কাজের শর্ত ও পড়ার নিয়ম:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "প্রমাণ হিসেবে যা দিতে হবে:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = job.requiredProof,
                        fontSize = 12.sp,
                        color = BlueTertiary,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "আপনার কাজের প্রুফ লিখুন:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = proofText,
                    onValueChange = { proofText = it },
                    placeholder = { Text("এখানে টেক্সট প্রুফ দিন...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("proof_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (proofText.trim().isNotEmpty()) {
                            onSubmitProof(proofText)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_proof_btn"),
                    enabled = proofText.trim().isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextDark)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Submit")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অনলাইনে সাবমিট করুন (Submit Proof)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmployerReviewSubmissionsDialog(
    job: Job,
    pendingSubmissions: List<Submission>,
    onDismiss: () -> Unit,
    onApprove: (Submission) -> Unit,
    onReject: (Submission) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .heightIn(max = 500.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কর্মী কাজ যাচাইকরণ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueTertiary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = job.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                val activeSubmissions = pendingSubmissions.filter { it.status == "PENDING" }

                if (activeSubmissions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো পেন্ডিং রিভিউ প্রুফ নেই!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(activeSubmissions) { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "কর্মী: ${sub.workerEmail}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "প্রমাণ: ${sub.proofText}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { onReject(sub) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                            border = BorderStroke(1.dp, ErrorRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text("প্রত্যাখ্যান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { onApprove(sub) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("অনুমোদন দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
