package com.example.omnitrace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<ImageItem> imageItemList;
    private Context context;

    public ImageAdapter(List<ImageItem> imageItemList, Context context) {
        this.imageItemList = imageItemList;
        this.context = context;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView descriptionView;
        public Button deleteBtn;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            descriptionView = itemView.findViewById(R.id.textView);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ImageViewHolder(view);
    }

    //Manages the recyclerview/table
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem currentItem = imageItemList.get(position);
        holder.imageView.setImageResource(currentItem.getImageResource());
        holder.descriptionView.setText(currentItem.getDescription());
        holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.deleteBtn.setOnClickListener(v -> {
            removeItem(position);
            if (context instanceof WarningActivity){
                ((WarningActivity) context).saveListToPreferences();
            }
        });

    }

    //Returns size of list
    @Override
    public int getItemCount() {
        return imageItemList.size();
    }

    //Removes item from list
    public void removeItem(int position){
        imageItemList.remove(position);
        notifyItemRemoved(position);
    }
}
