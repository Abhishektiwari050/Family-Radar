package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FamilyMemberEntity::class, GeofenceEntity::class, TrackingLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun geofenceDao(): GeofenceDao
    abstract fun trackingLogDao(): TrackingLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "family_tracker_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(db: AppDatabase) {
    val memberDao = db.familyMemberDao()
    val geofenceDao = db.geofenceDao()
    val logDao = db.trackingLogDao()

    // Default coordinates setup around a central "Home", e.g. Central Park Zoo (lat: 40.7678, lng: -73.9718)
    val centerLat = 40.7678
    val centerLng = -73.9718

    val defaultMembers = listOf(
        FamilyMemberEntity(
            id = 2,
            name = "Sarah Carter",
            relationship = "Mom",
            latitude = centerLat + 0.0035,
            longitude = centerLng - 0.0020,
            batteryPercent = 88,
            statusMessage = "☕ At Starbucks nearby",
            avatarEmoji = "👩",
            colorHex = "#EC4899", // Pink
            speed = 1.2
        ),
        FamilyMemberEntity(
            id = 3,
            name = "Robert Carter",
            relationship = "Dad",
            latitude = centerLat - 0.0050,
            longitude = centerLng + 0.0065,
            batteryPercent = 74,
            statusMessage = "🚗 Heading this way",
            avatarEmoji = "👨",
            colorHex = "#3B82F6", // Blue
            speed = 36.5
        ),
        FamilyMemberEntity(
            id = 4,
            name = "Emily Carter",
            relationship = "Sister",
            latitude = centerLat + 0.0060,
            longitude = centerLng + 0.0040,
            batteryPercent = 95,
            statusMessage = "📚 At local school library",
            avatarEmoji = "👧",
            colorHex = "#10B981", // Emerald
            speed = 0.0
        )
    )

    val defaultGeofences = listOf(
        GeofenceEntity(
            id = 1,
            name = "Home Base",
            latitude = centerLat,
            longitude = centerLng,
            radiusMeters = 150.0,
            emoji = "🏠"
        ),
        GeofenceEntity(
            id = 2,
            name = "Central Metro Library",
            latitude = centerLat + 0.0062,
            longitude = centerLng + 0.0042,
            radiusMeters = 120.0,
            emoji = "📚"
        ),
        GeofenceEntity(
            id = 3,
            name = "Coffee Stop",
            latitude = centerLat + 0.0034,
            longitude = centerLng - 0.0022,
            radiusMeters = 100.0,
            emoji = "☕"
        )
    )

    val initialLogs = listOf(
        TrackingLogEntity(memberName = "System", event = "✅ Family radar channel activated successfully.")
    )

    memberDao.insertAll(defaultMembers)
    geofenceDao.insertAll(defaultGeofences)
    for (log in initialLogs) {
        logDao.insertLog(log)
    }
}
