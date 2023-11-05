package com.example.dashcarr.data.database

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.data.database.dao.MessagesDao
import com.example.dashcarr.data.database.dao.PointOfInterestDao
import com.example.dashcarr.data.database.dao.SentMessageDao
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity

/**
 * Room Database class for the app.
 *
 * This database class defines the entities it contains and provides a method to access
 * the associated Data Access Object (DAO).
 *
 * @property isDataBaseCreated A [MutableLiveData] to observe whether the database has been created.
 */
@Database(
    entities = [
        PointOfInterestEntity::class,
        FriendsEntity::class,
        MessagesEntity::class,
        SentMessagesEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    private var isDataBaseCreated = MutableLiveData<Boolean>()

    companion object {
        private const val dbName = "DB_DASHCARR"
        private var dbInstance: AppDatabase? = null

        fun getInstance(app: Context): AppDatabase {
            if (dbInstance == null) {
                synchronized(AppDatabase::class.java) {
                    dbInstance = createDataBase(app)
                    dbInstance!!.updateDatabaseCreated(app)
                }
            }
            return dbInstance!!
        }

        private fun createDataBase(app: Context): AppDatabase {
            return Room.databaseBuilder(app.applicationContext, AppDatabase::class.java, dbName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }


    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(dbName).exists()) {
            provideDatabaseCreated()
        }
    }

    private fun provideDatabaseCreated() {
        isDataBaseCreated.postValue(true)
    }

    abstract fun pointsDao(): PointOfInterestDao
    abstract fun FriendsDao(): FriendsDao
    abstract fun MessagesDao(): MessagesDao
    abstract fun SentMessageDao(): SentMessageDao
}