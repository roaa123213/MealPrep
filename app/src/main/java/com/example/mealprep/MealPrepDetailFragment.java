package com.example.mealprep;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MealPrepDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MealPrepDetailFragment extends Fragment {

    private MealPrep meal;



    public MealPrepDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of MealPrepDetailFragment.
     *
     * @param meal The MealPrep object to display in the fragment.
     * @return A new instance of MealPrepDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MealPrepDetailFragment newInstance(MealPrep meal) {
        MealPrepDetailFragment fragment = new MealPrepDetailFragment();
        Bundle args = new Bundle();
        // Pass the Medicine object using Serializable
        args.putSerializable("meal", meal);
        fragment.setArguments(args);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            meal = (MealPrep) getArguments().getSerializable("meal");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meal_prep_detail, container, false);

        TextView tvName = view.findViewById(R.id.tvDetailName);
        TextView tvCalories = view.findViewById(R.id.tvDetailCalories);
        TextView tvIngredients = view.findViewById(R.id.tvDetailIngredients);
        TextView tvPrepTime = view.findViewById(R.id.tvDetailPrepTime);
        ImageView ivPhoto = view.findViewById(R.id.ivDetailPhoto);

        // Bind data to views
        if (meal != null) {
            tvName.setText(meal.getName());
            tvCalories.setText(meal.getCalories());
            tvIngredients.setText(meal.getIngredients());
            tvPrepTime.setText(meal.getPrepTime());

            String photoUrl = meal.getPhoto();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.default_image);
            }
        }

        return view;

    }
}