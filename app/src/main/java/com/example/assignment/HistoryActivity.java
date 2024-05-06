package com.example.assignment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;

public class HistoryActivity extends AppCompatActivity {
    private DBCon dbCon;
    private TextView tvDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        tvDataList = findViewById(R.id.tvDataList);

        dbCon = new DBCon(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
            });
        readData();
    }

    private void readData() {
        SQLiteDatabase db = dbCon.getReadableDatabase();
        StringBuilder sbHistory = new StringBuilder();

        String[] projection = {"id", "name", "date", "bmi"};
        Cursor cursor = db.query("user_table", projection, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            float bmi = cursor.getFloat(cursor.getColumnIndexOrThrow("bmi"));

            sbHistory.append("Date: ").append(date).append("\n");
            sbHistory.append("Name: ").append(name).append("\n");
            sbHistory.append("BMI: ").append(String.format("%.2f", bmi)).append("\n\n");
        }

        cursor.close();
        db.close();

        if (sbHistory.length() == 0) {
            sbHistory.append("No history available.");
        }

        tvDataList.setText(sbHistory.toString());
    }

    public void clearDatabase() {
        SQLiteDatabase db = dbCon.getWritableDatabase();
        db.delete("user_table", null, null);
        db.close();
    }

    public void onClearButtonClick(View view) {
        clearDatabase();
        Toast.makeText(this, "Database cleared", Toast.LENGTH_SHORT).show();
        readData();
    }
}