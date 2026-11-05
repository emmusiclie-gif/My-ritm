package com.moyritm.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StickEntryEntity::class,
        MedicationEntity::class,
        MedicationScheduleEntity::class,
        MedicationIntakeEntity::class,
        FoodProductEntity::class,
        FoodEntryEntity::class,
        WaterEntryEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stickDao(): StickDao
    abstract fun medicationDao(): MedicationDao
    abstract fun foodDao(): FoodDao
    abstract fun waterDao(): WaterDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_ritm_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
