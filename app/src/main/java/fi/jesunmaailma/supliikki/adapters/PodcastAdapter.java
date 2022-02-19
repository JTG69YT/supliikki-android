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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.ui.activities.ListenPodcastActivity;
import fi.jesunmaailma.supliikki.ui.activities.Login;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
    List<Podcast> podcasts;
    View view;
    FirebaseAuth auth;
    FirebaseUser user;

    public PodcastAdapter(List<Podcast> podcasts, FirebaseAuth auth, FirebaseUser user) {
        this.podcasts = podcasts;
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

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.podcastThumbnail);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    Intent intent = new Intent(v.getContext(), ListenPodcastActivity.class);
                    intent.putExtra("podcast", podcast);
                    v.getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(v.getContext(), Login.class);
                    v.getContext().startActivity(intent);
                }
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
