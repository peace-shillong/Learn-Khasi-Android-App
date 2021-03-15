package com.sngur.learnkhasi.model.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_word")
public class FavoriteWord {
    @PrimaryKey @NonNull
    private String wordId;
    private String word,userId,audioUrl;
    private int reported;
    private Long timestamp;
    private int noOfVotes;
    private String userName,wordType;
    private String englishMeaning,khasiMeaning;
    private boolean translatedToKhasi,translatedToEnglish,translatedToHindi,translatedToGaro;
    private String fromLanguage,toLanguage;//extra for intent

    @Ignore
    public FavoriteWord() {
    }

    public FavoriteWord(String wordId, String word, String userId, String audioUrl, int reported, Long timestamp, int noOfVotes, String userName, String wordType, String englishMeaning, String khasiMeaning, boolean translatedToKhasi, boolean translatedToEnglish, boolean translatedToHindi, boolean translatedToGaro, String fromLanguage, String toLanguage) {
        this.wordId = wordId;
        this.word = word;
        this.userId = userId;
        this.audioUrl = audioUrl;
        this.reported = reported;
        this.timestamp = timestamp;
        this.noOfVotes = noOfVotes;
        this.userName = userName;
        this.wordType = wordType;
        this.englishMeaning = englishMeaning;
        this.khasiMeaning = khasiMeaning;
        this.translatedToKhasi = translatedToKhasi;
        this.translatedToEnglish = translatedToEnglish;
        this.translatedToHindi = translatedToHindi;
        this.translatedToGaro = translatedToGaro;
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getReported() {
        return reported;
    }

    public void setReported(int reported) {
        this.reported = reported;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNoOfVotes() {
        return noOfVotes;
    }

    public void setNoOfVotes(int noOfVotes) {
        this.noOfVotes = noOfVotes;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getWordType() {
        return wordType;
    }

    public void setWordType(String wordType) {
        this.wordType = wordType;
    }

    public String getEnglishMeaning() {
        return englishMeaning;
    }

    public void setEnglishMeaning(String englishMeaning) {
        this.englishMeaning = englishMeaning;
    }

    public String getKhasiMeaning() {
        return khasiMeaning;
    }

    public void setKhasiMeaning(String khasiMeaning) {
        this.khasiMeaning = khasiMeaning;
    }

    public boolean isTranslatedToKhasi() {
        return translatedToKhasi;
    }

    public void setTranslatedToKhasi(boolean translatedToKhasi) {
        this.translatedToKhasi = translatedToKhasi;
    }

    public boolean isTranslatedToEnglish() {
        return translatedToEnglish;
    }

    public void setTranslatedToEnglish(boolean translatedToEnglish) {
        this.translatedToEnglish = translatedToEnglish;
    }

    public boolean isTranslatedToHindi() {
        return translatedToHindi;
    }

    public void setTranslatedToHindi(boolean translatedToHindi) {
        this.translatedToHindi = translatedToHindi;
    }

    public boolean isTranslatedToGaro() {
        return translatedToGaro;
    }

    public void setTranslatedToGaro(boolean translatedToGaro) {
        this.translatedToGaro = translatedToGaro;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public String getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(String toLanguage) {
        this.toLanguage = toLanguage;
    }
}
