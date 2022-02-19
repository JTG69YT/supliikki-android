package fi.jesunmaailma.supliikki.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Host;
import fi.jesunmaailma.supliikki.models.Podcast;
import fi.jesunmaailma.supliikki.ui.activities.HostDetailsActivity;

public class HostsAdapter extends RecyclerView.Adapter<HostsAdapter.ViewHolder> {
    List<Host> hosts;
    View view;

    public HostsAdapter(List<Host> hosts) {
        this.hosts = hosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.host_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostsAdapter.ViewHolder holder, int position) {
        Host host = hosts.get(position);

        Picasso.get()
                .load(host.getHostImage())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(holder.hostThumbnail);

        holder.hostName.setText(host.getName());
        holder.hostDescription.setText(host.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), HostDetailsActivity.class);
                intent.putExtra("host", host);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView hostThumbnail;
        TextView hostName, hostDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            hostThumbnail = itemView.findViewById(R.id.hostThumbnail);
            hostName = itemView.findViewById(R.id.hostName);
            hostDescription = itemView.findViewById(R.id.hostDescription);
        }
    }
}
