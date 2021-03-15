package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//To Store ENG, KHASI, GARO and HINDI
@Entity(tableName = "sentences")
public class StoredSentence {
    @PrimaryKey
    @NonNull
    private String sentenceId;
    private String fromLang; //Store Values English, Khasi, Garo, Hindi
    private String sentence,userId,audioUrl;
    private int reported;
    private Long timestamp;
    private boolean translatedToKhasi;
    private int noOfVotes;
    private String userName,category;
    private boolean translatedToEnglish,translatedToHindi,translatedToGaro;

    public StoredSentence(@NonNull String sentenceId,String fromLang,String sentence, String userId, String audioUrl, int reported, Long timestamp, boolean translatedToKhasi, int noOfVotes, String userName, String category, boolean translatedToEnglish, boolean translatedToHindi, boolean translatedToGaro) {
        this.sentenceId=sentenceId;
        this.fromLang=fromLang;
        this.sentence = sentence;
        this.userId = userId;
        this.audioUrl = audioUrl;
        this.reported = reported;
        this.timestamp = timestamp;
        this.translatedToKhasi = translatedToKhasi;
        this.noOfVotes = noOfVotes;
        this.userName = userName;
        this.category = category;
        this.translatedToEnglish = translatedToEnglish;
        this.translatedToHindi = translatedToHindi;
        this.translatedToGaro = translatedToGaro;
    }

    @NonNull
    public void setSentenceId(String sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public void setReported(int reported) {
        this.reported = reported;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTranslatedToKhasi(boolean translatedToKhasi) {
        this.translatedToKhasi = translatedToKhasi;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTranslatedToEnglish(boolean translatedToEnglish) {
        this.translatedToEnglish = translatedToEnglish;
    }

    public void setTranslatedToHindi(boolean translatedToHindi) {
        this.translatedToHindi = translatedToHindi;
    }

    public void setTranslatedToGaro(boolean translatedToGaro) {
        this.translatedToGaro = translatedToGaro;
    }

    public String getFromLang() {
        return fromLang;
    }

    public void setFromLang(String fromLang) {
        this.fromLang = fromLang;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public String getSentence() {
        return sentence;
    }

    public String getUserId() {
        return userId;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public int getReported() {
        return reported;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public boolean getTranslatedToKhasi() {
        return translatedToKhasi;
    }

    public int getNoOfVotes() {
        return noOfVotes;
    }

    public void setNoOfVotes(int noOfVotes) {
        this.noOfVotes = noOfVotes;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getUserName() {
        return userName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isTranslatedToEnglish() {
        return translatedToEnglish;
    }

    public boolean isTranslatedToHindi() {
        return translatedToHindi;
    }

    public boolean isTranslatedToGaro() {
        return translatedToGaro;
    }
}
