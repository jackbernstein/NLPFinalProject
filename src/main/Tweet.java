package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Classifier class that creates a multinomial classifier
 *
 * @author Renae Tamura, Linnea Dahmen, Jack Bernstein
 *
 */
public class Tweet {

  Rating rating;
  Airline airline;
  String tweet;


  public Tweet(Rating rating, Airline airline, String tweet) {
    this.rating = rating;
    this.airline = airline;
    this.tweet = tweet;
  }

  public String toString() {
    return "This is a " + rating + " tweet about " + airline + ", and the tweet is: " + tweet;
  }

  public String getTweet() {
    return tweet;
  }

  public Rating getRating() {
    return rating;
  }

  public Airline getAirline() {
    return airline;
  }



}
