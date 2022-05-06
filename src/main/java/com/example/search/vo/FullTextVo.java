package com.example.search.vo;

public class FullTextVo {
    private String title;
    private String Content;

    public float score;
    private String type;
    private String time;

    public FullTextVo() {
    }

    public FullTextVo(String title, String content, float score, String type, String time) {
        this.title = title;
        Content = content;
        this.score = score;
        this.type = type;
        this.time = time;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
