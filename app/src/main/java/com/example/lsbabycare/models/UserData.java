package com.example.lsbabycare.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_data")
public class UserData {

    @PrimaryKey
    private int id = 1;
    private int range;
    private int secsBeforeVibrate;

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }
    public int getSecsBeforeVibrate() { return secsBeforeVibrate; }
    public void setSecsBeforeVibrate(int secsBeforeVibrate) { this.secsBeforeVibrate = secsBeforeVibrate; }
}
