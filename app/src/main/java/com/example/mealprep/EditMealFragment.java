package com.example.mealprep;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class EditMealFragment extends Fragment {

    private EditText etName, etCalories, etPrepTime, etIngredients;
    private Button btnSave;
    private ImageView img;
    private FirebaseServices fbs;
    private Utils utils;
    private String mealId;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public EditMealFragment() {
        // Required empty public constructor
    }

    public static EditMealFragment newInstance(String mealId) {
        EditMealFragment fragment = new EditMealFragment();
        Bundle args = new Bundle();
        args.putString("mealId", mealId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_meal, container, false);

        // Connect the back arrow ImageView
        ImageView ivBackArrowEdit = view.findViewById(R.id.ivBackArrowEditMed);
        ivBackArrowEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the previous fragment (e.g., AllMedicinesFragment)
                getParentFragmentManager().popBackStack();
            }
        });



        // Initialize views
        etName = view.findViewById(R.id.etNameEditMeal);
        etCalories = view.findViewById(R.id.etCaloriesEditMeal);
        etPrepTime = view.findViewById(R.id.etPrepTimeEditMeal);
        etIngredients = view.findViewById(R.id.etIngredientsEditMeal);
        btnSave = view.findViewById(R.id.btnSaveMeal);
        img = view.findViewById(R.id.imageViewEdit);

        // Initialize services
        fbs = FirebaseServices.getInstance();
        utils = Utils.getInstance();

        // Retrieve medicineId from arguments
        if (getArguments() != null) {
            mealId = getArguments().getString("mealId");
        }

        // Validate medicineId
        if (mealId == null || mealId.isEmpty()) {
            Toast.makeText(requireActivity(), "Invalid meal ID", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack(); // Exit the fragment
            return view;
        }

        // Load medicine details
        loadMealDetails(mealId);

        // Set up gallery launcher for image selection
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);

                        // Upload the image and handle the result
                        utils.uploadImage(requireActivity(), selectedImageUri, new Utils.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(Uri downloadUrl) {
                                fbs.setSelectedImageURL(downloadUrl.toString());
                                Toast.makeText(requireActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

        // Handle button clicks
        btnSave.setOnClickListener(v -> updateMeal(mealId));
        img.setOnClickListener(v -> openGallery());

        return view;
    }

    private void loadMealDetails(String mealId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference mealRef = db.collection("users")
                .document(userId)
                .collection("meals")
                .document(mealId);

        mealRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MealPrep meal = documentSnapshot.toObject(MealPrep.class);
                        if (meal != null) {
                            etName.setText(meal.getName());
                            etCalories.setText(meal.getCalories());
                            etPrepTime.setText(meal.getPrepTime());
                            etIngredients.setText(meal.getIngredients());

                            String photoUrl = meal.getPhoto();
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                utils.loadImageIntoImageView(img, photoUrl, requireActivity());
                                fbs.setSelectedImageURL(photoUrl); // Preserve the existing image URL
                            } else {
                                img.setImageResource(R.drawable.default_image);
                            }
                        }
                    } else {
                        Toast.makeText(requireActivity(), "Meal not found", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack(); // Exit the fragment
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireActivity(), "Failed to load meal" +
                            " details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Exit the fragment
                });
    }

    private void updateMeal(String mealId) {
        String nameString = etName.getText().toString().trim();
        String caloriesString = etCalories.getText().toString().trim();
        String preptimeString = etPrepTime.getText().toString().trim();
        String ingredientsString = etIngredients.getText().toString().trim();

        // Validate input
        if (nameString.isEmpty() || caloriesString.isEmpty() || preptimeString.isEmpty() || ingredientsString.isEmpty()) {
            utils.showMessageDialog(requireActivity(), "Some fields are empty or invalid");
            return;
        }



        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        DocumentReference mealRef = db.collection("users")
                .document(userId)
                .collection("meals")
                .document(mealId);

        // Preserve the existing image URL if no new image is selected
        AtomicReference<String> photoUrl = new AtomicReference<>(fbs.getSelectedImageURL());
        if (photoUrl.get() == null || photoUrl.get().isEmpty()) {
            mealRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            MealPrep existingMedicine = documentSnapshot.toObject(MealPrep.class);
                            if (existingMedicine != null) {
                                photoUrl.set(existingMedicine.getPhoto()); // Update asynchronously

                                // Perform the update only after retrieving the photo URL
                                mealRef.update(
                                                "name", nameString,
                                                "calories", caloriesString,
                                                "preptime", preptimeString,
                                                "ingredient", ingredientsString,
                                                "photo", photoUrl.get() // Correct usage
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireActivity(), "Meal updated successfully!", Toast.LENGTH_SHORT).show();


                                            getParentFragmentManager().popBackStack(); // Go back to the previous fragment

                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireActivity(), "Failed to update meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), "Failed to retrieve existing image URL", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update only the specified fields
            mealRef.update(
                            "name", nameString,
                            "calories", caloriesString,
                            "preptime", preptimeString,
                            "ingredients", ingredientsString,
                            "photo", photoUrl.get()
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireActivity(), "Meal updated successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireActivity(), "Failed to update meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }



}