package com.shakir.xedin.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.michaelflisar.changelog.ChangelogBuilder;
import com.shakir.xedin.BuildConfig;
import com.shakir.xedin.R;
import com.shakir.xedin.adapters.MoviesAdapter;
import com.shakir.xedin.interfaces.MoviesApiService;
import com.shakir.xedin.models.Movie;
import com.shakir.xedin.utils.MovieResult;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    int flag = 1;
    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private LinearLayout search;
    private LinearLayout main;
    private EditText searcher;
    private Button switcher;
    private int mode = 0;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (main.getVisibility() == View.GONE) {
                        main.setVisibility(View.VISIBLE);
                    }
                    search.setVisibility(View.GONE);
                    if (flag != 1) {
                        UseRest("movie");
                        flag = 1;
                    }
                    return true;
                case R.id.navigation_dashboard:
                    if (main.getVisibility() == View.GONE) {
                        main.setVisibility(View.VISIBLE);
                    }
                    search.setVisibility(View.GONE);
                    if (flag != 0) {
                        UseRest("tv");
                        flag = 0;
                    }
                    return true;
                case R.id.navigation_notifications:
                    if (main.getVisibility() == View.GONE) {
                        main.setVisibility(View.VISIBLE);
                    }
                    if (flag != 2) {
                        search.setVisibility(View.VISIBLE);
                        searcher.setText("");
                        mRecyclerView.setVisibility(View.GONE);
                        searcher.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mode == 0) {
                                    UseRest("movie", searcher.getText().toString());
                                } else {
                                    UseRest("tv", searcher.getText().toString());
                                }
                            }
                        });
                        flag = 2;
                    }
                    return true;
                case R.id.navigation_about:
                    Intent about = new Intent(MainActivity.this, AboutActivity.class);
                    MainActivity.this.startActivity(about);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTheme(R.style.AppTheme);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayOptions
                    (ActionBar.DISPLAY_SHOW_CUSTOM);
            actionbar.setCustomView(R.layout.action_bar);
            actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#10101a")));
        }
        getWindow().setNavigationBarColor(Color.parseColor("#10101a"));
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        if (isFirstRun) {
            Intent intro = new Intent(this, IntroActivity.class);
            startActivity(intro);
            ChangelogBuilder builder = new ChangelogBuilder()
                    .withTitle("Xedin Changelog")
                    .withOkButtonLabel("Yeah Yeah")
                    .withUseBulletList(true);
            builder.buildAndShowDialog(this, true);
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();

        mRecyclerView = findViewById(R.id.recyclerView);
        searcher = findViewById(R.id.searcher);
        search = findViewById(R.id.search);
        main = findViewById(R.id.mained);
        switcher = findViewById(R.id.switcher);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = new MoviesAdapter(this);
        mRecyclerView.setAdapter(new AlphaInAnimationAdapter(new ScaleInAnimationAdapter(mAdapter)));
        mRecyclerView.getLayoutManager();
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == 0) {
                    switcher.setText("Movies");
                    searcher.setHint("TV shows");
                    mode = 1;
                } else {
                    switcher.setText("TV");
                    searcher.setHint("Movies");
                    mode = 0;
                }
            }
        });
        //.setMeasurementCacheEnabled(false);
        UseRest("movie");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void UseRest(String on) {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            movies.add(new Movie());
        }
        mAdapter.setMovieList(movies);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.themoviedb.org/3")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addEncodedQueryParam("api_key", BuildConfig.API_KEY);
                    }
                })
                .build();
        MoviesApiService service = restAdapter.create(MoviesApiService.class);
        if (on.equals("movie")) {
            service.getPopularMovies(new Callback<MovieResult>() {
                @Override
                public void success(MovieResult movieResult, Response response) {
                    mAdapter.setMovieList(movieResult.getResults());
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        } else {
            service.getPopularShows(new Callback<MovieResult>() {
                @Override
                public void success(MovieResult movieResult, Response response) {
                    mAdapter.setMovieList(movieResult.getResults());
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        }
    }

    public void UseRest(String on, String searchQuery) {
        List<Movie> movies = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            movies.add(new Movie());
        }
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.themoviedb.org/3")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addEncodedQueryParam("api_key", BuildConfig.API_KEY);
                        request.addEncodedQueryParam("query", searchQuery);
                    }
                })
                .build();
        MoviesApiService service = restAdapter.create(MoviesApiService.class);
        if (on.equals("movie")) {
            service.getSearchMovies(new Callback<MovieResult>() {
                @Override
                public void success(MovieResult movieResult, Response response) {
                    mAdapter.setMovieList(movieResult.getResults());
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        } else {
            service.getPopularShows(new Callback<MovieResult>() {
                @Override
                public void success(MovieResult movieResult, Response response) {
                    mAdapter.setMovieList(movieResult.getResults());
                    mRecyclerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(RetrofitError error) {
                    error.printStackTrace();
                }
            });
        }
    }
}