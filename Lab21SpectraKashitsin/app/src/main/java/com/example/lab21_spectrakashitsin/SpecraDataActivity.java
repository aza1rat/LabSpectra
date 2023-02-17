package com.example.lab21_spectrakashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.textservice.SpellCheckerSubtype;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;
import com.example.lab21_spectrakashitsin.helpers.SpectraHelper;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecraDataActivity extends AppCompatActivity {
    SpectraView sv;
    TextView note;
    TextView tag;
    TextView created;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specra_data);
        sv = findViewById(R.id.spectraImageView);
        note = findViewById(R.id.textViewDataNote);
        tag = findViewById(R.id.textViewDataTag);
        created = findViewById(R.id.textViewDataCreated);
        ApiHelper req = new ApiHelper(SpecraDataActivity.this)
        {
            public void onReady(String res) {
                try {
                    JSONArray array = new JSONArray(res);
                    JSONObject object = array.getJSONObject(0);

                    String b64 = object.getString("b64image");
                    note.setText("Название: "+object.getString("note"));
                    tag.setText("Тэг: " + object.getString("tag"));
                    created.setText("Создано: " + object.getString("created_at"));
                    byte[] image = Base64.decode(b64, Base64.DEFAULT);
                    Bitmap spectraImage = BitmapFactory.decodeByteArray(image,0,image.length);
                    SpectraHelper.initializeExperiment(sv,SpecraDataActivity.this,spectraImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        req.send("rpc/get_experiment_data","{\"experiment\": " + ExperimentAdapter.selected.id + "}");
    }

    public void onZoomIn(View v)
    {
        SpectraHelper.zoomIn(sv);
    }

    public void onZoomOut(View v)
    {
        SpectraHelper.zoomOut(sv);
    }

    public void onShowDialog(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dlg = builder.create();
        dlg.setTitle("Настройки отображения");
        LayoutInflater inflater =  this.getLayoutInflater();
        View displayView = inflater.inflate(R.layout.dialog_display, null);
        LinearLayout layoutSpectra = displayView.findViewById(R.id.layoutSpectra);
        layoutSpectra.setVisibility(View.GONE);
        Switch luminance = displayView.findViewById(R.id.switchLuminance);
        Switch profileR = displayView.findViewById(R.id.switchProfileR);
        Switch profileG = displayView.findViewById(R.id.switchProfileG);
        Switch profileB = displayView.findViewById(R.id.switchProfileB);
        luminance.setChecked(sv.have_luminance);
        luminance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.have_luminance = luminance.isChecked();
                DB.helper.updateLuminance(sv.have_luminance);
                sv.invalidate();
            }
        });
        profileR.setChecked(sv.profileR);
        profileR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.profileR = profileR.isChecked();
                DB.helper.updateR(sv.profileR);
                sv.invalidate();
            }
        });
        profileG.setChecked(sv.profileG);
        profileG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.profileG = profileG.isChecked();
                DB.helper.updateG(sv.profileG);
                sv.invalidate();
            }
        });
        profileB.setChecked(sv.profileB);
        profileB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.profileB = profileB.isChecked();
                DB.helper.updateB(sv.profileB);
                sv.invalidate();
            }
        });
        dlg.setView(displayView);
        dlg.setCancelable(true);
        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
    }
}