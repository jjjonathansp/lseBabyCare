package com.example.lsbabycare.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.lsbabycare.models.UserData;

@Dao
public interface UserDataDao {
    @Insert
    void insert(UserData userData);

    @Update
    void update(UserData userData);

    @Query("SELECT * FROM user_data LIMIT 1")
    UserData getUserData();
}