package ondrej.wrubel.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddNoteActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private EditText titleEditText, descEditText;
    private NotesDBHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isInserted = false;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nová poznámka");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_add);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_notes) {
                autoSaveNote(() -> {
                    Intent intent = new Intent(AddNoteActivity.this, NotesListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
                return true;
            } else if(item.getItemId() == R.id.nav_add) {
                return true;
            } else if(item.getItemId() == R.id.nav_tips) {
                autoSaveNote(() -> {
                    Intent intent = new Intent(AddNoteActivity.this, DestinationsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
                return true;
            }
            return false;
        });

        titleEditText = findViewById(R.id.editTextTitle);
        descEditText = findViewById(R.id.editTextDescription);
        dbHelper = new NotesDBHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoSaveNote(() -> {});
    }

    private void autoSaveNote(@NonNull final Runnable onComplete) {
        String title = titleEditText.getText().toString().trim();
        String desc = descEditText.getText().toString().trim();
        if(title.isEmpty() && desc.isEmpty()){
            Toast.makeText(this, "Empty note deleted", Toast.LENGTH_SHORT).show();
            if(isInserted){
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(NotesDBHelper.TABLE_NOTES, NotesDBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(noteId)});
            }
            onComplete.run();
        } else {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NotesDBHelper.COLUMN_TITLE, title);
            values.put(NotesDBHelper.COLUMN_DESCRIPTION, desc);
            if(!isInserted){
                long createdAt = System.currentTimeMillis();
                values.put(NotesDBHelper.COLUMN_CREATED_AT, createdAt);
                final double[] lat = {0.0}, lon = {0.0};
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                if(location != null){
                                    lat[0] = location.getLatitude();
                                    lon[0] = location.getLongitude();
                                }
                                values.put(NotesDBHelper.COLUMN_LATITUDE, lat[0]);
                                values.put(NotesDBHelper.COLUMN_LONGITUDE, lon[0]);
                                long newId = db.insert(NotesDBHelper.TABLE_NOTES, null, values);
                                noteId = (int)newId;
                                isInserted = true;
                                onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                values.put(NotesDBHelper.COLUMN_LATITUDE, lat[0]);
                                values.put(NotesDBHelper.COLUMN_LONGITUDE, lon[0]);
                                long newId = db.insert(NotesDBHelper.TABLE_NOTES, null, values);
                                noteId = (int)newId;
                                isInserted = true;
                                onComplete.run();
                            });
                } else {
                    values.put(NotesDBHelper.COLUMN_LATITUDE, lat[0]);
                    values.put(NotesDBHelper.COLUMN_LONGITUDE, lon[0]);
                    long newId = db.insert(NotesDBHelper.TABLE_NOTES, null, values);
                    noteId = (int)newId;
                    isInserted = true;
                    onComplete.run();
                }
            } else {
                db.update(NotesDBHelper.TABLE_NOTES, values, NotesDBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(noteId)});
                onComplete.run();
            }
        }
    }

    @Override
    public void onBackPressed() {
        autoSaveNote(() -> {
            Intent intent = new Intent(AddNoteActivity.this, NotesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
