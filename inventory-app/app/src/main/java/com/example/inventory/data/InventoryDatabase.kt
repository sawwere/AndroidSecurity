package com.example.inventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.commonsware.cwac.saferoom.SQLCipherUtils
import net.sqlcipher.database.SupportFactory

@Database(entities = [Item::class], version = 2, exportSchema = false)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var Instance: InventoryDatabase? = null

        private const val DATABASE_NAME = "item_database"

        fun getDatabase(context: Context, key: ByteArray): InventoryDatabase {
            return Instance ?: synchronized(this) {
                val databaseState = SQLCipherUtils.getDatabaseState(context, DATABASE_NAME)
                if (databaseState == SQLCipherUtils.State.UNENCRYPTED) {
                    SQLCipherUtils.encrypt(context, DATABASE_NAME, key)
                }

                val factory = SupportFactory(key)

                Room.databaseBuilder(context, InventoryDatabase::class.java, DATABASE_NAME)
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE items ADD COLUMN source TEXT NOT NULL DEFAULT 'MANUAL'")
    }
}
