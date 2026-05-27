package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobsFlow(): Flow<List<Job>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: Int): Job?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: Job): Long

    @Update
    suspend fun updateJob(job: Job)

    @Delete
    suspend fun deleteJob(job: Job)
    
    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getJobCount(): Int
}

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM submissions ORDER BY timestamp DESC")
    fun getAllSubmissionsFlow(): Flow<List<Submission>>

    @Query("SELECT * FROM submissions WHERE workerEmail = :email ORDER BY timestamp DESC")
    fun getSubmissionsByWorkerFlow(email: String): Flow<List<Submission>>

    @Query("SELECT * FROM submissions WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun getSubmissionsForJobFlow(jobId: Int): Flow<List<Submission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(sub: Submission): Long

    @Update
    suspend fun updateSubmission(sub: Submission)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 1")
    fun getWalletFlow(): Flow<Wallet?>

    @Query("SELECT * FROM wallet WHERE id = 1")
    suspend fun getWalletDirect(): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(wallet: Wallet)

    @Update
    suspend fun updateWallet(wallet: Wallet)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<UserTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: UserTransaction): Long
}
