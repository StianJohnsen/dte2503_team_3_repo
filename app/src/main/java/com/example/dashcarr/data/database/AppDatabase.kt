package com.example.dashcarr.data.database

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dashcarr.data.database.dao.PointOfInterestDao
import com.example.dashcarr.domain.entity.PointOfInterestEntity

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
        PointOfInterestEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {

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
}