package com.example.moviesearchapp;

public class Movie {
    private String title;
    private String director;
    private String actors;
    private String rating;
    private String language;
    private String year;
    private String type;
    private String genre;
    private String posterUrl;

    public Movie(String title, String director, String actors, String rating, String language, String year, String type, String genre, String posterUrl) {
        this.title = title;
        this.director = director;
        this.actors = actors;
        this.rating = rating;
        this.language = language;
        this.year = year;
        this.type = type;
        this.genre = genre;
        this.posterUrl = posterUrl;
    }

    public Movie() {};

    // Getters
    public String getTitle() { return title; }
    public String getDirector() { return director; }
    public String getActors() { return actors; }
    public String getRating() { return rating; }
    public String getLanguage() { return language; }
    public String getYear() { return year; }
    public String getType() { return type; }
    public String getGenre() { return genre; }
    public String getPosterUrl() { return posterUrl; }


    // Adding method to enable adding listener to cardview for each movie
    public void onItemClick(int position) {
        return;
    }
}
