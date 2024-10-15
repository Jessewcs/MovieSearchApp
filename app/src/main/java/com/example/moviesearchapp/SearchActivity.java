package com.example.moviesearchapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
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

        // Handle back button pressed
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

    private void searchMovies(String movieName) {
        // To be implemented
    }



















    private void shutdownAndTerminateSearch(ExecutorService executorService) {
        // To be implemented
    }

    private void hideKeyboard(View v) {
        // To be implemented
    }



}