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
        // To be implemented
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