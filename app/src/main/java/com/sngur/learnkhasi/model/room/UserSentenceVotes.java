package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_sentence_votes")
public class UserSentenceVotes {

    @PrimaryKey @NonNull
    private String sentenceId;
    //private String sentence;//Not required we will not keep
    private String fromLanguage; // this will identify which node/path we are talking about in firebase database
    private boolean vote;//true means upVote false means downVote
    @Ignore
    public UserSentenceVotes() {
    }

    public UserSentenceVotes(String sentenceId, String fromLanguage, boolean vote) {
        this.sentenceId = sentenceId;
        this.fromLanguage = fromLanguage;
        this.vote = vote;
    }

    public void setSentenceId(String sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public boolean isVote() {
        return vote;
    }
}
