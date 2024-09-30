package com.example.lsbabycare.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.lsbabycare.dao.UserDataDao;
import com.example.lsbabycare.models.UserData;

@Database(entities = {UserData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDataDao userDataDao();
}
