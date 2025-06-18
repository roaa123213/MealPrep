package com.example.mealprep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MainHome extends Fragment {

    public static MainHome newInstance() {
        return new MainHome();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAddMeal = view.findViewById(R.id.btnAddMealPrep);
        Button btnAllMeals = view.findViewById(R.id.btnAllMealsFromHome);
        
        btnAddMeal.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddMealPrepFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnAllMeals.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AllMealsFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}