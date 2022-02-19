package fi.jesunmaailma.supliikki.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.supliikki.R;
import fi.jesunmaailma.supliikki.adapters.HostsAdapter;
import fi.jesunmaailma.supliikki.models.Host;
import fi.jesunmaailma.supliikki.services.SupliikkiDataService;

public class HostsActivity extends AppCompatActivity {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String IMAGE = "image";

    FirebaseAnalytics analytics;

    RecyclerView hostList;
    HostsAdapter hostsAdapter;
    List<Host> hosts;

    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;

    CardView errorContainer;

    ProgressBar pbLoadingHosts;

    SupliikkiDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosts);

        service = new SupliikkiDataService(this);

        toolbar = findViewById(R.id.toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        errorContainer = findViewById(R.id.error_container);

        pbLoadingHosts = findViewById(R.id.pbLoadingHosts);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ID);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, NAME);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, IMAGE);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        hostList = findViewById(R.id.hosts_list);
        hosts = new ArrayList<>();

        hostsAdapter = new HostsAdapter(hosts);
        hostList.setAdapter(hostsAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        hostList.setLayoutManager(manager);

        pbLoadingHosts.setVisibility(View.VISIBLE);

        getHostData(getResources().getString(R.string.supliikki_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&hosts=all&user_id=1");

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

                pbLoadingHosts.setVisibility(View.VISIBLE);

                errorContainer.setVisibility(View.GONE);

                hostList.setVisibility(View.GONE);
                getHostData(getResources().getString(R.string.supliikki_prod_api_url) + "?api_key=1A4mgi2rBHCJdqggsYVx&hosts=all&user_id=1");
            }
        });
    }

    public void getHostData(String url) {
        service.getHostData(url, new SupliikkiDataService.OnDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                pbLoadingHosts.setVisibility(View.GONE);
                hostList.setVisibility(View.VISIBLE);

                hosts.clear();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject hostData = response.getJSONObject(i);

                        Host host = new Host();

                        host.setId(hostData.getString("id"));
                        host.setName(hostData.getString("name"));
                        host.setDescription(hostData.getString("description"));
                        host.setHostImage(hostData.getString("host_image"));

                        hosts.add(host);
                        hostsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                swipeRefreshLayout.setRefreshing(false);
                errorContainer.setVisibility(View.VISIBLE);

                pbLoadingHosts.setVisibility(View.GONE);

                hostList.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}