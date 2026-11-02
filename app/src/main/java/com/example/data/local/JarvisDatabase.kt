package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.data.model.NotificationSummaryItem
import com.example.data.model.ReminderItem
import com.example.data.model.WorkflowItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timestamp ASC")
    fun getAllReminders(): Flow<List<ReminderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderItem): Long

    @Update
    suspend fun updateReminder(reminder: ReminderItem)

    @Delete
    suspend fun deleteReminder(reminder: ReminderItem)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun clearCompleted()
}

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY id ASC")
    fun getAllWorkflows(): Flow<List<WorkflowItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowItem): Long

    @Update
    suspend fun updateWorkflow(workflow: WorkflowItem)

    @Delete
    suspend fun deleteWorkflow(workflow: WorkflowItem)

    @Query("SELECT * FROM workflows WHERE isEnabled = 1")
    suspend fun getEnabledWorkflows(): List<WorkflowItem>
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryItem): Long

    @Delete
    suspend fun deleteMemory(memory: MemoryItem)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    @Query("SELECT * FROM memories WHERE keyName = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryItem?
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT 50")
    fun getRecentNotifications(): Flow<List<NotificationSummaryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(item: NotificationSummaryItem): Long

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Database(
    entities = [
        ChatMessage::class,
        ReminderItem::class,
        WorkflowItem::class,
        MemoryItem::class,
        NotificationSummaryItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun reminderDao(): ReminderDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun memoryDao(): MemoryDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
