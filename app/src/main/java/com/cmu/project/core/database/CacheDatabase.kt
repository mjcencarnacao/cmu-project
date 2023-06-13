package com.cmu.project.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cmu.project.core.database.daos.BookDao
import com.cmu.project.core.database.daos.LibraryDao
import com.cmu.project.core.database.daos.UserDao
import com.cmu.project.core.database.entities.BookEntity
import com.cmu.project.core.database.entities.LibraryEntity
import com.cmu.project.core.database.entities.UserEntity

@Database(entities = [LibraryEntity::class, BookEntity::class, UserEntity::class], version = 1, exportSchema = false)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun libraryDao(): LibraryDao

    companion object {

        private const val DATABASE_NAME: String = "Cache"

        @Volatile
        private var INSTANCE: CacheDatabase? = null

        @Synchronized
        fun getInstance(context: Context): CacheDatabase {
            if (INSTANCE != null)
                return INSTANCE as CacheDatabase
            return Room.databaseBuilder(context.applicationContext, CacheDatabase::class.java, DATABASE_NAME).build()
        }
    }
}