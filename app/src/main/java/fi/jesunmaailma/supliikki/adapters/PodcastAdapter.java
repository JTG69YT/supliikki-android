package fi.jesunmaailma.supliikki.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.ui.activities.Profile;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
    Activity activity;
    List<Podcast> podcasts;
    View view;
    ExoPlayer player;

    FirebaseAuth auth;
    FirebaseUser user;

    public PodcastAdapter(Activity activity, List<Podcast> podcasts, ExoPlayer player, FirebaseAuth auth, FirebaseUser user) {
        this.activity = activity;
        this.podcasts = podcasts;
        this.player = player;
        this.auth = auth;
        this.user = user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.podcast_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastAdapter.ViewHolder holder, int position) {
        Podcast podcast = podcasts.get(position);
        int pos = position;

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.podcastThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    MediaItem mediaItem = getMediaItem(podcast);
                    if (!player.isPlaying()) {
                        player.setMediaItems(getMediaItems(), pos, 0);
                    } else {
                        player.pause();
                        player.seekTo(pos, 0);
                    }
                    player.prepare();
                    player.setPlayWhenReady(true);
                } else {
                    v.getContext().startActivity(new Intent(v.getContext(), Profile.class));
                    activity.overridePendingTransition(0, 0);
                    activity.finish();
                }
            }
        });
    }

    public List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            MediaItem.Builder mediaItem = new MediaItem.Builder();
            mediaItem.setUri(podcast.getPodcastUrl());
            mediaItem.setMediaMetadata(getMetadata(podcast));
            mediaItems.add(mediaItem.build());
        }
        return mediaItems;
    }

    public MediaItem getMediaItem(Podcast podcast) {
        return new MediaItem.Builder()
                .setUri(podcast.getPodcastUrl())
                .setMediaMetadata(getMetadata(podcast))
                .build();
    }

    public MediaMetadata getMetadata(Podcast podcast) {
        return new MediaMetadata.Builder()
                .setTitle(podcast.getName())
                .setDescription(podcast.getDescription())
                .setArtworkUri(Uri.parse(podcast.getThumbnailUrl()))
                .build();
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView podcastThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            podcastThumbnail = itemView.findViewById(R.id.podcastThumbnail);
        }
    }
}
