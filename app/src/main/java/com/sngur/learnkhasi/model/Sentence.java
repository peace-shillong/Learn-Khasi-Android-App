package com.sngur.learnkhasi.model;

import com.google.firebase.database.ServerValue;

import java.util.List;

public class Sentence {

    //simple sentence
    private String sentence,userId,audioUrl;
    private int reported;
    private Long timestamp;
    //private List<String> translatedSentencesId;
    private boolean translatedToKhasi;
    private int noOfVotes;
    private String userName,category;
    private boolean translatedToEnglish,translatedToHindi,translatedToGaro;

    public Sentence(){

    }

    public Sentence(String sentence, String userId, String audioUrl, int reported, Long timestamp, boolean translatedToKhasi, int noOfVotes, String userName, String category, boolean translatedToEnglish, boolean translatedToHindi, boolean translatedToGaro) {
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
