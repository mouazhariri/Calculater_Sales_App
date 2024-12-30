package com.example.my_application.activities;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.example.my_application.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // إعداد الأزرار مع توجيه المستخدم إلى الأنشطة المختلفة
        findViewById(R.id.btnManageReps).setOnClickListener(v -> {

            startActivity(new Intent(this, ManageRepsActivity.class));
        });
        findViewById(R.id.btnSalesInput).setOnClickListener(v ->
                startActivity(new Intent(this, SalesInputActivity.class)));
        findViewById(R.id.btnSearchSales).setOnClickListener(v ->
                startActivity(new Intent(this, SearchSalesActivity.class)));
        findViewById(R.id.btnSearchCommissions).setOnClickListener(v ->
                startActivity(new Intent(this, SearchCommissionsActivity.class)));
    }
}
