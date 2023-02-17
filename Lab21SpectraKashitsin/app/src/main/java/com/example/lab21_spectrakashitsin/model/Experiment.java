package com.example.lab21_spectrakashitsin.model;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Experiment {
    public int id;
    public Date createdDate;
    public String note;
    public String status;
    public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    public static SimpleDateFormat outputFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SS");
    public Experiment(int id, String note, String status, String date)
    {
        this.id = id;
        this.note = note;
        this.status = status;
        try {
            this.createdDate = dateFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public static Integer getStatus(String status)
    {
        switch (status)
        {
            case "created":return 1;
            case "running":return 2;
            case "done": return 3;
        }
        return 0;
    }

    public String getDate()
    {
        return outputFormatter.format(createdDate);
    }
}
