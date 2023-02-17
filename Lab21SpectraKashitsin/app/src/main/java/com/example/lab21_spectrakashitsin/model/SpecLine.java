package com.example.lab21_spectrakashitsin.model;

import android.graphics.Paint;

import org.json.JSONException;
import org.json.JSONObject;
/** Класс спектровых линий */
public class SpecLine {

    public float wavelength;
    int rel_intensity;
    int red;
    int green;
    int blue;
    public SpecLine(JSONObject object) throws JSONException
    {
        wavelength = (float) object.getDouble("wavelength");
        rel_intensity = object.getInt("rel_intensity");
        red = (int) (object.getDouble("red") * 255.0f);
        green = (int) (object.getDouble("green") * 255.0f);
        blue = (int) (object.getDouble("blue") * 255.0f);
    }
    public void setPaintColor(Paint p)
    {
        p.setARGB(255,red,green,blue);
    }
}
