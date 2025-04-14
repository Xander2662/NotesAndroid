package ondrej.wrubel.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class NotesListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private final List<Note> notesList = new ArrayList<>();
    private NotesDBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // Nastavení BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notes);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_notes) {
                return true;
            } else if (item.getItemId() == R.id.nav_add) {
                startActivity(new Intent(NotesListActivity.this, AddNoteActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_tips) {
                startActivity(new Intent(NotesListActivity.this, DestinationsActivity.class));
                finish();
                return true;
            }
            return false;
        });

        recyclerView = findViewById(R.id.recyclerViewNotes);
        // Použijeme GridLayoutManager
        int noOfColumns = Utils.calculateNoOfColumns(this, 180); // upravte podle potřeby
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, noOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);

        adapter = new NotesAdapter(notesList, note -> {
            Intent intent = new Intent(NotesListActivity.this, NoteDetailActivity.class);
            intent.putExtra("note_id", note.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        dbHelper = new NotesDBHelper(this);
        loadNotesFromDB();
    }

    // Pokaždé při návratu (onResume) načteme aktuální data z databáze
    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDB();
    }

    private void loadNotesFromDB() {
        notesList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(NotesDBHelper.TABLE_NOTES, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_TITLE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_DESCRIPTION));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_CREATED_AT));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotesDBHelper.COLUMN_LONGITUDE));
                Note note = new Note(id, title, desc, createdAt, latitude, longitude);
                notesList.add(note);
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
