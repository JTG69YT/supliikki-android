package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.adapters.BigSliderAdapter;
import fi.jesunmaailma.supliikki.adapters.PodcastAdapter;
import fi.jesunmaailma.supliikki.models.BigSlider;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.services.SupliikkiDataService;
import jp.wasabeef.picasso.transformations.BlurTransformation;

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

    ImageView backdropImg, rewind, playBtn, pauseBtn, forward, podcastThumbnail, playerThumbnail;
    TextView nowPlayingPodcastName, podcastName, podcastDescription, tvPosition, tvDuration;
    SeekBar seekBar;

    ExoPlayer player;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;

    CardView errorContainer;

    ProgressBar pbLoading, pbLoadingSlider, pbLoadingPodcasts;

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

        backdropImg = findViewById(R.id.backdropImg);
        podcastThumbnail = findViewById(R.id.podcastThumbnail);

        playerThumbnail = findViewById(R.id.playerThumbnail);

        rewind = findViewById(R.id.rewind);
        forward = findViewById(R.id.forward);

        podcastName = findViewById(R.id.podcastName);
        podcastDescription = findViewById(R.id.podcastDescription);
        nowPlayingPodcastName = findViewById(R.id.nowPlayingPodcastName);

        tvPosition = findViewById(R.id.tvPosition);
        tvDuration = findViewById(R.id.tvDuration);

        seekBar = findViewById(R.id.seekBar);

        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);

        service = new SupliikkiDataService(this);

        toolbar = findViewById(R.id.toolbar);

        clRoot = findViewById(R.id.clRoot);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        errorContainer = findViewById(R.id.error_container);

        pbLoading = findViewById(R.id.pb_loading);
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
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getMediaItemCount() > 0) {
                    player.play();
                    playBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    pauseBtn.setVisibility(View.GONE);
                    playBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = (int) player.getCurrentPosition();
                int duration = (int) player.getDuration();
                if (player.isPlaying() && duration > 5000) {
                    currentPosition = currentPosition - 5000;
                    tvPosition.setText(getReadableTime(currentPosition));
                    player.seekTo(currentPosition);
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = (int) player.getCurrentPosition();
                int duration = (int) player.getDuration();
                if (player.isPlaying() && duration != currentPosition) {
                    currentPosition = currentPosition + 5000;
                    tvPosition.setText(getReadableTime(currentPosition));
                    player.seekTo(player.getCurrentPosition());
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                }
                tvPosition.setText(getReadableTime((int) player.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (player.isPlaying()) {
            nowPlayingPodcastName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
            podcastName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
            podcastDescription.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.description);
            tvPosition.setText(getReadableTime((int) player.getCurrentPosition()));
            seekBar.setProgress((int) player.getCurrentPosition());
            tvDuration.setText(getReadableTime((int) player.getDuration()));
            seekBar.setMax((int) player.getDuration());
            playBtn.setVisibility(View.GONE);
            pauseBtn.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                    .placeholder(R.drawable.supliikki_placeholder_512x512)
                    .transform(new BlurTransformation(MainActivity.this, 20))
                    .into(backdropImg);

            Picasso.get()
                    .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                    .placeholder(R.drawable.supliikki_placeholder_512x512)
                    .into(playerThumbnail);

            Picasso.get()
                    .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                    .placeholder(R.drawable.supliikki_placeholder_512x512)
                    .into(podcastThumbnail);

            updatePlayerPositionProgress();
        }

        playerListener();
    }

    private void playerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    playBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    pbLoading.setVisibility(View.VISIBLE);
                } else if (playbackState == ExoPlayer.STATE_READY) {
                    playBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.VISIBLE);
                    pbLoading.setVisibility(View.GONE);

                    Picasso.get()
                            .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                            .placeholder(R.drawable.supliikki_placeholder_512x512)
                            .transform(new BlurTransformation(MainActivity.this, 20))
                            .into(backdropImg);

                    Picasso.get()
                            .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                            .placeholder(R.drawable.supliikki_placeholder_512x512)
                            .into(playerThumbnail);

                    Picasso.get()
                            .load(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri)
                            .placeholder(R.drawable.supliikki_placeholder_512x512)
                            .into(podcastThumbnail);

                    nowPlayingPodcastName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    podcastName.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    podcastDescription.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.description);
                    tvPosition.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekBar.setProgress((int) player.getCurrentPosition());
                    tvDuration.setText(getReadableTime((int) player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                    updatePlayerPositionProgress();
                }
            }
        });
    }

    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    tvPosition.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekBar.setProgress((int) player.getCurrentPosition());
                }

                //repeat calling method
                updatePlayerPositionProgress();
            }
        }, 1000);
    }

    @SuppressLint("DefaultLocale")
    private String getReadableTime(int totalDuration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(totalDuration),
                TimeUnit.MILLISECONDS.toSeconds(totalDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration)));
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