package com.example.lab21_spectrakashitsin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;
import com.example.lab21_spectrakashitsin.helpers.SpectraHelper;
import com.example.lab21_spectrakashitsin.model.ChemElement;
import com.example.lab21_spectrakashitsin.model.SpecLine;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SendExperimentActivity extends AppCompatActivity {
    SpectraView sv;
    Activity context;
    Spinner sp;
    ArrayAdapter<ChemElement> adp;
    ImageView iv;
    EditText inputNote;
    EditText inputTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_experiment);
        sv = findViewById(R.id.spectraSendView);
        iv = findViewById(R.id.imagePreview);
        context = this;
        sp = findViewById(R.id.spinnerSendElements);
        inputNote = findViewById(R.id.inputNote);
        inputTag = findViewById(R.id.inputTag);
        adp = new ArrayAdapter<ChemElement>(this, android.R.layout.simple_list_item_1);
        ApiHelper req = new ApiHelper(this)
        {
            @Override
            public void onReady(String res) {
                try {
                    ChemElement chemElement = DB.helper.getLastSelectedElement();
                    JSONArray arr = new JSONArray(res);
                    sp.setAdapter(adp);
                    for (int i = 0; i < arr.length(); i++) {
                        adp.add(new ChemElement(arr.getJSONObject(i)));
                        if (chemElement != null && adp.getItem(i).atomic_num == chemElement.atomic_num)
                            sp.setSelection(i);
                    }
                    SpectraHelper.initializeSend(sv,context);

                }
                catch (Exception ex)
                {

                }
            }
        };
        req.send("rpc/get_elements","{}");
    }

    public void onSaveImage(View v)
    {
        Bitmap ready = sv.getImage();
        if (ready == null)
        {
            sendExperiment("",sv.img_h,sv.img_w,sv.img_h);
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ready.compress(Bitmap.CompressFormat.JPEG,50, out);
        byte[] b = out.toByteArray();
        String b64 = Base64.encodeToString(b,Base64.DEFAULT);
        if (b64.length() * 2 > 4096)
        {
            Toast.makeText(SendExperimentActivity.this,"Изображение слишком большое",Toast.LENGTH_LONG).show();
            return;
        }
        sendExperiment(b64,ready.getHeight() - 1,ready.getWidth() - 1,ready.getHeight() - 1);
    }

    public void onPreview(View v)
    {
        Bitmap ready = sv.getImage();
        if (ready == null)
        {
            iv.setImageResource(R.drawable.noimage);
            return;
        }
        iv.setImageBitmap(ready);
    }

    public void sendExperiment(String image, int y0, int x1, int y1)
    {
        ApiHelper req = new ApiHelper(context){
            @Override
            public void onReady(String res) {
                super.onReady(res);
            }
        };
        JSONObject object = new JSONObject();
        try {
            object.put("b64image",image);
            object.put("note",inputNote.getText().toString());
            object.put("tag",inputTag.getText().toString());
            object.put("x0",0);
            object.put("x1",x1);
            object.put("y0",y0);
            object.put("y1",y1);
        }
        catch (Exception ex) {}
        req.send("rpc/run_experiment", object.toString());
    }

    public void loadFromGallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            try {
                final Uri imageUri = data.getData();
                final InputStream is = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                SpectraHelper.initializePictureSend(sv,context,bitmap);
            }
            catch (Exception ex)
            {

            }
        }
    }

    public void loadFromAssets(View v)
    {
        AssetManager assetManager = getApplicationContext().getAssets();
        try {
            ArrayList<String> spects = new ArrayList<>();
            String files[] = assetManager.list("");
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].contains("spectrum_"))
                    spects.add(files[i]);
            }
            String[] arraySpectraImage = new String[spects.size()];
            for (int i = 0; i < spects.size(); i++)
            {
                arraySpectraImage[i] = spects.get(i);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(arraySpectraImage, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i > -1)
                    {
                        try {
                            InputStream is = assetManager.open(arraySpectraImage[i]);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            SpectraHelper.initializePictureSend(sv, context,bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
           AlertDialog dlg = builder.create();
           dlg.show();
    }
        catch (Exception ex)
        {

        }
    }

    public void onShowDisplayDialog(View v)
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
        displayView.findViewById(R.id.switchSpectraDivisions)
                .setVisibility(View.GONE);
        dlg.setView(displayView);
        dlg.setCancelable(true);
        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
    }

    public void zoomIn(View v)
    {
        SpectraHelper.zoomIn(sv);
    }

    public void zoomOut(View v)
    {
        SpectraHelper.zoomOut(sv);
    }

    public void onElementLoad(View v)
    {
        ChemElement el = (ChemElement) sp.getSelectedItem();
        ApiHelper req = new ApiHelper(context){
            @Override
            public void onReady(String res) {
                try {

                    JSONArray arr = new JSONArray(res);
                    for (int i = 0; i < arr.length(); i++)
                        sv.lines.add(new SpecLine(arr.getJSONObject(i)));
                    SpectraHelper.initializeSend(sv,context);
                }
                catch (JSONException ex) {

                }
            }
        };
        sv.lines = new ArrayList<SpecLine>();
        req.send("rpc/get_lines","{\"atomic_num\": " + String.valueOf(el.atomic_num) + "}");
    }

}