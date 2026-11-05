package com.moyritm.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StickDao {
    @Query(
        "SELECT * FROM stick_entries " +
            "WHERE recordedAtMillis >= :fromMillis AND recordedAtMillis < :toMillis " +
            "ORDER BY recordedAtMillis DESC"
    )
    fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<StickEntryEntity>>

    @Insert
    suspend fun insert(entry: StickEntryEntity)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name")
    fun observeActive(): Flow<List<MedicationEntity>>

    @Query(
        "SELECT * FROM medication_schedules " +
            "WHERE medicationId = :medicationId AND enabled = 1 ORDER BY timeMinutes"
    )
    fun observeSchedules(medicationId: Long): Flow<List<MedicationScheduleEntity>>

    @Query(
        "SELECT * FROM medication_intakes " +
            "WHERE scheduledAtMillis >= :fromMillis AND scheduledAtMillis < :toMillis " +
            "ORDER BY scheduledAtMillis"
    )
    fun observeIntakesBetween(fromMillis: Long, toMillis: Long): Flow<List<MedicationIntakeEntity>>

    @Insert
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Insert
    suspend fun insertSchedule(schedule: MedicationScheduleEntity)

    @Insert
    suspend fun insertIntake(intake: MedicationIntakeEntity)

    @Query(
        "UPDATE medication_intakes " +
            "SET status = :status, takenAtMillis = :takenAtMillis WHERE id = :id"
    )
    suspend fun updateIntakeStatus(id: Long, status: String, takenAtMillis: Long?)
}

@Dao
interface FoodDao {
    @Query(
        "SELECT * FROM food_products " +
            "WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT 50"
    )
    fun searchProducts(query: String): Flow<List<FoodProductEntity>>

    @Query(
        "SELECT * FROM food_entries " +
            "WHERE eatenAtMillis >= :fromMillis AND eatenAtMillis < :toMillis " +
            "ORDER BY eatenAtMillis DESC"
    )
    fun observeEntriesBetween(fromMillis: Long, toMillis: Long): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProduct(product: FoodProductEntity): Long

    @Insert
    suspend fun insertEntry(entry: FoodEntryEntity)
}

@Dao
interface WaterDao {
    @Query(
        "SELECT * FROM water_entries " +
            "WHERE recordedAtMillis >= :fromMillis AND recordedAtMillis < :toMillis " +
            "ORDER BY recordedAtMillis DESC"
    )
    fun observeBetween(fromMillis: Long, toMillis: Long): Flow<List<WaterEntryEntity>>

    @Insert
    suspend fun insert(entry: WaterEntryEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: AppSettingsEntity)
}
