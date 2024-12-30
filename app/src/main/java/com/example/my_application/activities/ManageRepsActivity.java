package com.example.my_application.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.my_application.R;
import com.example.my_application.database.DatabaseHelper;

public class ManageRepsActivity extends AppCompatActivity {

    private Button btnAddRep;
    private ListView listViewReps;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_reps); // Ensure correct layout is set
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed()); // Navigate back when clicked

        // Initialize views
        btnAddRep = findViewById(R.id.btnAddRep);
        listViewReps = findViewById(R.id.listViewReps);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Handle Add Representative button click
        if (btnAddRep != null) {
            btnAddRep.setOnClickListener(v -> {
                // Navigate to AddRepresentativeActivity
//                Toast.makeText(this, "تم الضغط على زر إضافة ممثل.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AddRepresentativeActivity.class);
                intent.putExtra("REP_ID", -1); // Pass the representative's ID to edit
                startActivity(intent);
            });
        } else {
            Log.e("ManageRepsActivity", "Add Representative button not found!");
        }

        // Populate the ListView
        populateRepsList();

        // Handle list item click
        if (listViewReps != null) {
            listViewReps.setOnItemClickListener((parent, view, position, id) -> {
                Log.e("id of rep", String.valueOf(id));
                Intent intent = new Intent(this, RepDetailsActivity.class);
                intent.putExtra("REP_ID", id); // Pass the selected representative's ID
                startActivity(intent);
            });
        } else {
            Log.e("ManageRepsActivity", "ListView not found!");
        }
    }

    private void populateRepsList() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(this);
        }

        Cursor cursor = databaseHelper.getAllRepresentatives();
        if (cursor != null && cursor.getCount() > 0) {
            String[] fromColumns = { "name", "region" }; // Replace with your database column names
            int[] toViews = { android.R.id.text1, android.R.id.text2 }; // Android's default layout for list items

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2, // Use a built-in layout for simplicity
                    cursor,
                    fromColumns,
                    toViews,
                    0
            );

            listViewReps.setAdapter(adapter);
        } else {
            Log.e("ManageRepsActivity", "No representatives found!");
            Toast.makeText(this, "لا يوجد مندوبين, الرجاء إضافة أحدهم باستخدام الزر أعلاه.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateRepsList(); // Refresh the list when returning to this activity
    }
}
