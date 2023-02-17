package com.example.lab21_spectrakashitsin.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab21_spectrakashitsin.ListExperimentActivity;
import com.example.lab21_spectrakashitsin.SpecraDataActivity;
import com.example.lab21_spectrakashitsin.SpectraView;

public class SpectraHelper {
    static float zoom_percent = 0.1f;

    public static void initialize(SpectraView sv, Activity ctx)
    {
        sv.ctx = ctx;
        sv.have_ready = false;
        sv.have_dots = false;
        sv.have_divisions = DB.helper.getDivisions();
        DB.helper.getSettings();
        sv.setWillNotDraw(false);
        sv.invalidate();
    }

    public static void initializeExperiment(SpectraView sv, Activity ctx, Bitmap image)
    {
        sv.ctx = ctx;
        sv.have_ready = true;
        sv.image = image;
        sv.have_dots = false;
        SpectraView.wlen_min = 380.0f;
        SpectraView.wlen_max = 780.0f;
        SpectraView.last_image = 0.0f;
        Boolean[] graphics = DB.helper.getGraphics();
        sv.have_luminance = graphics[0];
        sv.profileR = graphics[1];
        sv.profileG = graphics[2];
        sv.profileB = graphics[3];
        sv.setWillNotDraw(false);
        sv.invalidate();
    }

    public static void initializeSend(SpectraView sv, Activity ctx)
    {
        sv.ctx = ctx;
        sv.have_ready = false;
        sv.have_dots = true;
        sv.have_divisions = false;
        sv.setWillNotDraw(false);
        sv.invalidate();
    }

    public static void initializePictureSend(SpectraView sv, Activity ctx, Bitmap image)
    {
        sv.ctx = ctx;
        sv.have_ready = true;
        sv.image = image;
        sv.have_dots = true;
        sv.setWillNotDraw(false);
        sv.invalidate();
    }

    public static void zoomIn(SpectraView sv)
    {
        float wlen_center = (SpectraView.wlen_max + SpectraView.wlen_min) / 2.0f;
        float wlen_dist = wlen_center - SpectraView.wlen_min;
        SpectraView.wlen_min += wlen_dist * zoom_percent;
        SpectraView.wlen_max -= wlen_dist * zoom_percent;
        sv.have_background = false;
        DB.helper.updateWlen(SpectraView.wlen_min,SpectraView.wlen_max);
        DB.helper.updateX(SpectraView.last_x);
        sv.invalidate();
    }
    public static void zoomOut(SpectraView sv)
    {
        float wlen_center = (SpectraView.wlen_max + SpectraView.wlen_min) / 2.0f;
        float wlen_dist = wlen_center - SpectraView.wlen_min;
        SpectraView.wlen_min -= wlen_dist * zoom_percent;
        SpectraView.wlen_max += wlen_dist * zoom_percent;
        sv.have_background = false;
        DB.helper.updateWlen(SpectraView.wlen_min, SpectraView.wlen_max);
        DB.helper.updateX(SpectraView.last_x);
        sv.invalidate();
    }

}
