package com.sngur.learnkhasi.model;

public class Counter {
    //keeps track on the number of words
    private int noOfKhasiWords,noOfEnglishWords,noOfGaroWords,noOfTranslatedSentences,noOfUsers,noOfLessons,appVersionCode;
    private String newFeatures;

    public Counter(int noOfKhasiWords, int noOfEnglishWords, int noOfGaroWords, int noOfTranslatedSentences,int noOfUsers,int noOfLessons,int appVersionCode,String newFeatures) {
        this.noOfKhasiWords = noOfKhasiWords;
        this.noOfEnglishWords = noOfEnglishWords;
        this.noOfGaroWords = noOfGaroWords;
        this.noOfTranslatedSentences=noOfTranslatedSentences;
        this.noOfUsers=noOfUsers;
        this.noOfLessons=noOfLessons;
        this.appVersionCode=appVersionCode;
        this.newFeatures=newFeatures;
    }

    public Counter() {
        this.noOfKhasiWords = 0;
        this.noOfEnglishWords = 0;
        this.noOfGaroWords = 0;
        this.noOfTranslatedSentences=0;
        this.noOfUsers=0;
        this.noOfLessons=0;
        this.appVersionCode=2;
        this.newFeatures="";
    }

    public int getNoOfKhasiWords() {
        return noOfKhasiWords;
    }

    public void setNoOfKhasiWords(int noOfKhasiWords) {
        this.noOfKhasiWords = noOfKhasiWords;
    }

    public int getNoOfEnglishWords() {
        return noOfEnglishWords;
    }

    public void setNoOfEnglishWords(int noOfEnglishWords) {
        this.noOfEnglishWords = noOfEnglishWords;
    }

    public int getNoOfGaroWords() {
        return noOfGaroWords;
    }

    public void setNoOfGaroWords(int noOfGaroWords) {
        this.noOfGaroWords = noOfGaroWords;
    }

    public int getNoOfTranslatedSentences() {
        return noOfTranslatedSentences;
    }

    public void setNoOfTranslatedSentences(int noOfTranslatedSentences) {
        this.noOfTranslatedSentences = noOfTranslatedSentences;
    }

    public int getNoOfUsers() {
        return noOfUsers;
    }

    public void setNoOfUsers(int noOfUsers) {
        this.noOfUsers = noOfUsers;
    }

    public int getNoOfLessons() {
        return noOfLessons;
    }

    public void setNoOfLessons(int noOfLessons) {
        this.noOfLessons = noOfLessons;
    }

    public int getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(int appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getNewFeatures() {
        return newFeatures;
    }

    public void setNewFeatures(String newFeatures) {
        this.newFeatures = newFeatures;
    }
}
