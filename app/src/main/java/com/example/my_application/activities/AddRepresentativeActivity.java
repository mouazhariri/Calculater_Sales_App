package com.example.my_application.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.my_application.R;
import com.example.my_application.database.DatabaseHelper;

import java.io.File;

public class AddRepresentativeActivity extends AppCompatActivity {

    private EditText edtRepName, edtPhoneNumber, edtRegion;
    private ImageView imageView;
    private Button btnSelectImage;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private long repId ;

    private static final int IMAGE_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_representative);

        // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title); // Get reference to the TextView

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed()); // Navigate back when clicked

        edtRepName = findViewById(R.id.edtRepName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtRegion = findViewById(R.id.edtRegion);
        imageView = findViewById(R.id.imageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        Button btnSave = findViewById(R.id.btnSave);

        dbHelper = new DatabaseHelper(this);

        // Check if editing an existing representative
        repId = getIntent().getLongExtra("REP_ID", -1);
        if (repId != -1) {
            Log.d("Edit","EDIT MANDOP DONE");
            // Set title to "تعديل مندوب" for editing
            toolbarTitle.setText("تعديل مندوب");
            loadRepData(repId);
        } else {
            Log.d("ADD","ADD MANDOP DONE");

            // Set title to "إضافة مندوب" for adding new rep
            toolbarTitle.setText("إضافة مندوب");
        }

        // Select image button action
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        // Save button action
        btnSave.setOnClickListener(v -> saveRepData());
    }

    private void loadRepData(long id) {
        Cursor cursor = dbHelper.getRepresentativeById(id);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                String name = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnName()));
                String phone = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnPhone()));
                String region = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnRegion()));
                String photoUri = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnImage()));

                edtRepName.setText(name);
                edtPhoneNumber.setText(phone);
                edtRegion.setText(region);

                // Check if photoUri is not null or empty before attempting to load image
                if (photoUri != null && !photoUri.isEmpty()) {
                    // Load image from internal storage
                    File file = new File(getFilesDir(), photoUri.substring(photoUri.lastIndexOf("/") + 1));
                    if (file.exists()) {
                        loadImageFromFile(file.getAbsolutePath());
                    } else {
                        imageView.setImageResource(R.drawable.person_image); // Fallback image if file does not exist
                    }
                } else {
                    imageView.setImageResource(R.drawable.person_image); // Fallback image if photoUri is null or empty
                }
            } catch (Exception e) {
                Log.e("RepDetailsActivity", "Error loading details", e);
                Toast.makeText(this, "خطأ في تحميل تفاصيل المندوب", Toast.LENGTH_LONG).show(); // Error loading representative details
            } finally {
                cursor.close();
            }
        } else {
            Log.e("RepDetailsActivity", "No data found for ID: " + id);
            Toast.makeText(this, "لم يتم العثور على المندوب", Toast.LENGTH_SHORT).show(); // Representative not found
            finish();
        }
    }

    private void loadImageFromFile(String photoPath) {
        try {
            File file = new File(photoPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.person_image); // Fallback image if file does not exist
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.person_image); // Fallback image
        }
    }

    private void saveRepData() {
        String name = edtRepName.getText().toString().trim();
        String phone = edtPhoneNumber.getText().toString().trim();
        String region = edtRegion.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || phone.isEmpty() || region.isEmpty()) {
            Toast.makeText(this, "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show(); // Please fill all fields
            return;
        }

        // Save image to internal storage (if selected)
        String savedImagePath = null;
        if (selectedImageUri != null) {
            savedImagePath = dbHelper.saveImageToInternalStorage(selectedImageUri);
        }

        // Prepare values for database
        ContentValues values = new ContentValues();
        values.put(dbHelper.getColumnName(), name);
        values.put(dbHelper.getColumnPhone(), phone);
        values.put(dbHelper.getColumnRegion(), region);
        if (savedImagePath != null) {
            values.put(dbHelper.getColumnImage(), savedImagePath);
        }

        // Insert or update the database
        if (repId == -1) {
            // Insert new representative
            long result = dbHelper.insertRepresentative(values);
            if (result != -1) {
                Toast.makeText(this, "تم إضافة المندوب بنجاح", Toast.LENGTH_SHORT).show(); // Representative added successfully
                finish();
            } else {
                Toast.makeText(this, "خطأ في إضافة المندوب", Toast.LENGTH_SHORT).show(); // Error adding representative
            }
        } else {
            // Update existing representative
            int rowsAffected = dbHelper.updateRepresentative(repId, values);
            if (rowsAffected > 0) {
                Toast.makeText(this, "تم تحديث المندوب بنجاح", Toast.LENGTH_SHORT).show(); // Representative updated successfully
                dbHelper.getRepresentativeById(repId);
                finish();
            } else {
                Toast.makeText(this, "خطأ في تحديث المندوب", Toast.LENGTH_SHORT).show(); // Error updating representative
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }
}
