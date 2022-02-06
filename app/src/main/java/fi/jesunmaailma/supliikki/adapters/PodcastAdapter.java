package fi.jesunmaailma.supliikki.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.ui.activities.ListenPodcastActivity;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
    List<Podcast> podcasts;
    View view;

    public PodcastAdapter(List<Podcast> podcasts) {
        this.podcasts = podcasts;
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

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .placeholder(R.mipmap.supliikki_logo_color)
                .into(holder.podcastThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ListenPodcastActivity.class);
                intent.putExtra("podcast", podcast);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return podcasts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView podcastThumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            podcastThumbnail = itemView.findViewById(R.id.podcastThumbnail);
        }
    }
}
