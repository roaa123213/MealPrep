package com.example.mealprep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
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
    // TODO: Rename and change types and number of parameters
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
                            }

                            @Override
                            public void onUploadFailure() {
                                Toast.makeText(requireActivity(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_meal_prep, container, false);
    }

    @Override
    public void onStart() {
        Log.d("AddMealPrepFragment", "onStart");
        super.onStart();
        utils = Utils.getInstance();
        fbs = FirebaseServices.getInstance();

        // Initialize all view components here
        etName = getView().findViewById(R.id.etNameAddMeal);
        etCalories = getView().findViewById(R.id.etCaloriesAddMeal);
        etPrepTime= getView().findViewById(R.id.etPrepTimeAddMeal);
        etIngredients = getView().findViewById(R.id.etIngredientsAddMeal);
        btnAddMeal = getView().findViewById(R.id.btnAddMeal);
        img = getView().findViewById(R.id.imageView);

        btnAddMeal.setOnClickListener(v -> addMealPrep());
        img.setOnClickListener(v -> openGallery());



    }

    private void addMealPrep() {
        String nameString = etName.getText().toString().trim();
        String caloriesString = etCalories.getText().toString().trim();
        String prepTimeString = etPrepTime.getText().toString().trim();
        String ingredientsString = etIngredients.getText().toString().trim();

        // Validate input
        if (nameString.isEmpty() || caloriesString.isEmpty() || prepTimeString.isEmpty() || ingredientsString.isEmpty() || selectedImageUri == null) {
            utils.showMessageDialog(requireActivity(), "Some fields are empty or invalid");
            return;
        }

        MealPrep mealPrep;
        if (fbs.getSelectedImageURL() == null) {
            mealPrep = new MealPrep(nameString, caloriesString, prepTimeString, ingredientsString, "");
        } else {
            mealPrep = new MealPrep(nameString, caloriesString, prepTimeString, ingredientsString, fbs.getSelectedImageURL());
        }

        fbs.getFire().collection("meals").add(mealPrep)
                .addOnSuccessListener(documentReference -> Toast.makeText(requireActivity(), "Success", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), "Failure", Toast.LENGTH_SHORT).show());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }
}