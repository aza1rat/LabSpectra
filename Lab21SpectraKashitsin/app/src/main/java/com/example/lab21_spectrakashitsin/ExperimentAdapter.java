package com.example.lab21_spectrakashitsin;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lab21_spectrakashitsin.model.Experiment;

import org.w3c.dom.Text;

import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExperimentAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    public static ArrayList<Experiment> experiments;
    public static ArrayList<Experiment> workingExperiments;
    public static Experiment selected;

    public ExperimentAdapter(Context ctx)
    {
        this.ctx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return experiments.size();
    }

    @Override
    public Object getItem(int i) {
        return experiments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getIdFromItem(Experiment experiment)
    {
        for (int i = 0; i < experiments.size(); i++)
        {
            if (experiments.get(i).equals(experiment))
                return i;
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.list_experiment,parent,false);
        Experiment experiment = (Experiment) getItem(position);
        ((TextView)view.findViewById(R.id.textViewNote)).setText(experiment.note);
        ((TextView)view.findViewById(R.id.textViewDateCreated)).setText(experiment.getDate());
        ((TextView)view.findViewById(R.id.textViewStatus)).setText(experiment.status);
        ImageView image = view.findViewById(R.id.imageViewStatus);
        TextView time = view.findViewById(R.id.textViewTime);
        time.setVisibility(View.INVISIBLE);
        switch (experiment.status)
        {
            case "done": image.setImageResource(R.drawable.ic_baseline_done_24);break;
            case "running": image.setImageResource(R.drawable.ic_baseline_access_time_24);break;
            case "created": image.setImageResource(R.drawable.ic_baseline_hourglass_empty_24);break;
        }
        if (!(experiment.status.equals("done")))
        {
            Date dateNow = Calendar.getInstance().getTime();
            long tsp = (dateNow.getTime() - experiment.createdDate.getTime()) / 1000;
            int days = (int)tsp / 86400;
            int o = (int)tsp % 86400;
            int hours = o / 3600;
            o = o % 3600;
            int minutes = o / 60;
            int seconds = o % 60;
            time.setText("Длится: "+days + " д. " + hours + ":" + minutes + ":" + seconds);
            time.setVisibility(View.VISIBLE);
        }
        return view;
    }
}