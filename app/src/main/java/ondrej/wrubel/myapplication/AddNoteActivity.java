package ondrej.wrubel.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    // Pro novou poznámku používáme noteId = -1, dokud se nevloží záznam do DB.
    private int noteId = -1;
    private boolean isInserted = false;

    // Klíče pro temporary data uložena do SharedPreferences
    private static final String PREFS_NAME = "TempNote";
    private static final String KEY_TITLE = "temp_note_title";
    private static final String KEY_DESC = "temp_note_desc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note); // Layout pro novou poznámku

        // Nastavení Toolbaru s menu_new_note.xml (bez akce Show Map)
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nová poznámka");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Inflate menu pro novou poznámku
        toolbar.inflateMenu(R.menu.menu_new_note);

        // Nastavení BottomNavigationView
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

        // Zkontrolujeme oprávnění k poloze
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    // Uloží temporary data do SharedPreferences
    private void saveTemporaryNote() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sp.edit()
                .putString(KEY_TITLE, titleEditText.getText().toString())
                .putString(KEY_DESC, descEditText.getText().toString())
                .apply();
    }

    // Auto-save probíhá při opuštění aktivity
    @Override
    protected void onPause() {
        super.onPause();
        autoSaveNote(() -> {});
    }

    // Auto-save note do DB; pokud jsou obě pole prázdná – poznámka se smaže
    private void autoSaveNote(@NonNull final Runnable onComplete) {
        String title = titleEditText.getText().toString().trim();
        String desc = descEditText.getText().toString().trim();

        if(title.isEmpty() && desc.isEmpty()){
            Toast.makeText(this, "Empty note deleted", Toast.LENGTH_SHORT).show();
            if(isInserted){
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(NotesDBHelper.TABLE_NOTES, NotesDBHelper.COLUMN_ID + "=?",
                        new String[]{String.valueOf(noteId)});
            }
            onComplete.run();
        } else {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NotesDBHelper.COLUMN_TITLE, title);
            values.put(NotesDBHelper.COLUMN_DESCRIPTION, desc);
            // U nové poznámky vložíme také createdAt a polohu; u editace neaktualizujeme čas a polohu
            if(!isInserted) {
                long createdAt = System.currentTimeMillis();
                values.put(NotesDBHelper.COLUMN_CREATED_AT, createdAt);
                final double[] lat = {0.0}, lon = {0.0};
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                if(location != null){
                                    lat[0] = location.getLatitude();
                                    lon[0] = location.getLongitude();
                                }
                                values.put(NotesDBHelper.COLUMN_LATITUDE, lat[0]);
                                values.put(NotesDBHelper.COLUMN_LONGITUDE, lon[0]);
                                long newId = db.insert(NotesDBHelper.TABLE_NOTES, null, values);
                                noteId = (int) newId;
                                isInserted = true;
                                onComplete.run();
                            }).addOnFailureListener(e -> {
                                values.put(NotesDBHelper.COLUMN_LATITUDE, lat[0]);
                                values.put(NotesDBHelper.COLUMN_LONGITUDE, lon[0]);
                                long newId = db.insert(NotesDBHelper.TABLE_NOTES, null, values);
                                noteId = (int) newId;
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
                db.update(NotesDBHelper.TABLE_NOTES, values,
                        NotesDBHelper.COLUMN_ID + "=?",
                        new String[]{String.valueOf(noteId)});
                onComplete.run();
            }
        }
    }

    // Zpětná šipka – při stisku se vyvolá auto-save a pak se přejde do listu poznámek
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Systémový back – totéž chování jako u zpětné šipky
    @Override
    public void onBackPressed() {
        autoSaveNote(() -> {
            Intent intent = new Intent(AddNoteActivity.this, NotesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
