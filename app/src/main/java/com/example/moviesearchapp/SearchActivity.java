package com.example.moviesearchapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class SearchActivity extends AppCompatActivity {

    private EditText movieNameEditText;
    private Spinner spinnerType;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView resultTextView;
    private RecyclerView recyclerView;
    private String movieName;
    private boolean isSearching;
    private List<Movie> movieList;

    // OMDb API Key
    private final String apiKey = "87a49b6f";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        movieNameEditText = findViewById(R.id.movieNameEditText);
        spinnerType = findViewById(R.id.spinnerType);
        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        resultTextView = findViewById(R.id.resultTextView);
        recyclerView = findViewById(R.id.recyclerView);

        movieList = new ArrayList<>(); // Initialize the movie list

        /**
         * hideKeyboard(v): Hides the soft keyboard after the user initiates a search.
         * Input Validation: Checks if the user has entered a movie name.
         * Updating UI: Shows the progress bar and hides any previous messages.
         * Initiating Search: Calls searchMovies with the entered movie name.
         * User Feedback: Shows a Toast message if the input is empty.
         */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);
                movieName = movieNameEditText.getText().toString().trim();
                if (!movieName.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE); // Show ProgressBar
                    recyclerView.setVisibility(View.GONE); // Hide RecyclerView

                    searchMovies(movieName); // Search movies when button is clicked
                } else {
                    Toast.makeText(SearchActivity.this, "Please enter a movie name!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isSearching) {
            searchMovies(movieName);
        }

        /**
         * A custom back button handler.
         * Checks if a search is in progress (isSearching flag), If so, prompts the user with a confirmation dialog.
         * If the user confirms, shuts down the executor service and exits the activity, If not, simply exits the activity.
         * Needed to prevent abrupt termination of ongoing network operations and improve user experience.
         */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSearching) {
                    // Show a confirmation dialog if the search is currently running
                    new AlertDialog.Builder(SearchActivity.this)
                            .setTitle("Confirm Exit")
                            .setMessage("The search is currently running! Are you sure you want to terminate the search and exit?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                shutdownAndTerminateSearch(executorService);
                                finish(); // Finish the activity
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    // If search is not running, just proceed with the back press
                    finish();
                }
            }
        });
    }

    /**
     * Cleans up resources when the activity is destroyed.
     * Shuts down the ExecutorService to prevent memory leaks and stop background tasks.
     * Ensures that background operations do not continue when the activity is no longer active.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownAndTerminateSearch(executorService);
    }

    /**
     * A utility method to run code on the main UI thread.
     * Checks if the activity is still active before executing the Runnable.
     * UI updates must be performed on the main thread to avoid exceptions.
     */
    private void updateUI(Runnable updateTask) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(updateTask);
        }
    }

    private void searchMovies(String movieName) {
        isSearching = true;
        executorService.execute(() -> {
            try {
                String encodedMovieName = URLEncoder.encode(movieName, "UTF-8");
                String urlString = "http://img.omdbapi.com/?s=" + encodedMovieName + "$apikey=" + apiKey;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse and display the movie details
                    updateUI(() -> parseSearchResults(response.toString()));
                } else {
                    updateUI(() -> showError("Failed to fetch movie details! Response code: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                updateUI(() -> showError("An error occurred while fetching the movie details!"));
            }
        });
    }

    /**
     * A method to parse the JSON response from the search query.
     * Converts the response string into a JSONObject.
     * Checks if the response contains a "Search" array.
     * Iterates over the array to extract imdbID for each movie.
     * Calls fetchDetailedMovieInfo(imdbID) for each movie.
     * Needed bc the initial search response provides basic information. Additional details require separate requests.
     */
    private void parseSearchResults(final String searchResponse) {
        updateUI(() -> {
            try {
                JSONObject movieJson = new JSONObject(searchResponse);
                if (movieJson.has("Search")) {
                    JSONArray moviesArray = movieJson.getJSONArray("Search");
                    movieList.clear();
                    for (int i = 0; i < moviesArray.length(); i++) {
                        JSONObject movie = moviesArray.getJSONObject(i);
                        String imdbID = movie.getString("imdbID");

                        // Fetch detailed movie info by IMDb ID
                        fetchDetailedMovieInfo(imdbID);
                    }
                    resultTextView.setVisibility(View.GONE);
                    isSearching = false;
                } else {
                    resultTextView.setText("Movie was not found! Please try again!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultTextView.setText(R.string.search_error_message);
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void fetchDetailedMovieInfo(String imdbID) {
        // To be implemented
    }

    /**
     * Display the error message
     */
    private void showError(final String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        progressBar.setVisibility(View.GONE); // Hide if there is an error
    }

    /**
     * Attempts to gracefully shut down the executor service, waiting for tasks to finish before forcing a shutdown.
     */
    private void shutdownAndTerminateSearch(ExecutorService thread) {
        thread.shutdown();
        try {
            if (!thread.awaitTermination(10, TimeUnit.SECONDS)) {
                thread.shutdownNow();
                if (!thread.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Search did not terminate!");
                }
            }
        } catch (InterruptedException e) {
            thread.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * A utility method to hide the soft keyboard
     * Uses the InputMethodManager to hide the keyboard from the current focus.
     * To improve UI aesthetics after the user initiates a search.
     */
    private void hideKeyboard(View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }
}