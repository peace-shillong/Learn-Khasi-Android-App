package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_word_voted")
public class UserWordVoted {
    @PrimaryKey
    @NonNull
    private String wordId;
    private String fromLanguage; // this will identify which node/path we are talking about in firebase database
    private boolean vote;//true means upVote false means downVote

    @Ignore
    public UserWordVoted() {
    }

    public UserWordVoted(@NonNull String wordId, String fromLanguage, boolean vote) {
        this.wordId = wordId;
        this.fromLanguage = fromLanguage;
        this.vote = vote;
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

    public boolean isVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }
}
