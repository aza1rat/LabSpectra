package com.example.lab21_spectrakashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;
import com.example.lab21_spectrakashitsin.helpers.SpectraHelper;
import com.example.lab21_spectrakashitsin.model.ChemElement;
import com.example.lab21_spectrakashitsin.model.SpecLine;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    Activity context;
    Spinner sp;
    SpectraView sv;
    ArrayAdapter<ChemElement> adp;
    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sv = findViewById(R.id.spectraView);
        context = this;
        sp = findViewById(R.id.spinnerElements);
        adp = new ArrayAdapter<ChemElement>(this, android.R.layout.simple_list_item_1);
        DB.helper.insertExperiment(ExperimentAdapter.selected.id,ExperimentAdapter.selected.status);
        ApiHelper req = new ApiHelper(this){
            @Override
            public void onReady(String res) {
                try {
                    ChemElement chElement = null;
                    int chElementId = DB.helper.getElementFromExperiment(ExperimentAdapter.selected.id);
                    if (chElementId != 0)
                    {
                        chElement = DB.helper.getElementFromID(chElementId);
                    }
                    else
                        chElement = DB.helper.getLastSelectedElement();
                    JSONArray arr = new JSONArray(res);
                    sp.setAdapter(adp);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        adp.add(new ChemElement(arr.getJSONObject(i)));
                        if (chElement != null && adp.getItem(i).atomic_num==chElement.atomic_num)
                            sp.setSelection(i);
                    }
                }
                catch (Exception ex) {}
            }
        };
        req.send("rpc/get_elements","{}");
        b = findViewById(R.id.buttonLoadElement);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChemElement el = (ChemElement) sp.getSelectedItem();
                ApiHelper req = new ApiHelper(context){
                    @Override
                    public void onReady(String res) {
                        try {
                            JSONArray arr = new JSONArray(res);
                            for (int i = 0; i < arr.length(); i++)
                                sv.lines.add(new SpecLine(arr.getJSONObject(i)));
                            sv.invalidate();
                        }
                        catch (JSONException ex) {

                        }
                    }
                };
                sv.lines.clear();
                req.send("rpc/get_lines","{\"atomic_num\": " + String.valueOf(el.atomic_num) + "}");
            }
        });
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ChemElement chemElement = adp.getItem(i);
                int elementID = DB.helper.insertElement(chemElement.atomic_num,chemElement.full_name);
                DB.helper.updateLastSelectedElement(elementID);
                DB.helper.updateExperiment(ExperimentAdapter.selected.id,elementID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        SpectraHelper.initialize(sv,this);
    }

    public void zoomIn(View v)
    {
        SpectraHelper.zoomIn(sv);
    }

    public void zoomOut(View v)
    {
        SpectraHelper.zoomOut(sv);
    }

    public void showDisplayDialog(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dlg = builder.create();
        dlg.setTitle("Настройки отображения");
        LayoutInflater inflater =  this.getLayoutInflater();
        View displayView = inflater.inflate(R.layout.dialog_display, null);
        LinearLayout layoutGraphs = displayView.findViewById(R.id.layoutGraphs);
        layoutGraphs.setVisibility(View.GONE);
        Slider backgroundIntensity = displayView.findViewById(R.id.sliderBackgroundIntensity);
        backgroundIntensity.setValue(sv.bg_lum);
        backgroundIntensity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                sv.bg_lum = (float)backgroundIntensity.getValue();
                DB.helper.updateLum(sv.bg_lum);
                sv.invalidate();
                return false;
            }
        });
        Switch spectraDivision = displayView.findViewById(R.id.switchSpectraDivisions);
        spectraDivision.setChecked(sv.have_divisions);
        spectraDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sv.have_divisions = spectraDivision.isChecked();
                DB.helper.updateDivisions(sv.have_divisions);
                sv.invalidate();
            }
        });
        dlg.setView(displayView);
        dlg.setCancelable(true);
        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
    }

    public void showSettings(View v)
    {
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }
}