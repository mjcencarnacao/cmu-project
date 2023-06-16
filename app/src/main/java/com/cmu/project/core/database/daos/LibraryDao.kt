package com.cmu.project.core.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cmu.project.core.database.entities.LibraryEntity

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library")
    fun getAll(): List<LibraryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(libraryEntity: LibraryEntity)

    @Delete
    fun delete(libraryEntity: LibraryEntity)

    @Query("SELECT (SELECT COUNT(*) FROM library) == 0")
    fun isEmpty(): Boolean

    @Query("SELECT * FROM library WHERE id LIKE :id LIMIT 1")
    fun findById(id: String): LibraryEntity

    @Query("SELECT * FROM library WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): LibraryEntity

    @Query("DELETE FROM library")
    fun deleteAll()

}