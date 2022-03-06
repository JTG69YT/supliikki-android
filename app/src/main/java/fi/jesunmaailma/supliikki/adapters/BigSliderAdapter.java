package fi.jesunmaailma.supliikki.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.BigSlider;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class BigSliderAdapter extends RecyclerView.Adapter<BigSliderAdapter.ViewHolder> {
    Activity activity;
    List<BigSlider> bigSliderList;
    View view;

    public BigSliderAdapter(Activity activity, List<BigSlider> bigSliderList) {
        this.activity = activity;
        this.bigSliderList = bigSliderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.big_slider_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BigSliderAdapter.ViewHolder holder, int position) {
        BigSlider bigSlider = bigSliderList.get(position);

        Picasso.get()
                .load(bigSlider.getBackdropUrl())
                .transform(new BlurTransformation(activity.getApplicationContext(),20))
                .into(holder.backdropImg);

        Picasso.get()
                .load(bigSlider.getThumbnailUrl())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.podcastThumbnail);

        holder.podcastName.setText(bigSlider.getName());
        holder.podcastDescription.setText(bigSlider.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = bigSlider.getLink();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bigSliderList.size();
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
