package com.example.mealprep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.mealprep.FirebaseServices;
import com.example.mealprep.MealPrep;
import com.example.mealprep.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddMealPrepFragment extends Fragment {

    private EditText etName, etCalories, etPrepTime, etIngredients;
    private Button btnAddMeal;
    private FirebaseServices fbs;
    private Uri selectedImageUri;
    private ImageView img;

    private Utils utils;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public AddMealPrepFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_meal_prep, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        utils = Utils.getInstance();
        fbs = FirebaseServices.getInstance();

        // ربط العناصر بالـ IDs الموجودة في XML
        etName = requireView().findViewById(R.id.etNameAddMealPrep);
        etCalories = requireView().findViewById(R.id.etCaloriesEditMeal);
        etPrepTime = requireView().findViewById(R.id.etPrepTimeEditMeal);
        etIngredients = requireView().findViewById(R.id.etIngredientsEditMeal);
        img = requireView().findViewById(R.id.imageView);
        btnAddMeal = requireView().findViewById(R.id.btnAddMealPrep);

        // إعداد زر الإضافة
        btnAddMeal.setOnClickListener(v -> addMealPrep());

        // إعداد النقر على الصورة لفتح المعرض
        img.setOnClickListener(v -> openGallery());

        setupActivityResultLauncher();
    }

    private void setupActivityResultLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);

                        // رفع الصورة إلى Firebase
                        utils.uploadImage(requireActivity(), selectedImageUri, new Utils.ImageUploadCallback() {
                            @Override
                            public void onUploadSuccess(Uri downloadUrl) {
                                fbs.setSelectedImageURL(downloadUrl.toString());
                                Toast.makeText(requireActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                selectedImageUri = null;
                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                selectedImageUri = null;
                            }
                        });
                    } else {
                        selectedImageUri = null;
                    }
                }
        );
    }

    private void addMealPrep() {
        String name = etName.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();
        String prepTime = etPrepTime.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();

        if (name.isEmpty() || calories.isEmpty() || prepTime.isEmpty() || ingredients.isEmpty()) {
            utils.showMessageDialog(requireActivity(), "Please fill in all fields.");
            return;
        }

        if (selectedImageUri == null && (fbs.getSelectedImageURL() == null || fbs.getSelectedImageURL().isEmpty())) {
            utils.showMessageDialog(requireActivity(), "Please select an image.");
            return;
        }

        MealPrep meal = new MealPrep(name, calories, prepTime, ingredients, fbs.getSelectedImageURL());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        CollectionReference mealsRef = db.collection("users")
                .document(userId)
                .collection("meals");

        mealsRef.add(meal)
                .addOnSuccessListener(documentReference -> {
                    String mealId = documentReference.getId();
                    meal.setId(mealId);

                    documentReference.update("id", mealId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(requireActivity(), "Meal added successfully!", Toast.LENGTH_SHORT).show();
                                resetForm();
                            })
                            .addOnFailureListener(e -> Log.e("AddMeal", "Failed to update ID", e));
                })
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), "Failed to add meal.", Toast.LENGTH_SHORT).show());
    }

    private void resetForm() {
        etName.getText().clear();
        etCalories.getText().clear();
        etPrepTime.getText().clear();
        etIngredients.getText().clear();
        img.setImageResource(android.R.drawable.ic_menu_gallery);
        selectedImageUri = null;
        fbs.setSelectedImageURL(null);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }
}