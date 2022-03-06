package fi.jesunmaailma.supliikki.adapters;

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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
    List<Podcast> podcasts;
    View view;
    ExoPlayer player;

    public PodcastAdapter(List<Podcast> podcasts, ExoPlayer player) {
        this.podcasts = podcasts;
        this.player = player;
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

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.podcastThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaItem mediaItem = getMediaItem(podcast);
                if (!player.isPlaying()) {
                    player.setMediaItems(getMediaItems(), pos, 0);
                } else {
                    player.pause();
                    player.seekTo(pos, 0);
                }
                player.prepare();
                player.setPlayWhenReady(true);
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
        MediaItem.Builder builder = new MediaItem.Builder();
        builder.setUri(podcast.getPodcastUrl());
        builder.setMediaMetadata(getMetadata(podcast));
        return builder.build();
    }

    public MediaMetadata getMetadata(Podcast podcast) {
        MediaMetadata.Builder builder = new MediaMetadata.Builder();
        builder.setTitle(podcast.getName());
        builder.setDescription(podcast.getDescription());
        builder.setArtworkUri(Uri.parse(podcast.getThumbnailUrl()));
        return builder.build();
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
