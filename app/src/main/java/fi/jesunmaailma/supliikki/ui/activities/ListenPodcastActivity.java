package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ListenPodcastActivity extends AppCompatActivity {
    Toolbar toolbar;
    Podcast podcast;
    ImageView backdropImg, podcastLogo, rewind, play, pause, forward;
    TextView podcastName, podcastDescription, tvPosition, tvDuration;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    Handler handler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_podcast);

        podcast = (Podcast) getIntent().getSerializableExtra("podcast");

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        backdropImg = findViewById(R.id.backdropImg);
        podcastLogo = findViewById(R.id.podcastThumbnail);
        rewind = findViewById(R.id.rewind);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        forward = findViewById(R.id.forward);

        podcastName = findViewById(R.id.podcastName);
        podcastDescription = findViewById(R.id.podcastDescription);
        tvPosition = findViewById(R.id.tv_position);
        tvDuration = findViewById(R.id.tv_duration);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(100);

        Glide.with(ListenPodcastActivity.this)
                .load(podcast.getBackdropUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(12, 12)))
                .into(backdropImg);

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .into(podcastLogo);

        podcastName.setText(podcast.getName());
        podcastDescription.setText(podcast.getDescription());

        mediaPlayer = new MediaPlayer();

        String url = podcast.getPodcastUrl();
        playPodcast(url);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                updateSeekbar();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(updater);
                mediaPlayer.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                if (mediaPlayer.isPlaying() && duration != currentPosition) {
                    currentPosition = currentPosition + 5000;
                    tvPosition.setText(millisecondsToTimer(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });

        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying() && currentPosition > 5000) {
                    currentPosition = currentPosition - 5000;
                    tvPosition.setText(millisecondsToTimer(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SeekBar seekBar = (SeekBar) v;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                tvPosition.setText(millisecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }

                tvPosition.setText(millisecondsToTimer(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int i) {
                seekBar.setSecondaryProgress(i);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                seekBar.setProgress(0);
                mediaPlayer.seekTo(0);
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                tvPosition.setText(R.string.zero_seconds);
                tvDuration.setText(R.string.zero_seconds);
                mediaPlayer.reset();
                String url = podcast.getPodcastUrl();
                playPodcast(url);
            }
        });

    }

    private void playPodcast(String url) {
        try {
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepare();
            tvDuration.setText(millisecondsToTimer(mediaPlayer.getDuration()));
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSeekbar();
            long currentDuration = mediaPlayer.getCurrentPosition();
            tvPosition.setText(millisecondsToTimer(currentDuration));
        }
    };

    public void updateSeekbar() {
        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) * 100));
            handler.postDelayed(updater, 0);
        }
    }

    public String millisecondsToTimer(long milliseconds) {
        String timerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondsString;
        return timerString;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}