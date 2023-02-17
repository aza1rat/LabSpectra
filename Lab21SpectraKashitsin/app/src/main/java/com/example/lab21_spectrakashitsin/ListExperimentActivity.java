package com.example.lab21_spectrakashitsin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;
import com.example.lab21_spectrakashitsin.helpers.DBHelper;
import com.example.lab21_spectrakashitsin.model.ChemElement;
import com.example.lab21_spectrakashitsin.model.Experiment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ListExperimentActivity extends AppCompatActivity {
    ListView lv;
    ExperimentAdapter adapter;
    String SelectedTag = "test";
    String[] tags;
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_experiment);
        DB.helper = new DBHelper(this,"spectra.db",null,1);
        DB.helper.getSettings();
        getTags();
        lv = findViewById(R.id.listViewExperiments);
        adapter = new ExperimentAdapter(this);
        updateExperiments(SelectedTag);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Experiment experiment = ExperimentAdapter.experiments.get(i);
                ExperimentAdapter.selected = experiment;
                Intent intent;
                if (experiment.status.equals("done"))
                {
                    intent = new Intent(ListExperimentActivity.this, SpecraDataActivity.class);
                }
                else
                {
                    intent = new Intent(ListExperimentActivity.this, MainActivity.class);
                }
                startActivity(intent);
            }
        });
    }

    public void getTags()
    {
        ApiHelper req = new ApiHelper(this)
        {
            @Override
            public void onReady(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    tags = new String[jsonArray.length()];
                    for (int i = 0; i < tags.length; i++)
                        tags[i] = jsonArray.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                super.onReady(res);
            }

            @Override
            public void onFailed() {
                Intent intent = new Intent(ListExperimentActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        };
        req.send("rpc/get_tags","{}");
    }

    public void onSelectTag(View v)
    {
        getTags();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(tags, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i > -1 && !(tags[i].equals(SelectedTag)))
                {
                    ExperimentAdapter.experiments.clear();
                    ExperimentAdapter.workingExperiments.clear();
                    updateExperiments(tags[i]);
                }
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void timed()
    {
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < ExperimentAdapter.workingExperiments.size(); i++)
                {
                    Experiment experiment = ExperimentAdapter.workingExperiments.get(i);
                    ApiHelper req = new ApiHelper(ListExperimentActivity.this){

                        public void onReady(String res) {
                            try {
                                if (res.equals("\"done\""))
                                {
                                    ExperimentAdapter.workingExperiments.remove(experiment);
                                    experiment.status = "done";
                                    ExperimentAdapter.experiments.set(adapter.getIdFromItem(experiment),experiment);
                                    adapter = new ExperimentAdapter(ListExperimentActivity.this);
                                    lv.setAdapter(adapter);
                                }
                                adapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    req.send("rpc/get_status","{\"experiment\":" + experiment.id + "}");
                }
            }
        };
        timer.scheduleAtFixedRate(tt,0,1000);

    }

    public void toSendExperiment(View v)
    {
        Intent intent = new Intent(ListExperimentActivity.this, SendExperimentActivity.class);
        startActivity(intent);
    }

    public void updateExperiments(String tag)
    {
        getTags();
        SelectedTag = tag;
        ApiHelper req = new ApiHelper(this){
            @Override
            public void onReady(String res) {
                try {
                    ExperimentAdapter.experiments = new ArrayList<Experiment>();
                    ExperimentAdapter.workingExperiments = new ArrayList<Experiment>();
                    JSONArray arr = new JSONArray(res);
                    for (int i = 0; i < arr.length(); i++)
                    {
                        JSONObject object = arr.getJSONObject(i);
                        Experiment experiment = new Experiment(object.getInt("id"),
                                object.getString("note"),object.getString("status"),object.getString("created_at"));
                        ExperimentAdapter.experiments.add(experiment);
                        if (!(experiment.status.equals("done")))
                            ExperimentAdapter.workingExperiments.add(experiment);
                    }
                    if (timer != null)
                        timer.cancel();
                    timer = new Timer(true);
                    if (ExperimentAdapter.workingExperiments.size() != 0)
                        timed();
                    if (lv.getAdapter() == null)
                        lv.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        req.send("rpc/get_experiments","{\"tagname\": \"" + tag + "\"}");
    }
}