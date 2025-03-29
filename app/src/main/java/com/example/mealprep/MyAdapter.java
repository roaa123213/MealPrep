package com.example.mealprep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import com.squareup.picasso.Picasso;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<MealPrep> mealList;
    private FirebaseServices fbs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MealPrep meal); // Pass the clicked Medicine object
    }


    public MyAdapter(Context context, ArrayList<MealPrep> mealList) {
        this.context = context;
        this.mealList = mealList;
        this.fbs = FirebaseServices.getInstance();
        this.listener = listener;
    }


    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v= LayoutInflater.from(context).inflate(R.layout.meal_item ,parent,false);
        return  new MyAdapter.MyViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        MealPrep meal = mealList.get(position);
        holder.tvName.setText(meal.getName());
        holder.tvCalories.setText(meal.getCalories());
        holder.tvIngredients.setText(meal.getIngredients());
        holder.tvPrepTime.setText(meal.getPrepTime());
        // Load image into ImageView using Glide

        // Load image into ImageView using Picasso
        String photo = meal.getPhoto();
        if (photo != null && !photo.isEmpty()) {
            Picasso.get()
                    .load(photo)
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Error image if loading fails
                    .into(holder.ivMealPhoto);
        } else {
            holder.ivMealPhoto.setImageResource(R.drawable.default_image); // Default image
        }
        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(meal); // Notify listener with the clicked MealPrep
            }
        });

    }

    @Override
    public int getItemCount(){
        return mealList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        TextView tvCalories;
        TextView tvIngredients;
        TextView tvPrepTime;
        ImageView ivMealPhoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tvNameAddMeal);
            tvCalories=itemView.findViewById(R.id.tvCaloriesAddMeal);
            tvIngredients=itemView.findViewById(R.id.tvIngredientsAddMeal);
            tvPrepTime=itemView.findViewById(R.id.tvPrepTimeAddMeal);
            ivMealPhoto = itemView.findViewById(R.id.imageView2);


        }
    }
}
