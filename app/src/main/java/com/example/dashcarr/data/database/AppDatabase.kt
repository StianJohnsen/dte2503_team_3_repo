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
 * The main database class for the application.
 * This abstract class extends RoomDatabase and provides DAOs for different entities.
 *
 * @property isDataBaseCreated A LiveData indicating whether the database has been created.
 */
@Database(
    entities = [
        PointOfInterestEntity::class,
        FriendsEntity::class,
        MessagesEntity::class,
        SentMessagesEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    private var isDataBaseCreated = MutableLiveData<Boolean>()

    companion object {
        private const val dbName = "DB_DASHCARR"

        // Singleton instance of the database.
        private var dbInstance: AppDatabase? = null

        /**
         * Returns the singleton instance of the database.
         * If the instance does not exist, it creates and initializes it.
         * @param app The application context used to create the database.
         * @return The singleton instance of AppDatabase.
         */
        fun getInstance(app: Context): AppDatabase {
            if (dbInstance == null) {
                synchronized(AppDatabase::class.java) {
                    dbInstance = createDataBase(app)
                    dbInstance!!.updateDatabaseCreated(app)
                }
            }
            return dbInstance!!
        }

        /**
         * Creates the database instance using Room's database builder.
         * @param app The application context used to create the database.
         * @return The newly created AppDatabase instance.
         */
        private fun createDataBase(app: Context): AppDatabase {
            return Room.databaseBuilder(app.applicationContext, AppDatabase::class.java, dbName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    /**
     * Updates the 'isDataBaseCreated' LiveData when the database is created.
     * @param context The context used to check if the database exists.
     */
    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(dbName).exists()) {
            provideDatabaseCreated()
        }
    }

    /**
     * Sets the 'isDataBaseCreated' LiveData to true.
     */
    private fun provideDatabaseCreated() {
        isDataBaseCreated.postValue(true)
    }

    // DAOs for different entities in the database.
    abstract fun pointsDao(): PointOfInterestDao
    abstract fun FriendsDao(): FriendsDao
    abstract fun MessagesDao(): MessagesDao
    abstract fun SentMessageDao(): SentMessageDao
}
