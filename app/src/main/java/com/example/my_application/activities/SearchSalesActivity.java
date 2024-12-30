package com.example.my_application.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.my_application.database.DatabaseHelper;
import com.example.my_application.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchSalesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Spinner spnRepName, spnMonth, spnYear;
    private Button btnSearch;
    private TextView txtResult;

    // Map to store representative names and IDs
    private HashMap<String, Integer> repIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_sales);

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

        btnSearch.setOnClickListener(v -> searchSalesAndCommissionData());
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

    private void searchSalesAndCommissionData() {
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

        Cursor salesCursor = dbHelper.getSalesData(repId, month, year);
//        Cursor commissionCursor = dbHelper.getCommissionData(repId, month, year);

        StringBuilder result = new StringBuilder();

        if (salesCursor.moveToFirst()) {
            double salesInRegion = salesCursor.getDouble(salesCursor.getColumnIndex("salesInRegion"));
            double salesOutRegion = salesCursor.getDouble(salesCursor.getColumnIndex("salesOutRegion"));

            result.append(String.format("مبيعات داخل المنطقة: %.2f ل.س\n", salesInRegion));
            result.append(String.format("مبيعات خارج المنطقة: %.2f ل.س\n", salesOutRegion));
        } else {
            result.append("لا توجد بيانات مبيعات.\n");
        }
        salesCursor.close();

//        if (commissionCursor.moveToFirst()) {
//            double commissionInRegion = commissionCursor.getDouble(commissionCursor.getColumnIndex("commissionInRegion"));
//            double commissionOutRegion = commissionCursor.getDouble(commissionCursor.getColumnIndex("commissionOutRegion"));
//
//            result.append(String.format("عمولة داخل المنطقة: %.2f ل.س\n", commissionInRegion));
//            result.append(String.format("عمولة خارج المنطقة: %.2f ل.س\n", commissionOutRegion));
//        } else {
//            result.append("لا توجد بيانات عمولة.\n");
//        }
//        commissionCursor.close();

        txtResult.setText(result.toString());
    }
}
