package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.adapters.BigSliderAdapter;
import fi.jesunmaailma.supliikki.adapters.PodcastAdapter;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.services.SupliikkiDataService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGE = "image";

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore database;
    FirebaseAnalytics analytics;
    DocumentReference documentReference;

    GoogleSignInClient client;

    CoordinatorLayout clRoot;

    ViewPager2 bigSliderList;
    DotsIndicator dotsIndicator;
    RecyclerView podcastList;
    BigSliderAdapter bigSliderAdapter;
    PodcastAdapter podcastAdapter;
    List<Podcast> podcastSliderList, podcasts;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    CardView errorContainer;

    ProgressBar pbLoadingSlider, pbLoadingPodcasts;

    SupliikkiDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service = new SupliikkiDataService(this);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        clRoot = findViewById(R.id.clRoot);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        errorContainer = findViewById(R.id.error_container);

        pbLoadingSlider = findViewById(R.id.pbLoadingSlider);
        pbLoadingPodcasts = findViewById(R.id.pbLoadingPodcasts);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        );

        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseFirestore.getInstance();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, NAME);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, IMAGE);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        client = GoogleSignIn.getClient(MainActivity.this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        dotsIndicator = findViewById(R.id.dotsIndicator);

        bigSliderList = findViewById(R.id.big_slider_list);
        podcastSliderList = new ArrayList<>();

        bigSliderAdapter = new BigSliderAdapter(this, podcastSliderList, auth, user);
        bigSliderList.setAdapter(bigSliderAdapter);
        dotsIndicator.setViewPager2(bigSliderList);

        pbLoadingSlider.setVisibility(View.VISIBLE);
        pbLoadingPodcasts.setVisibility(View.VISIBLE);

        getSliderData(
                getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliiikki_api_key) +
                        "&podcasts=all&user_id=1"
        );
        getPodcastData(
                getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliiikki_api_key) +
                        "&podcasts=all&user_id=1"
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

                pbLoadingSlider.setVisibility(View.VISIBLE);
                pbLoadingPodcasts.setVisibility(View.VISIBLE);

                errorContainer.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
                dotsIndicator.setVisibility(View.GONE);
                getSliderData(getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliiikki_api_key) +
                        "&podcasts=all&user_id=1");

                podcastList.setVisibility(View.GONE);
                getPodcastData(getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliiikki_api_key) +
                        "&podcasts=all&user_id=1");
            }
        });

        UpdateNavHeader();
    }

    public void UpdateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ImageView ivUserPic = headerView.findViewById(R.id.ivUserPic);
        TextView tvName = headerView.findViewById(R.id.tv_name);
        TextView tvSignIn = headerView.findViewById(R.id.tv_login);
        MaterialButton tvSignOut = headerView.findViewById(R.id.tv_sign_out);

        if (user != null) {
            tvName.setVisibility(View.VISIBLE);
            tvSignIn.setVisibility(View.GONE);
            tvSignOut.setVisibility(View.VISIBLE);

            documentReference = database
                    .collection("Users")
                    .document(user.getUid());

            documentReference
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            if (snapshot.exists()) {
                                ivUserPic.setImageResource(R.drawable.ic_user_2);
                                tvName.setText(
                                        String.format(
                                                "Kirjauduit sisään tilillä:\n%s %s",
                                                snapshot.getString("firstName"),
                                                snapshot.getString("lastName")
                                        )
                                );
                            } else {
                                Picasso.get()
                                        .load(user.getPhotoUrl())
                                        .into(ivUserPic);
                                tvName.setText(
                                        String.format(
                                                "Kirjauduit sisään tilillä:\n%s",
                                                user.getDisplayName()
                                        )
                                );
                            }
                        }
                    });
        } else {
            ivUserPic.setImageResource(R.drawable.ic_user_2);
            tvName.setVisibility(View.GONE);
            tvSignIn.setVisibility(View.VISIBLE);
            tvSignOut.setVisibility(View.GONE);
        }

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
                Intent intent = new Intent(getApplicationContext()
                        , Login.class);
                startActivity(intent);
            }
        });

        tvSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer(drawerLayout);
                auth.signOut();

                client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(
                                new Intent(
                                        getApplicationContext(),
                                        MainActivity.class
                                )
                        );
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
            }
        });
    }

    private void getSliderData(String url) {
        service.getPodcastData(url, new SupliikkiDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);
                bigSliderList.setVisibility(View.VISIBLE);
                dotsIndicator.setVisibility(View.VISIBLE);

                podcastSliderList.clear();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject podcastData = response.getJSONObject(i);

                        Podcast podcast = new Podcast();

                        podcast.setId(podcastData.getString("id"));
                        podcast.setName(podcastData.getString("name"));
                        podcast.setDescription(podcastData.getString("description"));
                        podcast.setThumbnailUrl(podcastData.getString("thumbnail_url"));
                        podcast.setBackdropUrl(podcastData.getString("backdrop_url"));
                        podcast.setPodcastUrl(podcastData.getString("podcast_url"));

                        podcastSliderList.add(podcast);
                        bigSliderAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
                dotsIndicator.setVisibility(View.GONE);
            }
        });
    }

    private void getPodcastData(String url) {
        podcastList = findViewById(R.id.podcasts_list);
        podcasts = new ArrayList<>();

        podcastAdapter = new PodcastAdapter(podcasts, auth, user);
        podcastList.setAdapter(podcastAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        podcastList.setLayoutManager(manager);

        service.getPodcastData(url, new SupliikkiDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);
                podcastList.setVisibility(View.VISIBLE);

                podcasts.clear();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject podcastData = response.getJSONObject(i);

                        Podcast podcast = new Podcast();

                        podcast.setId(podcastData.getString("id"));
                        podcast.setName(podcastData.getString("name"));
                        podcast.setDescription(podcastData.getString("description"));
                        podcast.setThumbnailUrl(podcastData.getString("thumbnail_url"));
                        podcast.setBackdropUrl(podcastData.getString("backdrop_url"));
                        podcast.setPodcastUrl(podcastData.getString("podcast_url"));

                        podcasts.add(podcast);
                        podcastAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);

                podcastList.setVisibility(View.GONE);
            }
        });
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_home) {
            closeDrawer(drawerLayout);
        }

        if (item.getItemId() == R.id.mi_hosts) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), HostsActivity.class));
        }

        if (item.getItemId() == R.id.mi_settings) {
            closeDrawer(drawerLayout);
            startActivity(new Intent(getApplicationContext(), Settings.class));
        }

        if (item.getItemId() == R.id.mi_exit) {
            closeDrawer(drawerLayout);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Haluatko varmasti poistua Supliikki-palvelusta?");
            builder.setNegativeButton("Kyllä", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton("Ei", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return false;
    }
}