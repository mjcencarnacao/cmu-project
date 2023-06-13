package com.cmu.project.core.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cmu.project.core.database.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUser(): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("DELETE FROM user")
    fun delete()

    @Query("SELECT (SELECT COUNT(*) FROM user) == 0")
    fun isEmpty(): Boolean

}