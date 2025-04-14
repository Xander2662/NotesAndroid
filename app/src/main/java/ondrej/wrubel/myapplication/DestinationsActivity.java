package ondrej.wrubel.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DestinationsActivity extends AppCompatActivity {

    private DestinationsAdapter adapter;
    private final List<DestinationTip> destinationList = new ArrayList<>();
    // Použijeme REST Countries API jako vzorový "destinations API"
    private final String API_URL = "https://restcountries.com/v3.1/all";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Tipy na destinace");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Nastavení BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_tips);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId()==R.id.nav_notes) {
                startActivity(new Intent(DestinationsActivity.this, NotesListActivity.class));
                finish();
                return true;
            }
            else if (item.getItemId()==R.id.nav_add) {
                startActivity(new Intent(DestinationsActivity.this, AddNoteActivity.class));
                finish();
                return true;
            }
            else return item.getItemId() == R.id.nav_tips;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewDestinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DestinationsAdapter(destinationList);
        recyclerView.setAdapter(adapter);

        fetchDestinationsFromAPI();
    }

    private void fetchDestinationsFromAPI() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                // Čteme odpověď
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    responseBuilder.append(line);
                }
                reader.close();
                // Parsování JSON
                JSONArray jsonArray = new JSONArray(responseBuilder.toString());
                List<DestinationTip> tempList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++){
                    JSONObject countryObj = jsonArray.getJSONObject(i);
                    // Název země se nachází v objektu "name" jako "common"
                    String countryName = "";
                    if (countryObj.has("name")) {
                        JSONObject nameObj = countryObj.getJSONObject("name");
                        countryName = nameObj.optString("common", "Neznámá země");
                    }
                    // Jako stručný popis použijeme region
                    String region = countryObj.optString("region", "Neznámý region");
                    tempList.add(new DestinationTip(countryName, "Region: " + region));
                }
                // Aktualizace UI
                runOnUiThread(() -> {
                    destinationList.clear();
                    destinationList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(DestinationsActivity.this, "Chyba při načítání dat: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // Zajišťuji funkčnost tlačítka Up (zpět) v toolbaru.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
