package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private EditText heightInput, weightInput, nameInput;
    private Spinner heightUnitSpinner, weightUnitSpinner;
    private String heightUnit = "Centimeters(cm)", weightUnit = "Kilograms(Kg)";
    private Button btnGoogleMap;
    private ImageView resultImage;
    private LinearLayout mainLayoutA;
    private DBCon dbCon;
    private static final int LOC_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Double latitude, longitude;

    private final Object lock = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbCon = new DBCon(this);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        window.setStatusBarColor(ContextCompat.getColor(this.getApplicationContext(), R.color.black));
        nameInput = findViewById(R.id.etName);
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        heightUnitSpinner = findViewById(R.id.heightUnitSpinner);
        weightUnitSpinner = findViewById(R.id.weightUnitSpinner);
        Button calculateButton = findViewById(R.id.calculateButton);
        Button btnHistory = findViewById(R.id.btnHistory);
        btnGoogleMap = findViewById(R.id.btnOpenMap);
        resultImage = findViewById(R.id.resultImage);
        mainLayoutA = (LinearLayout) findViewById(R.id.mainLayout);
        final TextView resultText = findViewById(R.id.resultText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        btnGoogleMap.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> heightAdapter = ArrayAdapter.createFromResource(this,
                R.array.height_units, android.R.layout.simple_spinner_item);
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        heightUnitSpinner.setAdapter(heightAdapter);

        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_units, android.R.layout.simple_spinner_item);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitSpinner.setAdapter(weightAdapter);

        heightUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                heightUnit = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        weightUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weightUnit = parent.getItemAtPosition(position).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOC_PERMISSION_REQUEST_CODE);
        } else {
            createLocationRequest();
            createLocationCallback();
        }

        btnGoogleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapsActivity(latitude, longitude);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
            }
        });

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightStr = heightInput.getText().toString();
                String weightStr = weightInput.getText().toString();
                String name = nameInput.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                if (!"".equals(heightStr) && !"".equals(weightStr)) {
                    float height = Float.parseFloat(heightStr);
                    float weight = Float.parseFloat(weightStr);

                    if (heightUnit.equals("Centimeters(cm)")) {
                        height = height / 100;
                    }
                    if (weightUnit.equals("Pound(Ib)")) {
                        weight = (float) (weight * 0.453592);
                    }

                    float bmi = weight / (height * height);

                    String bmiResult = interpretBMI(bmi);
                    imm.hideSoftInputFromWindow(mainLayoutA.getWindowToken(), 0);
                    resultText.setText(String.format("Your BMI is %.2f\n%s", bmi, bmiResult));

                    insertData(name, sdf.format(new Date()), bmi);
                } else {
                    resultText.setText("Please enter height and weight");
                }
            }
        });
    }
    private String interpretBMI(float bmiValue) {
        if (bmiValue < 18.5) {
            Log.d("interpretBMI", "BMI is underweight");
            btnGoogleMap.setVisibility(View.VISIBLE);
            resultImage.setBackgroundResource(R.drawable.tb);
            return "Underweight";
        } else if (bmiValue < 25) {
            Log.d("interpretBMI", "BMI is normal weight");
            btnGoogleMap.setVisibility(View.GONE);
            resultImage.setBackgroundResource(R.drawable.fb);
            return "Normal weight";
        } else if (bmiValue < 30) {
            Log.d("interpretBMI", "BMI is overweight");
            btnGoogleMap.setVisibility(View.VISIBLE);
            resultImage.setBackgroundResource(R.drawable.ob);
            return "Overweight";
        } else {
            Log.d("interpretBMI", "BMI is obese");
            btnGoogleMap.setVisibility(View.VISIBLE);
            resultImage.setBackgroundResource(R.drawable.ow);
            return "Obese";
        }
    }

    private void insertData(String name, String date, Float bmi ) {
        SQLiteDatabase db = dbCon.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("date", date);
        values.put("bmi", bmi);
        long newRowId = db.insert("user_table", null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to insert data", Toast.LENGTH_SHORT).show();
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
    }

    private void createLocationCallback() {
        Log.e("Location Activity", "Callback Running");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("Location Activity", "Location result is null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("Location Activity", "Runing For");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    locationClient.removeLocationUpdates(locationCallback);
                }
            }
        };
    }

    private void startMapsActivity(double latitude, double longitude) {
        Intent intent = new Intent(MainActivity.this, GoogleMapActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOC_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createLocationRequest();
                createLocationCallback();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}