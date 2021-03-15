package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "editors")
public class Editors {
    @PrimaryKey @NonNull
    private String userId;
    @Ignore
    public Editors() {
    }

    public Editors(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
}
