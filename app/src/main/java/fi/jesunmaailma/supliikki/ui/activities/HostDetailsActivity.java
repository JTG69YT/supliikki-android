package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.models.Host;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class HostDetailsActivity extends AppCompatActivity {
    Host host;
    Toolbar toolbar;

    ImageView backdropImg, hostThumbnail;
    TextView hostName, hostDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_details);

        host = (Host) getIntent().getSerializableExtra("host");

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        backdropImg = findViewById(R.id.backdropImg);
        hostThumbnail = findViewById(R.id.hostThumbnail);
        hostName = findViewById(R.id.hostName);
        hostDescription = findViewById(R.id.hostDescription);

        Picasso.get()
                .load(host.getHostImage())
                .transform(new BlurTransformation(getApplicationContext(), 20))
                .into(backdropImg);

        Picasso.get()
                .load(host.getHostImage())
                .placeholder(R.drawable.supliikki_placeholder_512x512)
                .into(hostThumbnail);

        hostName.setText(host.getName());
        hostDescription.setText(host.getDescription());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}