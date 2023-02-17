package com.example.lab21_spectrakashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;

public class SettingsActivity extends AppCompatActivity {
    EditText inputAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        inputAddress = findViewById(R.id.inputAdress);
        inputAddress.setText(ApiHelper.address);
    }

    public void onOk(View v)
    {
        ApiHelper.address = inputAddress.getText().toString();
        DB.helper.updateAddress(ApiHelper.address);
    }

    public void onCancel(View v)
    {
        ApiHelper.address = "http://spectra.spbcoit.ru/lab/spectra/api";
    }
}