package fi.jesunmaailma.supliikki.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.ui.activities.ListenPodcastActivity;
import fi.jesunmaailma.supliikki.ui.activities.Login;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class BigSliderAdapter extends RecyclerView.Adapter<BigSliderAdapter.ViewHolder> {
    Activity activity;
    List<Podcast> podcasts;
    View view;
    FirebaseAuth auth;
    FirebaseUser user;

    public BigSliderAdapter(Activity activity, List<Podcast> podcasts, FirebaseAuth auth, FirebaseUser user) {
        this.activity = activity;
        this.podcasts = podcasts;
        this.auth = auth;
        this.user = user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.big_slider_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BigSliderAdapter.ViewHolder holder, int position) {
        Podcast podcast = podcasts.get(position);

        Glide.with(activity)
                .load(podcast.getBackdropUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(8, 3)))
                .into(holder.backdropImg);

        Picasso.get()
                .load(podcast.getThumbnailUrl())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.podcastThumbnail);

        holder.podcastName.setText(podcast.getName());
        holder.podcastDescription.setText(podcast.getDescription());

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
        ImageView backdropImg, podcastThumbnail;
        TextView podcastName, podcastDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backdropImg = itemView.findViewById(R.id.backdropImg);
            podcastThumbnail = itemView.findViewById(R.id.podcastThumbnail);
            podcastName = itemView.findViewById(R.id.podcastName);
            podcastDescription = itemView.findViewById(R.id.podcastDescription);
        }
    }
}
