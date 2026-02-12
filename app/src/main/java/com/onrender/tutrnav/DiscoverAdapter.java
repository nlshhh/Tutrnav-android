package com.onrender.tutrnav;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DiscoverAdapter extends RecyclerView.Adapter<DiscoverAdapter.DiscoverViewHolder> {

    private List<DiscoverModel> discoverList;

    public DiscoverAdapter(List<DiscoverModel> discoverList) {
        this.discoverList = discoverList;
    }

    @NonNull
    @Override
    public DiscoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discover_card, parent, false);
        return new DiscoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoverViewHolder holder, int position) {
        // MAGIC LINE: This creates the infinite loop logic
        int realPosition = position % discoverList.size();

        DiscoverModel item = discoverList.get(realPosition);
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());
        holder.image.setImageResource(item.getImageRes());
    }

    @Override
    public int getItemCount() {
        // Return a massive number so it feels infinite
        return Integer.MAX_VALUE;
    }

    static class DiscoverViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        ImageView image;

        public DiscoverViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvDiscoverTitle);
            subtitle = itemView.findViewById(R.id.tvDiscoverSubtitle);
            image = itemView.findViewById(R.id.imgDiscover);
        }
    }
}