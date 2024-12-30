package com.example.my_application.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.my_application.database.DatabaseHelper;
import com.example.my_application.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchCommissionsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Spinner spnRepName, spnMonth, spnYear;
    private Button btnSearch;
    private TextView txtResult;

    // Map to store representative names and IDs
    private HashMap<String, Integer> repIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_commissions);

        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> onBackPressed()); // Navigate back when clicked

        dbHelper = new DatabaseHelper(this);
        spnRepName = findViewById(R.id.spnRepName);
        spnMonth = findViewById(R.id.spnMonth);
        spnYear = findViewById(R.id.spnYear);
        btnSearch = findViewById(R.id.btnSearch);
        txtResult = findViewById(R.id.txtResult);

        loadRepresentatives();
        loadMonthsAndYears();

        btnSearch.setOnClickListener(v -> searchCommissionsData());
    }

    private void loadRepresentatives() {
        Cursor cursor = dbHelper.getAllRepresentatives();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        repIdMap.clear();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(dbHelper.getColumnId()));
            String name = cursor.getString(cursor.getColumnIndex(dbHelper.getColumnName()));
            adapter.add(name);
            repIdMap.put(name, id);
        }
        cursor.close();

        spnRepName.setAdapter(adapter);
    }

    private void loadMonthsAndYears() {
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generateMonths());
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(monthAdapter);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, generateYears());
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnYear.setAdapter(yearAdapter);
    }

    private ArrayList<String> generateMonths() {
        ArrayList<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        return months;
    }

    private ArrayList<String> generateYears() {
        ArrayList<String> years = new ArrayList<>();
        for (int i = 2010; i <= 2030; i++) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    private void searchCommissionsData() {
        String selectedRep = (String) spnRepName.getSelectedItem();
        String selectedMonth = (String) spnMonth.getSelectedItem();
        String selectedYear = (String) spnYear.getSelectedItem();

        if (selectedRep == null || selectedMonth == null || selectedYear == null) {
            Toast.makeText(this, "يرجى اختيار جميع القيم", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer repId = repIdMap.get(selectedRep);
        if (repId == null) {
            Toast.makeText(this, "تعذر العثور على معرف الممثل", Toast.LENGTH_SHORT).show();
            return;
        }

        int month = Integer.parseInt(selectedMonth);
        int year = Integer.parseInt(selectedYear);

        Cursor cursor = dbHelper.getCommissionData(repId, month, year);

        StringBuilder result = new StringBuilder();

        if (cursor.moveToFirst()) {
            double commissionInMonth = cursor.getDouble(cursor.getColumnIndex(dbHelper.getColumnCommissionInMonth()));
            double cc = cursor.getDouble(cursor.getColumnIndex(dbHelper.getColumnCommissionInid()));
            double ss = cursor.getDouble(cursor.getColumnIndex(dbHelper.getColumnCommissionInMonth()));
            Log.d("Commission", String.valueOf(commissionInMonth));
            result.append(String.format("عمولة الشهر: %.2f ل.س\n", commissionInMonth));  // Format as string with currency
        } else {
            result.append("لا توجد بيانات عمولة لهذه الفترة.");
        }
        cursor.close();

        txtResult.setText(result.toString());
    }
}
