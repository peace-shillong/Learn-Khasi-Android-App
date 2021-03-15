package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class UserWordReported {
    @PrimaryKey
    @NonNull
    private String wordId;
    private String fromLanguage; // this will identify which node/path we are talking about in firebase database
    private boolean reported;//true means sentence was reported by user

    @Ignore
    public UserWordReported() {
    }

    public UserWordReported(@NonNull String wordId, String fromLanguage, boolean reported) {
        this.wordId = wordId;
        this.fromLanguage = fromLanguage;
        this.reported = reported;
    }

    @NonNull
    public String getWordId() {
        return wordId;
    }

    public void setWordId(@NonNull String wordId) {
        this.wordId = wordId;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }
}
