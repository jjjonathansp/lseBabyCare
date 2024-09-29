package com.example.lsbabycare.models;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserData implements Serializable {
    private Integer range;
    private Integer secsBeforeVibrate;
}
