package ondrej.wrubel.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Note");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notes);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_notes) {
                startActivity(new Intent(NoteDetailActivity.this, NotesListActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_add) {
                startActivity(new Intent(NoteDetailActivity.this, AddNoteActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_tips) {
                startActivity(new Intent(NoteDetailActivity.this, DestinationsActivity.class));
                finish();
                return true;
            }
            return false;
        });

        titleEditText = findViewById(R.id.editTextTitle);
        descEditText = findViewById(R.id.editTextDescription);
        timeTextView = findViewById(R.id.textViewTime);
        locationTextView = findViewById(R.id.textViewLocation);
        dbHelper = new NotesDBHelper(this);

        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            loadNoteFromDB(noteId);
        } else {
            titleEditText.setText("");
            descEditText.setText("");
            timeTextView.setText("");
            locationTextView.setText("");
        }
    }

    private void loadNoteFromDB(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NotesDBHelper.TABLE_NOTES, null,
                NotesDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareNote();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        } else if (id == R.id.action_show_map) {
            showMap();
            return true;
        } else if (id == android.R.id.home) {
            startActivity(new Intent(NoteDetailActivity.this, NotesListActivity.class));
            finish();
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
                .setCancelable(true)
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteNote();
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void deleteNote() {
        if (noteId != -1) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete(NotesDBHelper.TABLE_NOTES, NotesDBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(noteId)});
            if (rows > 0) {
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }
        startActivity(new Intent(NoteDetailActivity.this, NotesListActivity.class));
        finish();
    }

    private void showMap() {
        if (noteId != -1) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(NotesDBHelper.TABLE_NOTES,
                    new String[]{NotesDBHelper.COLUMN_LATITUDE, NotesDBHelper.COLUMN_LONGITUDE},
                    NotesDBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(noteId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LATITUDE));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LONGITUDE));
                cursor.close();
                String location = "Lat:" + lat + " Lon:" + lon;
                Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(this, "Map application not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Note not saved yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NoteDetailActivity.this, NotesListActivity.class));
        finish();
    }
}
