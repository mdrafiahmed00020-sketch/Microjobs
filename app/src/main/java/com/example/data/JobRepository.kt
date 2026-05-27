package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow

class JobRepository(private val db: AppDatabase) {
    private val jobDao = db.jobDao()
    private val submissionDao = db.submissionDao()
    private val walletDao = db.walletDao()
    private val transactionDao = db.transactionDao()

    val allJobs: Flow<List<Job>> = jobDao.getAllJobsFlow()
    val allTransactions: Flow<List<UserTransaction>> = transactionDao.getAllTransactionsFlow()
    val wallet: Flow<Wallet?> = walletDao.getWalletFlow()
    val allSubmissions: Flow<List<Submission>> = submissionDao.getAllSubmissionsFlow()

    fun getSubmissionsByWorker(email: String): Flow<List<Submission>> = 
        submissionDao.getSubmissionsByWorkerFlow(email)

    fun getSubmissionsForJob(jobId: Int): Flow<List<Submission>> =
        submissionDao.getSubmissionsForJobFlow(jobId)

    suspend fun getJobById(id: Int): Job? = jobDao.getJobById(id)

    suspend fun seedDatabase() {
        val count = jobDao.getJobCount()
        if (count == 0) {
            // Seed default jobs
            val defaultJobs = listOf(
                Job(
                    title = "bKash অ্যাপে ফাইভ স্টার রিভিউ দিন",
                    description = "গুগল প্লে স্টোরে bKash অ্যাপ সার্চ করুন। ফাইভ স্টার রেটিং দিয়ে একটি সুন্দর পজিটিভ রিভিউ লিখুন। কমপক্ষে ৩-৪ টি শব্দে ভালো কিছু লিখুন।",
                    category = "App Store Review",
                    paymentAmount = 15.0,
                    requiredProof = "আপনার সাবমিট করা জিমেইল/রিভিউ নামের টেক্সট এবং সম্পন্ন রিভিউয়ের শেষ লাইনের বিবরণ দিন।",
                    limitOfWorkers = 50,
                    currentCompletes = 12,
                    postedBy = "Admin (MicroJob)"
                ),
                Job(
                    title = "YouTube চ্যানেল সাবস্ক্রাইব ও বেল বাটন প্রেস",
                    description = "আমাদের 'Earning Zone BD' নামের ইউটিউব চ্যানেলে প্রবেশ করে সাবস্ক্রাইব করুন এবং বেল আইকনটি অন করুন। আমাদের শেষ একটি ভিডিওতে লাইক দিন।",
                    category = "YouTube Task",
                    paymentAmount = 4.50,
                    requiredProof = "আপনার ইউটিউব চ্যানেলের হ্যান্ডেল (@user...) বা প্রোফাইল স্ক্রিনশট নাম প্রদান করুন।",
                    limitOfWorkers = 120,
                    currentCompletes = 42,
                    postedBy = "Earning Zone"
                ),
                Job(
                    title = "টেক আর্টিকেলে ১ মিনিট ভিজিট ও স্ক্রল",
                    description = "আমাদের ওয়েবসাইট ভিজিট করুন। পুরো আর্টিকেলটি অলসভাবে স্ক্রল করুন। যেকোনো ১টি রানিং অফার লিংকে ক্লিক করে ক্লিকড প্রুফ নিন।",
                    category = "Website Visit",
                    paymentAmount = 3.20,
                    requiredProof = "ভিজিট করার পর আর্টিকেলের একদম নিচে শেষ লাইনে থাকা গোপন সংকেত (যেমন: 'Success') লিখুন।",
                    limitOfWorkers = 200,
                    currentCompletes = 85,
                    postedBy = "TechBD LLC"
                ),
                Job(
                    title = "ফেসবুক পেজ লাইক ও ফ্লো",
                    description = "প্রদত্ত ফেসবুক পেজটি লাইক এবং ফলো করুন। পেজের প্রথম ৩টি পোস্টে রিয়্যাকশন দিন যাতে করে আপনার রিয়্যাল প্রোফাইল যাচাই সম্ভব হয়।",
                    category = "Facebook Task",
                    paymentAmount = 5.0,
                    requiredProof = "আপনার ফেসবুক ইউজার বা প্রোফাইল লিংক এখানে প্রুফ হিসেবে দিন।",
                    limitOfWorkers = 80,
                    currentCompletes = 19,
                    postedBy = "BD Mart"
                )
            )

            for (job in defaultJobs) {
                jobDao.insertJob(job)
            }
            Log.d("JobRepository", "Jobs seeded successfully!")
        }

        // Initialize Wallet
        val currentWallet = walletDao.getWalletDirect()
        if (currentWallet == null) {
            walletDao.insertOrReplace(Wallet(id = 1, earnedBalance = 0.0, depositBalance = 150.0))
            
            // Add initial transaction
            transactionDao.insertTransaction(
                UserTransaction(
                    type = "DEPOSIT",
                    amount = 150.0,
                    paymentMethod = "System",
                    accountInfo = "স্বাগতম ব্যালেন্স (পোস্টিং টেস্টিং)",
                    status = "COMPLETED"
                )
            )
            Log.d("JobRepository", "Wallet seeded successfully with 150 BDT!")
        }
    }

    suspend fun postJob(title: String, description: String, category: String, paymentAmount: Double, requiredProof: String, limit: Int): Boolean {
        val totalCost = paymentAmount * limit
        val currentWallet = walletDao.getWalletDirect() ?: Wallet(id = 1, earnedBalance = 0.0, depositBalance = 150.0)

        if (currentWallet.depositBalance < totalCost) {
            return false // Insufficient funds
        }

        // Deduct from deposit balance
        val updatedWallet = currentWallet.copy(depositBalance = currentWallet.depositBalance - totalCost)
        walletDao.insertOrReplace(updatedWallet)

        // Insert new job
        val job = Job(
            title = title,
            description = description,
            category = category,
            paymentAmount = paymentAmount,
            requiredProof = requiredProof,
            limitOfWorkers = limit,
            currentCompletes = 0,
            postedBy = "You (Employer)"
        )
        val jobId = jobDao.insertJob(job)

        // Record transaction
        transactionDao.insertTransaction(
            UserTransaction(
                type = "POST_JOB",
                amount = totalCost,
                paymentMethod = "Internal",
                accountInfo = "জব পোস্ট খরচ (ID: $jobId)",
                status = "COMPLETED"
            )
        )
        return true
    }

    suspend fun depositMoney(amount: Double, method: String, txId: String): Boolean {
        val currentWallet = walletDao.getWalletDirect() ?: Wallet(id = 1, earnedBalance = 0.0, depositBalance = 150.0)
        val updatedWallet = currentWallet.copy(depositBalance = currentWallet.depositBalance + amount)
        walletDao.insertOrReplace(updatedWallet)

        transactionDao.insertTransaction(
            UserTransaction(
                type = "DEPOSIT",
                amount = amount,
                paymentMethod = method,
                accountInfo = "বিকাশ/নগদ TrxID: $txId",
                status = "COMPLETED"
            )
        )
        return true
    }

    suspend fun withdrawMoney(amount: Double, method: String, accountInfo: String): Boolean {
        val currentWallet = walletDao.getWalletDirect() ?: Wallet(id = 1, earnedBalance = 0.0, depositBalance = 150.0)
        if (currentWallet.earnedBalance < amount) {
            return false
        }

        val updatedWallet = currentWallet.copy(earnedBalance = currentWallet.earnedBalance - amount)
        walletDao.insertOrReplace(updatedWallet)

        transactionDao.insertTransaction(
            UserTransaction(
                type = "WITHDRAW",
                amount = amount,
                paymentMethod = method,
                accountInfo = "নম্বর: $accountInfo (পেন্ডিং)",
                status = "PENDING"
            )
        )
        return true
    }

    suspend fun submitJobProof(jobId: Int, proofText: String, email: String): Submission? {
        val job = jobDao.getJobById(jobId) ?: return null
        val submission = Submission(
            jobId = jobId,
            jobTitle = job.title,
            jobPayment = job.paymentAmount,
            proofText = proofText,
            status = "PENDING",
            workerEmail = email
        )
        val subId = submissionDao.insertSubmission(submission)
        return submission.copy(id = subId.toInt())
    }

    suspend fun insertMockSubmission(sub: Submission) {
        submissionDao.insertSubmission(sub)
    }

    suspend fun approveSubmission(sub: Submission) {
        submissionDao.updateSubmission(sub.copy(status = "APPROVED"))

        // Credit worker balance (in our app's single wallet flow, if the user worked, they get credited!)
        if (sub.workerEmail == "mdrafi2038@gmail.com") {
            val currentWallet = walletDao.getWalletDirect() ?: Wallet(id = 1, earnedBalance = 0.0, depositBalance = 150.0)
            val updatedWallet = currentWallet.copy(earnedBalance = currentWallet.earnedBalance + sub.jobPayment)
            walletDao.insertOrReplace(updatedWallet)

            transactionDao.insertTransaction(
                UserTransaction(
                    type = "EARNED",
                    amount = sub.jobPayment,
                    paymentMethod = "Work",
                    accountInfo = "পেমেন্ট: ${sub.jobTitle}",
                    status = "COMPLETED"
                )
            )
        }

        // Increment currentCompletes on Job
        val job = jobDao.getJobById(sub.jobId)
        if (job != null) {
            jobDao.updateJob(job.copy(currentCompletes = job.currentCompletes + 1))
        }
    }

    suspend fun rejectSubmission(sub: Submission) {
        submissionDao.updateSubmission(sub.copy(status = "REJECTED"))
    }
}
