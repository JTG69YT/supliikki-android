package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.adapters.BigSliderAdapter;
import fi.jesunmaailma.supliikki.adapters.PodcastAdapter;
import fi.jesunmaailma.supliikki.models.BigSlider;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.services.SupliikkiDataService;

public class MainActivity extends AppCompatActivity {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGE = "image";

    FirebaseAnalytics analytics;

    CoordinatorLayout clRoot;

    ViewPager2 bigSliderList;
    RecyclerView podcastList;
    BigSliderAdapter bigSliderAdapter;
    PodcastAdapter podcastAdapter;
    List<BigSlider> promoItemsList;
    List<Podcast> podcasts;

    ConstraintLayout playerWrapper;
    ImageView podcastThumbnail;
    TextView podcastName;
    ExoPlayer player;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    CardView play, pause, errorContainer;

    ProgressBar pbLoadingAudio, pbLoadingSlider, pbLoadingPodcasts;

    SupliikkiDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        podcastList = findViewById(R.id.podcasts_list);
        podcasts = new ArrayList<>();
        player = new ExoPlayer.Builder(this).build();

        podcastAdapter = new PodcastAdapter(podcasts, player);
        podcastList.setAdapter(podcastAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        podcastList.setLayoutManager(manager);

        playerWrapper = findViewById(R.id.playerWrapper);
        podcastThumbnail = playerWrapper.findViewById(R.id.playerThumbnail);
        podcastName = playerWrapper.findViewById(R.id.playerName);

        play = playerWrapper.findViewById(R.id.play);
        pause = playerWrapper.findViewById(R.id.pause);

        pbLoadingAudio = playerWrapper.findViewById(R.id.pbLoadingAudio);

        service = new SupliikkiDataService(this);

        toolbar = findViewById(R.id.toolbar);

        clRoot = findViewById(R.id.clRoot);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        errorContainer = findViewById(R.id.error_container);

        pbLoadingSlider = findViewById(R.id.pbLoadingSlider);
        pbLoadingPodcasts = findViewById(R.id.pbLoadingPodcasts);

        setSupportActionBar(toolbar);

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, NAME);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, IMAGE);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        bigSliderList = findViewById(R.id.big_slider_list);
        promoItemsList = new ArrayList<>();

        bigSliderAdapter = new BigSliderAdapter(this, promoItemsList);
        bigSliderList.setAdapter(bigSliderAdapter);

        pbLoadingSlider.setVisibility(View.VISIBLE);
        pbLoadingPodcasts.setVisibility(View.VISIBLE);

        getSliderData(
                getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliikki_api_key) +
                        "&promoItems=all&user_id=1"
        );
        getPodcastData(
                getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliikki_api_key) +
                        "&podcasts=all&user_id=1"
        );

        playerControls();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

                pbLoadingSlider.setVisibility(View.VISIBLE);
                pbLoadingPodcasts.setVisibility(View.VISIBLE);

                errorContainer.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
                getSliderData(getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliikki_api_key) +
                        "&promoItems=all&user_id=1"
                );

                podcastList.setVisibility(View.GONE);
                getPodcastData(getResources().getString(R.string.supliikki_prod_api_url) +
                        "?api_key=" +
                        getResources().getString(R.string.supliikki_api_key) +
                        "&podcasts=all&user_id=1"
                );
                playerControls();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }

    private void getSliderData(String url) {
        service.getPodcastData(url, new SupliikkiDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);
                bigSliderList.setVisibility(View.VISIBLE);

                promoItemsList.clear();

                try {
                    JSONObject promoItemData = response.getJSONObject(0);

                    BigSlider bigSlider = new BigSlider();

                    bigSlider.setId(promoItemData.getString("id"));
                    bigSlider.setName(promoItemData.getString("name"));
                    bigSlider.setDescription(promoItemData.getString("description"));
                    bigSlider.setLink(promoItemData.getString("link"));
                    bigSlider.setThumbnailUrl(promoItemData.getString("thumbnail_url"));
                    bigSlider.setBackdropUrl(promoItemData.getString("backdrop_url"));

                    promoItemsList.add(bigSlider);
                    bigSliderAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);

                pbLoadingSlider.setVisibility(View.GONE);
                pbLoadingPodcasts.setVisibility(View.GONE);

                bigSliderList.setVisibility(View.GONE);
            }
        });
    }

    private void getPodcastData(String url) {
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

    private void playerControls() {
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    pause.setVisibility(View.GONE);
                    play.setVisibility(View.VISIBLE);
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaItemCount() > 0) {
                    player.play();
                    play.setVisibility(View.GONE);
                    pause.setVisibility(View.VISIBLE);
                }
            }
        });

        playerListener();
    }

    private void playerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                assert mediaItem != null;

                Picasso.get()
                        .load(mediaItem.mediaMetadata.artworkUri)
                        .placeholder(R.drawable.supliikki_placeholder_512x512)
                        .into(podcastThumbnail);

                podcastName.setText(mediaItem.mediaMetadata.title);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    playerWrapper.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                    pause.setVisibility(View.GONE);
                    pbLoadingAudio.setVisibility(View.VISIBLE);
                } else if (playbackState == ExoPlayer.STATE_READY) {
                    play.setVisibility(View.GONE);
                    pause.setVisibility(View.VISIBLE);
                    pbLoadingAudio.setVisibility(View.GONE);

                    Picasso.get()
                            .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                            .placeholder(R.drawable.supliikki_placeholder_512x512)
                            .into(podcastThumbnail);

                    podcastName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nav_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mi_hosts) {
            startActivity(new Intent(getApplicationContext(), HostsActivity.class));
        }

        if (item.getItemId() == R.id.mi_settings) {
            startActivity(new Intent(getApplicationContext(), Settings.class));
        }

        if (item.getItemId() == R.id.mi_exit) {
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