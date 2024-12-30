package com.example.my_application.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.my_application.database.DatabaseHelper;
import com.example.my_application.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class SalesInputActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Spinner spinnerReps;
    private EditText edtSalesOwnRegion, edtSalesOtherRegions;
    private TextView txtCommissionResult;
    private Button btnCalculate, btnSave;

    // Map to store representative names and IDs
    private HashMap<String, Integer> repIdMap = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_input);

        // Back button click listener
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed()); // Navigate back when clicked

        // Initialize views
        dbHelper = new DatabaseHelper(this);
        spinnerReps = findViewById(R.id.spinnerReps);
        edtSalesOwnRegion = findViewById(R.id.edtSalesOwnRegion);
        edtSalesOtherRegions = findViewById(R.id.edtSalesOtherRegions);
        txtCommissionResult = findViewById(R.id.txtCommissionResult);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);

        // Load representatives into spinner
        loadRepresentatives();

        // Set click listeners for the buttons
        btnCalculate.setOnClickListener(v -> calculateCommission());
        btnSave.setOnClickListener(v -> saveSalesData());
    }

    // Load representatives from database into the spinner
    private void loadRepresentatives() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT _id, name FROM Representatives", null); // Adjust column names as per your database

        ArrayList<String> repsList = new ArrayList<>();
        repIdMap.clear();

        // Verify the cursor contains valid columns
        int idColumnIndex = cursor.getColumnIndex("_id");
        int nameColumnIndex = cursor.getColumnIndex("name");

        if (idColumnIndex == -1 || nameColumnIndex == -1) {
            Toast.makeText(this, "تعذر تحميل بيانات الممثلين. تحقق من قاعدة البيانات.", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(idColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            repsList.add(name);
            repIdMap.put(name, id);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReps.setAdapter(adapter);
    }

    // Calculate commission based on sales input
    private void calculateCommission() {
        String repName = spinnerReps.getSelectedItem().toString();
        String salesOwnRegionStr = edtSalesOwnRegion.getText().toString();
        String salesOtherRegionsStr = edtSalesOtherRegions.getText().toString();

        if (salesOwnRegionStr.isEmpty() || salesOtherRegionsStr.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال المبيعات", Toast.LENGTH_SHORT).show();
            return;
        }

        double salesOwnRegion = Double.parseDouble(salesOwnRegionStr);
        double salesOtherRegions = Double.parseDouble(salesOtherRegionsStr);
        double commission = 0;

        // Calculate commission for sales in own region
        if (salesOwnRegion <= 100_000_000) {
            commission += salesOwnRegion * 0.05;
        } else {
            commission += 100_000_000 * 0.05 + (salesOwnRegion - 100_000_000) * 0.07;
        }

        // Calculate commission for sales in other regions
        commission += salesOtherRegions * 0.03;

        // Display the commission result
        txtCommissionResult.setText(String.format("العمولة الشهرية: %.2f ل.س", commission));
    }

    // Save the sales data and commission in the database
    private void saveSalesData() {
        String repName = spinnerReps.getSelectedItem().toString();
        String salesOwnRegionStr = edtSalesOwnRegion.getText().toString();
        String salesOtherRegionsStr = edtSalesOtherRegions.getText().toString();
        String commissionResult = txtCommissionResult.getText().toString();

        // Validate that the fields are not empty
        if (salesOwnRegionStr.isEmpty() || salesOtherRegionsStr.isEmpty() || commissionResult.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال البيانات وحساب العمولة أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected representative ID
        Integer repId = repIdMap.get(repName);
        if (repId == null) {
            Toast.makeText(this, "تعذر العثور على الممثل", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Log the values before inserting into the database
        Log.d("SalesInputActivity", "Saving data: repId=" + repId + ", month=" + getCurrentMonth() + ", year=" + getCurrentYear() +
                ", salesOwnRegion=" + salesOwnRegionStr + ", salesOtherRegions=" + salesOtherRegionsStr + ", commissionResult=" + commissionResult);

        // Convert the sales values to doubles
        double salesOwnRegion = 0.0;
        double salesOtherRegions = 0.0;

        try {
            salesOwnRegion = Double.parseDouble(salesOwnRegionStr); // Convert salesOwnRegion to double
            salesOtherRegions = Double.parseDouble(salesOtherRegionsStr); // Convert salesOtherRegions to double
        } catch (NumberFormatException e) {
            Log.e("SalesInputActivity", "Error parsing sales values: " + e.getMessage());
            Toast.makeText(this, "خطأ في تحويل القيم", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clean commission result: Remove any non-numeric characters except for the dot
        String commissionValueStr = cleanCommissionResult(commissionResult); // Use the cleaning method

        // Now parse the cleaned commission value string
        double commissionValue = 0.0;
        try {
            // Only parse if the string is not empty
            if (!commissionValueStr.isEmpty()) {
                commissionValue = Double.parseDouble(commissionValueStr); // Parse the cleaned value to double
            } else {
                throw new NumberFormatException("Empty or invalid commission value.");
            }
        } catch (NumberFormatException e) {
            Log.e("SalesInputActivity", "Error parsing commission value: " + e.getMessage());
            Toast.makeText(this, "خطأ في تحويل العمولة", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert sales data and commission into the database
        long result = dbHelper.insertSales(repId, getCurrentMonth(), getCurrentYear(), salesOwnRegion, salesOtherRegions);
        long resultCommission = dbHelper.insertCommission(repId, getCurrentMonth(), getCurrentYear(), commissionValue);

        // Log the results
        Log.d("SalesInputActivity", "Insert result: sales=" + result + ", commission=" + resultCommission);

        // Check the results of the insertions
        if (result != -1 && resultCommission != -1) {
            Toast.makeText(this, "تم حفظ البيانات بنجاح", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "فشل في حفظ البيانات", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to clean the commission result
    private String cleanCommissionResult(String commissionResult) {
        // Remove any non-numeric characters except for the dot
        String commissionValueStr = commissionResult.replaceAll("[^0-9.]", "").trim();

        // Check if there are multiple dots and handle them
        int dotCount = commissionValueStr.length() - commissionValueStr.replace(".", "").length();
        if (dotCount > 1) {
            // If multiple dots exist, remove the extra dots (we keep the first one)
            int firstDotIndex = commissionValueStr.indexOf('.');
            commissionValueStr = commissionValueStr.substring(0, firstDotIndex + 1) +
                    commissionValueStr.substring(firstDotIndex + 1).replace(".", "");
        }

        return commissionValueStr;
    }

    // Get the current month (1-12)
    private int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    // Get the current year (e.g., 2024)
    private int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }
}
