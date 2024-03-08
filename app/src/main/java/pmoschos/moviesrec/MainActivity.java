package pmoschos.moviesrec;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pmoschos.moviesrec.adapters.MovieAdapter;
import pmoschos.moviesrec.models.Movie;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private ArrayList<Movie> movieArrayList;
    private TextView userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);
        recyclerView = findViewById(R.id.recyclerView);
        userId = findViewById(R.id.userId);

        movieArrayList = new ArrayList<>();
        movieAdapter = new MovieAdapter(getApplicationContext(), movieArrayList);
        recyclerView.setAdapter(movieAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editText.getText().toString().trim();
                if (userInput.isEmpty()) {
                    editText.setError("Please enter a user ID");
                } else {
                    userId.setText("Selected user id: " + userInput);
                    fetchMovieRecommendations(userInput);
                    // fetchMovieRecommendations("7");
                }
            }

        });
    }

    private void fetchMovieRecommendations(String userId) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "https://movies-rec.onrender.com/api/user_id/" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        movieArrayList.clear();
                        JSONArray jsonArray = response.getJSONArray("recommended_movies");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String movieTitle = jsonArray.getString(i);
                            Movie movie = new Movie(movieTitle);
                            movieArrayList.add(movie);
                        }
                        movieAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing movie list", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show()){

            // Overriding the getRetryPolicy method to increase the timeout
            @Override
            public RetryPolicy getRetryPolicy() {
                // Here you can try a higher timeout if 5 seconds doesn't work
                return new DefaultRetryPolicy(
                        5000, // Initial timeout in milliseconds (5 seconds)
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Maximum number of retry attempts
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT); // Backoff multiplier
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

}