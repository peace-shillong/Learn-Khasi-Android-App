package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_sentence_reported")
public class UserSentenceReported {
    @PrimaryKey
    @NonNull
    private String sentenceId;
    private String fromLanguage; // this will identify which node/path we are talking about in firebase database
    private boolean reported;//true means sentence was reported by user

    @Ignore
    public UserSentenceReported() {

    }

    public UserSentenceReported(@NonNull String sentenceId, String fromLanguage, boolean reported) {
        this.sentenceId = sentenceId;
        this.fromLanguage = fromLanguage;
        this.reported = reported;
    }

    @NonNull
    public String getSentenceId() {
        return sentenceId;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public boolean isReported() {
        return reported;
    }

    public void setSentenceId(@NonNull String sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }
}
