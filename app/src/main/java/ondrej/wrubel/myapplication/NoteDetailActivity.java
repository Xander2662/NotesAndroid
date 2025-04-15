package ondrej.wrubel.myapplication;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText titleEditText, descEditText;
    private TextView timeTextView, locationTextView;
    private NotesDBHelper dbHelper;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail); // Layout pro editaci poznámky

        // Nastavení Toolbaru s menu_note_detail.xml (obsahuje i ikonu Show Map)
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Note");
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.inflateMenu(R.menu.menu_note_detail);

        // Nastavení BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notes);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_notes) {
                autoSaveNote(() -> {
                    Intent intent = new Intent(NoteDetailActivity.this, NotesListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
                return true;
            } else if(item.getItemId() == R.id.nav_add) {
                autoSaveNote(() -> {
                    Intent intent = new Intent(NoteDetailActivity.this, AddNoteActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
                return true;
            } else if(item.getItemId() == R.id.nav_tips) {
                autoSaveNote(() -> {
                    Intent intent = new Intent(NoteDetailActivity.this, DestinationsActivity.class);
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
        timeTextView = findViewById(R.id.textViewTime);
        locationTextView = findViewById(R.id.textViewLocation);
        dbHelper = new NotesDBHelper(this);

        // Načteme noteId z Intentu
        noteId = getIntent().getIntExtra("note_id", -1);
        if(noteId != -1){
            loadNoteFromDB(noteId);
        } else {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Auto-save při úpravách (TextWatcher)
        TextWatcher autoSaveWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Upravujeme pouze text – automaticky se uloží při opuštění aktivity
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        titleEditText.addTextChangedListener(autoSaveWatcher);
        descEditText.addTextChangedListener(autoSaveWatcher);
    }

    private void loadNoteFromDB(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NotesDBHelper.TABLE_NOTES, null,
                NotesDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_TITLE));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_DESCRIPTION));
            long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_CREATED_AT));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LONGITUDE));
            cursor.close();
            titleEditText.setText(title);
            descEditText.setText(desc);

            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            timeTextView.setText(dateFormat.format(new Date(createdAt)));
            locationTextView.setText("Lat: " + latitude + ", Lon: " + longitude);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoSaveNote(() -> {});
    }

    // Uloží změny poznámky
    private void autoSaveNote(@NonNull final Runnable onComplete) {
        String title = titleEditText.getText().toString().trim();
        String desc = descEditText.getText().toString().trim();

        if(title.isEmpty() && desc.isEmpty()){
            Toast.makeText(this, "Empty note deleted", Toast.LENGTH_SHORT).show();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(NotesDBHelper.TABLE_NOTES, NotesDBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(noteId)});
            onComplete.run();
        } else {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(NotesDBHelper.COLUMN_TITLE, title);
            values.put(NotesDBHelper.COLUMN_DESCRIPTION, desc);
            db.update(NotesDBHelper.TABLE_NOTES, values,
                    NotesDBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(noteId)});
            onComplete.run();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int mid = item.getItemId();
        if(mid == R.id.action_share){
            shareNote();
            return true;
        } else if(mid == R.id.action_delete){
            confirmDelete();
            return true;
        } else if(mid == R.id.action_show_map){
            showMap();
            return true;
        } else if(mid == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareNote() {
        String shareText = "Title: " + titleEditText.getText().toString() + "\n"
                + "Note: " + descEditText.getText().toString() + "\n"
                + "Created: " + timeTextView.getText().toString() + "\n"
                + "Location: " + locationTextView.getText().toString();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share note via"));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Smazat poznámku")
                .setMessage("Opravdu chcete tuto poznámku smazat?")
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        deleteNote();
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void deleteNote() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(NotesDBHelper.TABLE_NOTES, NotesDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(noteId)});
        if(rows > 0){
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showMap() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NotesDBHelper.TABLE_NOTES,
                new String[]{NotesDBHelper.COLUMN_LATITUDE, NotesDBHelper.COLUMN_LONGITUDE},
                NotesDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(noteId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LATITUDE));
            double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LONGITUDE));
            cursor.close();
            // Vytvoříme URI ve tvaru: geo:lat,lon?q=lat,lon
            Uri geoUri = Uri.parse("geo:" + lat + "," + lon + "?q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            // Vytvoříme chooser, aby uživatel mohl vybrat aplikaci
            try {
                startActivity(Intent.createChooser(mapIntent, "Select a map application"));
            } catch (Exception e) {
                Toast.makeText(this, "Map application not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        autoSaveNote(() -> {
            Intent intent = new Intent(NoteDetailActivity.this, NotesListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}