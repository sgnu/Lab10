package edu.temple.bookcase;

public class Book {
    private String title, author, coverURL;
    private int id, published, duration, position;
    private boolean downloaded;

    public Book(String title, String author, String coverURL, int id, int published, int duration) {
        this.title = title;
        this.author = author;
        this.coverURL = coverURL;
        this.id = id;
        this.published = published;
        this.duration = duration;
    }

    public void setDownloaded(boolean bool) {
        this.downloaded = bool;
    }

    public void setPosition(int position) {
        this.position = position - 30;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverURL() { return coverURL; }
    public int getId() { return id; }
    public int getPublished() { return published; }
    public int getDuration() { return duration; }
    public int getPosition() { return position; }
    public boolean getDownloaded() { return downloaded; }
}