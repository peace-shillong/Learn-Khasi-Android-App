package com.sngur.learnkhasi.model;

public class User {
    private String uId,name,email,picture,survey1,survey2,joinedIn;
    private int points,levelId,badgeId,english,hindi,garo,khasi;

    public User() {
    }

    public User(String uId, String name, String email, String picture, String survey1, String survey2, String joinedIn, int points, int levelId, int badgeId, int english, int hindi, int garo, int khasi) {
        this.uId = uId;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.survey1 = survey1;
        this.survey2 = survey2;
        this.joinedIn = joinedIn;
        this.points = points;
        this.levelId = levelId;
        this.badgeId = badgeId;
        this.english = english;
        this.hindi = hindi;
        this.garo = garo;
        this.khasi = khasi;
    }

    public String getJoinedIn() {
        return joinedIn;
    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPicture() {
        return picture;
    }

    public String getSurvey1() {
        return survey1;
    }

    public String getSurvey2() {
        return survey2;
    }

    public int getPoints() {
        return points;
    }

    public int getLevelId() {
        return levelId;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public int getEnglish() {
        return english;
    }

    public int getHindi() {
        return hindi;
    }

    public int getGaro() {
        return garo;
    }

    public int getKhasi() {
        return khasi;
    }
}
