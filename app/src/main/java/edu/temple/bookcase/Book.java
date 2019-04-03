package edu.temple.bookcase;

public class Book {
    private String title, author, coverURL;
    private int id, published;

    public Book(String title, String author, String coverURL, int id, int published) {
        this.title = title;
        this.author = author;
        this.coverURL = coverURL;
        this.id = id;
        this.published = published;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverURL() { return coverURL; }
    public int getId() { return id; }
    public int getPublished() { return published; }
}