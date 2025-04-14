package ondrej.wrubel.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DestinationsAdapter extends RecyclerView.Adapter<DestinationsAdapter.DestinationViewHolder> {

    private final List<DestinationTip> destinationList;

    public DestinationsAdapter(List<DestinationTip> destinationList) {
        this.destinationList = destinationList;
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destination, parent, false);
        return new DestinationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        DestinationTip tip = destinationList.get(position);
        holder.nameText.setText(tip.getName());
        holder.descText.setText(tip.getDescription());
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    public static class DestinationViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText;
        public TextView descText;

        public DestinationViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewDestinationName);
            descText = itemView.findViewById(R.id.textViewDestinationDesc);
        }
    }
}
