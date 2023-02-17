package com.example.lab21_spectrakashitsin.model;

public class RGBProfile {
    public float nm;
    public float red;
    public float green;
    public float blue;
    public RGBProfile(double nm, double r, double g, double b)
    {
        this.nm = (float)nm;
        this.red = (float)r;
        this.green = (float)g;
        this.blue = (float)b;
    }
}
