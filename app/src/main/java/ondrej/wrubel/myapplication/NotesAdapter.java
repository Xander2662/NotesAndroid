package ondrej.wrubel.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    private final List<Note> notesList;
    private final OnItemClickListener listener;
    // Maximální délka zobrazeného popisu.
    private static final int MAX_DESCRIPTION_LENGTH = 100;

    public NotesAdapter(List<Note> notesList, OnItemClickListener listener) {
        this.notesList = notesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.bind(note, listener);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView titleText;
        public TextView descriptionText;

        public NoteViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textViewTitle);
            descriptionText = itemView.findViewById(R.id.textViewDescription);
        }

        public void bind(final Note note, final OnItemClickListener listener) {
            titleText.setText(note.getTitle());
            // Zkrácení popisu, pokud je delší než MAX_DESCRIPTION_LENGTH znaků.
            String desc = note.getDescription();
            if (desc.length() > MAX_DESCRIPTION_LENGTH) {
                desc = desc.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
            }
            descriptionText.setText(desc);
            itemView.setOnClickListener(v -> listener.onItemClick(note));
        }
    }
}