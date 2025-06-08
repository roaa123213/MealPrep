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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddMealPrepFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddMealPrepFragment extends Fragment {

    private EditText etName, etCalories, etPrepTime, etIngredients;
    private Button btnAddMeal;
    private FirebaseServices fbs;
    private Uri selectedImageUri;

    private static final int GALLERY_REQUEST_CODE = 123;

    ImageView img;

    private Utils utils;
    private ActivityResultLauncher<Intent> galleryLauncher;

    // TODO: Rename parameter arguments if needed
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AddMealPrepFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddMealPrepFragment.
     */
    public static AddMealPrepFragment newInstance(String param1, String param2) {
        AddMealPrepFragment fragment = new AddMealPrepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AddMealPrepFragment", "onCreate");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        img.setImageURI(selectedImageUri);

                        // Upload the image and handle the result
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("AddMealPrepFragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_add_meal_prep, container, false);

        // Optional: Add back arrow navigation
        ImageView ivBackArrow = view.findViewById(R.id.ivBackArrowAddMeal); // Add this in your XML
        if (ivBackArrow != null) {
            ivBackArrow.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.frameLayoutMain, new LoginFragment()) // Replace with your home fragment
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("AddMealPrepFragment", "onStart");

        utils = Utils.getInstance();
        fbs = FirebaseServices.getInstance();

        // Initialize all view components here
        etName = requireView().findViewById(R.id.etNameAddMeal);
        etCalories = requireView().findViewById(R.id.etCaloriesAddMeal);
        etPrepTime = requireView().findViewById(R.id.etPrepTimeAddMeal);
        etIngredients = requireView().findViewById(R.id.etIngredientsAddMeal);
        btnAddMeal = requireView().findViewById(R.id.btnAddMeal);
        img = requireView().findViewById(R.id.imageView);

        btnAddMeal.setOnClickListener(v -> addMealPrep());
        img.setOnClickListener(v -> openGallery());
    }

    private void addMealPrep() {
        String nameString = etName.getText().toString().trim();
        String caloriesString = etCalories.getText().toString().trim();
        String prepTimeString = etPrepTime.getText().toString().trim();
        String ingredientsString = etIngredients.getText().toString().trim();

        // Validate inputs
        if (nameString.isEmpty() || caloriesString.isEmpty() || prepTimeString.isEmpty() || ingredientsString.isEmpty()) {
            utils.showMessageDialog(requireActivity(), "All fields and image are required.");
            return;
        }

        // Validate image selection
        if (selectedImageUri == null && (fbs.getSelectedImageURL() == null || fbs.getSelectedImageURL().isEmpty())) {
            utils.showMessageDialog(requireActivity(), "Please select an image");
            return;
        }

        MealPrep meal = new MealPrep(
                nameString,
                caloriesString,
                prepTimeString,
                ingredientsString,
                fbs.getSelectedImageURL()
        );

        // Save medicine details to Firestore under the current user's collection
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

                    // Update the Firestore document with the medicine ID
                    documentReference.update("id", mealId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("AddMealFragment", "Meal added successfully");

                                // Reset the ImageView, selectedImageUri, and fbs.setSelectedImageURL()
                                img.setImageDrawable(null);
                                selectedImageUri = null;
                                fbs.setSelectedImageURL(null);

                                Toast.makeText(requireActivity(), "Meal added successfully!", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {
                                Log.e("AddMealFragment", "Failed to update meal ID: " + e.getMessage());
                                Toast.makeText(requireActivity(), "Failed to update meal ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("AddMealFragment", "Failed to add medicine: " + e.getMessage());
                    Toast.makeText(requireActivity(), "Failed to add medicine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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