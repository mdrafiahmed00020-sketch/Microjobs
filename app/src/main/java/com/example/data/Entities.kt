package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val paymentAmount: Double,
    val requiredProof: String,
    val limitOfWorkers: Int,
    val currentCompletes: Int,
    val postedBy: String = "Admin",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "submissions")
data class Submission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val jobTitle: String,
    val jobPayment: Double,
    val proofText: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val workerEmail: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet")
data class Wallet(
    @PrimaryKey val id: Int = 1,
    val earnedBalance: Double = 0.0,
    val depositBalance: Double = 150.0 // Pre-filled with some BDT for posting test jobs!
)

@Entity(tableName = "transactions")
data class UserTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // EARNED, DEPOSIT, WITHDRAW, POST_JOB
    val amount: Double,
    val paymentMethod: String, // bKash, Nagad, Rocket, System
    val accountInfo: String, // e.g. Phone number or Job Title
    val status: String, // COMPLETED, PENDING, REJECTED
    val timestamp: Long = System.currentTimeMillis()
)
