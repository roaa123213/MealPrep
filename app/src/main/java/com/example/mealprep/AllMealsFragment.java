package com.example.mealprep;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


public class AllMealsFragment extends Fragment implements MyAdapter.OnItemClickListener {

    private FirebaseServices fbs;
    private ArrayList<MealPrep> meals;
    private RecyclerView rvMeals;
    private MyAdapter adapter;
    private ProgressBar progressBar;



    public AllMealsFragment()  {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_all_meals, container, false);
        View view = inflater.inflate(R.layout.fragment_all_meals, container, false);
        rvMeals = view.findViewById(R.id.rvMeals);
        progressBar=view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        fbs = FirebaseServices.getInstance();
        meals = new ArrayList<>();
        adapter = new MyAdapter(getActivity(), meals,this);
        rvMeals.setAdapter(adapter);
        rvMeals.setHasFixedSize(true);
        rvMeals.setLayoutManager(new LinearLayoutManager(getActivity()));


        progressBar.setVisibility(View.VISIBLE);

        fbs.getFire().collection("meals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot dataSnapshot : queryDocumentSnapshots.getDocuments()) {
                        MealPrep mealPrep = dataSnapshot.toObject(MealPrep.class);
                        if (mealPrep != null) {
                            meals.add(mealPrep);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Hide progress bar after data is loaded
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "No data available", Toast.LENGTH_SHORT).show();
                    Log.e("AllMealsFragment", e.getMessage());

                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                });



    }

    // Handle item click
    @Override
    public void onItemClick(MealPrep meal) {
        // Navigate to the detail fragment
        MealPrepDetailFragment detailFragment = MealPrepDetailFragment.newInstance(meal);

        // Use Fragment Transactions to replace the current fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutMain, detailFragment) // Replace with your container ID
                .addToBackStack(null) // Add to back stack so users can return
                .commit();
    }
}