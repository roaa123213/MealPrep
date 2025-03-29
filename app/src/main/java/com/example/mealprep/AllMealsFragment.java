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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllMealsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllMealsFragment extends Fragment {

    private FirebaseServices fbs;
    private ArrayList<MealPrep> meals;
    private RecyclerView rvMeals;
    private MyAdapter adapter;
    private ProgressBar progressBar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllMealsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllMealsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllMealsFragment newInstance(String param1, String param2) {
        AllMealsFragment fragment = new AllMealsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        adapter = new MyAdapter(getActivity(), meals);
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
                    Log.e("AllMedicinesFragment", e.getMessage());

                    // Hide progress bar on failure
                    progressBar.setVisibility(View.GONE);
                });



    }
}