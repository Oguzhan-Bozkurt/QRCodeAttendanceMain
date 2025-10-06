package com.example.qrkodlayoklama.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = @Index(value = "username", unique = true))
public class User {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull public String username = "";
    @NonNull public String password = "";
    @NonNull public String role = "STUDENT";
}
