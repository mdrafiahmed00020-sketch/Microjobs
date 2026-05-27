package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MicroJobViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = JobRepository(database)

    // Reactive State Flows
    val jobs: StateFlow<List<Job>> = repository.allJobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallet: StateFlow<Wallet?> = repository.wallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<UserTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val submissions: StateFlow<List<Submission>> = repository.allSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback State
    private val _systemAlert = MutableStateFlow<String?>(null)
    val systemAlert: StateFlow<String?> = _systemAlert.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed database with jobs & initial wallet BDT if empty
            repository.seedDatabase()
        }
    }

    fun clearAlerts() {
        _systemAlert.value = null
        _successMessage.value = null
    }

    // Worker Work Flow
    fun submitProof(jobId: Int, proofText: String) {
        viewModelScope.launch {
            val userEmail = "mdrafi2038@gmail.com" // Preserved User email from context
            val sub = repository.submitJobProof(jobId, proofText, userEmail)
            if (sub != null) {
                _successMessage.value = "আপনার কাজটি সফলভাবে রিভিউ এর জন্য পাঠানো হয়েছে!"
                
                // Simulate an Admin/Employer reviewing the work after 10 seconds
                launch {
                    delay(8000)
                    repository.approveSubmission(sub)
                    _systemAlert.value = "কাজ অনুমোদিত! '${sub.jobTitle}' কাজের জন্য ৳${String.format("%.2f", sub.jobPayment)} আপনার ওয়ালেটে যোগ করা হয়েছে।"
                }
            } else {
                _systemAlert.value = "কাজ সাবমিশন ব্যর্থ হয়েছে। পুনরায় চেষ্টা করুন।"
            }
        }
    }

    // Employer Job Post Flow
    fun postJob(
        title: String,
        description: String,
        category: String,
        paymentAmount: Double,
        requiredProof: String,
        limit: Int,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.postJob(title, description, category, paymentAmount, requiredProof, limit)
            onResult(success)
            if (success) {
                _successMessage.value = "আপনার নতুন জবটি সফলভাবে পোস্ট হয়েছে!"
                
                // Simulate active workers submitting work on this newly posted job after 12 seconds
                launch {
                    delay(12000)
                    // Get latest completed or posted jobs by user to fetch jobId
                    val latestJobs = repository.allJobs.firstOrNull() ?: emptyList()
                    val userPostedJob = latestJobs.firstOrNull { it.postedBy.contains("You") }
                    
                    if (userPostedJob != null) {
                        val mockWorkers = listOf(
                            Pair("rakib99@gmail.com", "স্যার কাজ কমপ্লিট। চ্যানেল সাবস্ক্রাইব করেছি এবং লাইক দিয়েছি। স্ক্রিনশট দিলাম।"),
                            Pair("tania_mous@gmail.com", "Done, complete fully. Play Store user name Tania."),
                            Pair("rifat_boss@yahoo.com", "ভাই কাজ শেষ। সিক্রেট কোড Success প্রদান করলাম। চেক করুন।"),
                            Pair("milon_bd1@gmail.com", "ফেসবুকে শেয়ার দিয়েছি পাবলিকলি। আমার প্রোফাইল লিংক: fb.com/milon")
                        )
                        val randomWorker = mockWorkers.random()
                        
                        repository.insertMockSubmission(
                            Submission(
                                jobId = userPostedJob.id,
                                jobTitle = userPostedJob.title,
                                jobPayment = userPostedJob.paymentAmount,
                                proofText = randomWorker.second,
                                status = "PENDING",
                                workerEmail = randomWorker.first
                            )
                        )
                        _systemAlert.value = "গরম খবর! এক কর্মী আপনার '${userPostedJob.title}' জবের প্রমাণ সাবমিট করেছে।"
                    }
                }
            } else {
                _systemAlert.value = "অপর্যাপ্ত ডিপোজিট ব্যালেন্স! দয়া করে আপনার একাউন্টে টাকা যোগ করুন।"
            }
        }
    }

    // Deposit Money Flow
    fun depositMoney(amount: Double, method: String, txId: String) {
        viewModelScope.launch {
            repository.depositMoney(amount, method, txId)
            _successMessage.value = "৳${String.format("%.2f", amount)} ডিপোজিট সফল হয়েছে এবং আপনার ডিপোজিট ব্যালেন্সে যোগ হয়েছে!"
        }
    }

    // Withdraw Money Flow
    fun withdrawMoney(amount: Double, method: String, accountInfo: String) {
        viewModelScope.launch {
            val success = repository.withdrawMoney(amount, method, accountInfo)
            if (success) {
                _successMessage.value = "৳${String.format("%.2f", amount)} উত্তোলনের অনুরোধ সফল হয়েছে! ২ ঘণ্টার মধ্যে পেমেন্ট সম্পন্ন হবে।"
            } else {
                _systemAlert.value = "অপর্যাপ্ত আর্নিং ব্যালেন্স! কাজ সম্পন্ন করে আর্নিং ব্যালেন্স বৃদ্ধি করুন।"
            }
        }
    }

    // Employer actions on Worker Submissions
    fun approveWorkerSubmission(sub: Submission) {
        viewModelScope.launch {
            repository.approveSubmission(sub)
            _successMessage.value = "প্রমাণটি অনুমোদিত হয়েছে! কর্মীকে পেমেন্ট প্রদান করা হল।"
        }
    }

    fun rejectWorkerSubmission(sub: Submission) {
        viewModelScope.launch {
            repository.rejectSubmission(sub)
            _systemAlert.value = "প্রমাণটি সফলভাবে প্রত্যাখ্যান করা হয়েছে।"
        }
    }
}
