package com.example.my_application.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.my_application.R;
import com.example.my_application.database.DatabaseHelper;

import java.io.File;
import java.util.Arrays;

public class RepDetailsActivity extends AppCompatActivity {

    private TextView tvRepName, tvRepPhone, tvRepRegion;
    private ImageView ivRepImage;
    private Button btnEditRep, btnDeleteRep;

    private DatabaseHelper dbHelper;
    private long repId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep_details);
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed()); // Navigate back when clicked

        // Initialize views
        tvRepName = findViewById(R.id.tvRepName);
        tvRepPhone = findViewById(R.id.tvRepPhone);
        tvRepRegion = findViewById(R.id.tvRepRegion);
        ivRepImage = findViewById(R.id.ivRepImage);
        btnEditRep = findViewById(R.id.btnEditRep);
        btnDeleteRep = findViewById(R.id.btnDeleteRep);

        dbHelper = new DatabaseHelper(this);

        // Get the representative ID passed from the previous activity
        repId = getIntent().getLongExtra("REP_ID", -1);
        if (repId != -1) {
            loadRepDetails(repId);
        } else {
            Toast.makeText(this, "Error loading representative details", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Navigate to edit page when edit button is clicked
        btnEditRep.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRepresentativeActivity.class);
            intent.putExtra("REP_ID", repId); // Pass the representative's ID to edit
            startActivity(intent);
        });

        // Delete representative when delete button is clicked
        btnDeleteRep.setOnClickListener(v -> deleteRep());
    }

    private void loadRepDetails(long id) {
        Cursor cursor = dbHelper.getRepresentativeById(id);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String name = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnName()));
                String phone = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnPhone()));
                String region = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnRegion()));
                String photoUri = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnImage()));

                Log.d("RepDetailsActivity", "Name: " + name);
                Log.d("RepDetailsActivity", "Phone: " + phone);
                Log.d("RepDetailsActivity", "Region: " + region);
                Log.d("RepDetailsActivity", "Photo URI: " + photoUri);

                tvRepName.setText("اسم المندوب: " + (name != null ? name : "غير متوفر"));
                tvRepPhone.setText("رقم الهاتف: " + (phone != null ? phone : "غير متوفر"));
                tvRepRegion.setText("المنطقة: " + (region != null ? region : "غير متوفر"));

                // Check if photoUri is not null or empty before attempting to load image
                if (photoUri != null && !photoUri.isEmpty()) {
                    // Use Context to get the internal file
                    File file = new File(getFilesDir(), photoUri.substring(photoUri.lastIndexOf("/") + 1)); // Get file from app's internal storage directory
                    if (file.exists()) {
                        Log.d("RepDetailsActivity", "File exists: " + file.getAbsolutePath());
                        loadImageFromFile(file.getAbsolutePath()); // Directly use the file path
                    } else {
                        Log.e("RepDetailsActivity", "File does not exist: " + file.getAbsolutePath());
                        ivRepImage.setImageResource(R.drawable.person_image); // Fallback image if file does not exist
                    }
                } else {
                    ivRepImage.setImageResource(R.drawable.person_image); // Fallback image if photoUri is null or empty
                }
            } catch (Exception e) {
                Log.e("RepDetailsActivity", "Error loading details", e);
                Toast.makeText(this, "Error loading representative details", Toast.LENGTH_LONG).show();
            } finally {
                cursor.close();
            }
        } else {
            Log.e("RepDetailsActivity", "No data found for ID: " + id);
            Toast.makeText(this, "Representative not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void deleteRep() {
        int rowsAffected = dbHelper.deleteRepresentative(repId);
        if (rowsAffected > 0) {
            Toast.makeText(this, "Representative deleted successfully", Toast.LENGTH_SHORT).show();
            finish(); // Go back to the previous screen
        } else {
            Toast.makeText(this, "Error deleting representative", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadImageFromFile(String photoPath) {
        try {
            File file = new File(photoPath);
            if (file.exists()) {
                Log.d("loadImageFromFile", "Image file found: " + file.getAbsolutePath());

                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                ivRepImage.setImageBitmap(bitmap);
            } else {
                Log.e("loadImageFromFile", "Image file not found: " + file.getAbsolutePath());

                ivRepImage.setImageResource(R.drawable.person_image); // Fallback image if file does not exist
            }
        } catch (Exception e) {
            e.printStackTrace();
            ivRepImage.setImageResource(R.drawable.person_image); // Fallback image
        }
    }



}
