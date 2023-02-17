package com.example.lab21_spectrakashitsin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.example.lab21_spectrakashitsin.helpers.ApiHelper;
import com.example.lab21_spectrakashitsin.helpers.DB;
import com.example.lab21_spectrakashitsin.model.Dot;
import com.example.lab21_spectrakashitsin.model.Luminance;
import com.example.lab21_spectrakashitsin.model.RGBProfile;
import com.example.lab21_spectrakashitsin.model.SpecLine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SpectraView extends SurfaceView {

    ArrayList<SpecLine> lines = new ArrayList<>();
    ArrayList<Luminance> lums = new ArrayList<>();
    ArrayList<RGBProfile> profiles = new ArrayList<>();
    ArrayList<Dot> dots = new ArrayList<>();

    int[] divisions = new int[] {380,440,490,510,580,645,780};

    public static float lastDotX = 0;
    public static float lastDotY = 0;
    public static float bg_lum = 0.25f;

    public static float wlen_min = 380.0f;
    public static float wlen_max = 780.0f;
    public static float last_x = 0.0f;
    public static float last_image = 0.0f;
    public static int img_w;
    public static float radius = 30f;

    public boolean have_divisions = false;
    public int lastDot = -1;
    public boolean have_background = false;
    int img_h;
    boolean moving = false;
    public boolean have_ready = false;
    public boolean have_luminance = false;
    public boolean profileR = false;
    public boolean profileG = false;
    public boolean profileB = false;
    boolean have_profile = false;
    public boolean have_dots = false;

    public Bitmap image;
    public Bitmap bitmapSaving;
    public Canvas canvasSaving;

    public Activity ctx;
    JSONArray backgroundJSON;
    Paint p;

    public SpectraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
    }

    public void setReadyImage(final Canvas me, Bitmap image)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        Bitmap resized = Bitmap.createScaledBitmap(image,img_w,img_h,false);
        drawBitmap(me,resized,last_image,0,paint);
    }

    void downloadProfile()
    {
        profiles.clear();
        ApiHelper req = new ApiHelper(ctx){
            public void onReady(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject object = jsonArray.getJSONObject(i);
                        profiles.add(new RGBProfile(
                                object.getDouble("nm"),
                                object.getDouble("red"),
                                object.getDouble("green"),
                                object.getDouble("blue")
                        ));
                    }
                    invalidate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        req.send("rpc/get_color_profile","{\"experiment_id\": " +ExperimentAdapter.selected.id+"}");
    }

    void downloadLuminance()
    {
        lums.clear();
        ApiHelper req = new ApiHelper(ctx){
            public void onReady(String res) {
                try {
                    JSONArray jsonArray = new JSONArray(res);
                    for (int i = 0;i < jsonArray.length();i++)
                    {
                        JSONObject object = jsonArray.getJSONObject(i);
                        lums.add(new Luminance(
                                object.getDouble("nm"),
                                object.getDouble("lum")
                        ));

                    }
                    invalidate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        req.send("rpc/get_luminance_profile","{\"experiment_id\": "+ ExperimentAdapter.selected.id +"}");
    }

    void downloadBackground(final SpectraView me,int steps)
    {
        ApiHelper req = new ApiHelper(ctx){
            @Override
            public void onReady(String res) {
                try{
                    backgroundJSON = new JSONArray(res);
                }
                catch (JSONException ex) {}
                have_background = true;
                me.invalidate();
            }
        };
        JSONObject object = new JSONObject();
        try{
            object.put("nm_from",wlen_min);
            object.put("nm_to",wlen_max);
            object.put("steps",steps);
        }
        catch (JSONException ex) {}
        req.send("rpc/nm_to_rgb_range",object.toString());
    }

    float lerp(float a, float b, float t)
    {
        return a + (b - a) * t;
    }

    float unlerp(float x, float x0, float x1)
    {
        return (x - x0) / (x1 - x0);
    }

    float map(float x, float x0, float x1,float a, float b)
    {
        float t = unlerp(x,x0,x1);
        return lerp(a,b,t);
    }

    public void updateYDots()
    {
        if (dots.size() == 0)
            return;
        if (dots.get(0) == dots.get(1))
            return;
        for (Dot dot:dots)
        {
            dot.y = dots.get(0).y;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                lastDot = getDotAtXY(x,y);
                if (lastDot == -1)
                {
                    last_x = event.getX();
                }
                else
                {
                    lastDotX = x;
                    lastDotY = y;
                }
                moving = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (lastDot == -1)
                {
                    have_background = false;
                    DB.helper.updateWlen(wlen_min, wlen_max);
                }
                else
                {
                    updateYDots();
                }
                moving = false;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (lastDot >= 0)
                {
                    Dot dot = dots.get(lastDot);
                    dot.x += x - lastDotX;
                    dot.y += y - lastDotY;
                    lastDotX = x;
                    lastDotY = y;
                    invalidate();
                }
                else
                {
                    float new_x = event.getX();
                    float delta_x = new_x - last_x;
                    last_image+=delta_x;
                    float delta_nm = wlen_max - wlen_min;
                    float nm_per_pixel = delta_nm / img_w;
                    wlen_min -= delta_x * nm_per_pixel;
                    wlen_max -= delta_x * nm_per_pixel;
                    last_x = event.getX();
                }

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public int getDotAtXY(float x, float y)
    {
        for (int i = dots.size() - 1; i>= 0; i--)
        {
            Dot dot = dots.get(i);
            float dx = x - dot.x;
            float dy = y - dot.y;
            if (dx * dx + dy * dy <= radius * radius) return i;
        }
        return -1;

    }

    public void drawDot(Canvas canvas)
    {
        if (dots.size() == 0)
        {
            dots.add(new Dot(50,10));
            dots.add(new Dot(img_w - 50,10));
        }
        for (int i = 0; i < dots.size(); i++)
        {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            Dot dot = dots.get(i);
            canvas.drawCircle(dot.x,dot.y,radius,paint);
        }
    }

    public void drawLine(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint)
    {
        canvas.drawLine(startX,startY,stopX,stopY,paint);
        if (have_dots){
            canvasSaving.drawLine(startX,startY,stopX,stopY,paint);
        }
    }

    public void drawBitmap(Canvas canvas, Bitmap bitmap, float left, float top, Paint paint)
    {
        canvas.drawBitmap(bitmap,left,top,paint);
        if (have_dots) {
            canvasSaving.drawBitmap(bitmap,left,top,paint);
        }
    }

    public Bitmap getImage()
    {
        Bitmap screen = null;
        try {
            screen = Bitmap.createBitmap(bitmapSaving, (int) dots.get(0).x, (int) dots.get(1).y, (int) (dots.get(1).x - dots.get(0).x), (int) (0 + dots.get(0).y));
        }
        catch (Exception ex) {}
        return screen;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        img_w = w;
        img_h = h;
        bitmapSaving = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        canvasSaving = new Canvas(bitmapSaving);
        canvasSaving.drawColor(Color.BLACK);
        if (profileR || profileG || profileB)
            have_profile = true;
        else
            have_profile = false;

        if (have_ready)
        {
            setReadyImage(canvas,image);
            if (have_luminance)
            {
                if (lums.size() == 0)
                    downloadLuminance();
                else
                {
                    for (int i = 0; i < lums.size();i++)
                    {
                        if (i == lums.size()-1)
                            break;
                        Luminance lu = lums.get(i);
                        float x = map(lu.nm,wlen_min,wlen_max,0,w-1);
                        float y = map(1 - lu.lum,0,1,0,h-1);
                        lu = lums.get(i+1);
                        float x1 = map(lu.nm,wlen_min,wlen_max,0,w-1);
                        float y1 = map(1 - lu.lum,0,1,0,h-1);
                        Paint p2 = new Paint();
                        p2.setColor(Color.WHITE);
                        canvas.drawLine(x,y,x1,y1, p2);
                    }
                }
            }
            if (have_profile)
            {
                if (profiles.size() == 0)
                    downloadProfile();
                else
                {
                    for (int i = 0; i < profiles.size();i++)
                    {
                        if (i == profiles.size()-1)
                            break;
                        RGBProfile profile = profiles.get(i);
                        float x = map(profile.nm,wlen_min,wlen_max,0,w-1);
                        float y1 = map(1 - profile.red,0,1,0,h-1);
                        float y2 = map(1 - profile.green,0,1,0,h-1);
                        float y3 = map(1 - profile.blue,0,1,0,h-1);
                        profile = profiles.get(i+1);
                        float x1 = map(profile.nm,wlen_min,wlen_max,0,w-1);
                        float y12 = map(1 - profile.red,0,1,0,h-1);
                        float y22 = map(1 - profile.green,0,1,0,h-1);
                        float y32 = map(1 - profile.blue,0,1,0,h-1);
                        Paint p2 = new Paint();
                        if (profileR){
                            p2.setColor(Color.RED);
                            canvas.drawLine(x,y1,x1,y12,p2);
                        }
                        if (profileG) {
                            p2.setColor(Color.GREEN);
                            canvas.drawLine(x,y2,x1,y22,p2);
                        }
                        if (profileB) {
                            p2.setColor(Color.BLUE);
                            canvas.drawLine(x,y3,x1,y32,p2);
                        }
                    }
                }
            }
            if (have_dots)
            {
                drawDot(canvas);
            }
            return;
        }

        if (!have_background)
            downloadBackground(this,w);
        else
        {
            if (!moving)
            {
                for (int i = 0; i < backgroundJSON.length(); i++)
                {
                    try {
                        JSONObject object = backgroundJSON.getJSONObject(i);
                        int r = (int)(object.getDouble("red")*bg_lum*255.0);
                        int g = (int)(object.getDouble("green")*bg_lum*255.0);
                        int b = (int)(object.getDouble("blue")*bg_lum*255.0);
                        p.setARGB(255,r,g,b);
                        drawLine(canvas,i,0,i,h,p);
                    }
                    catch (JSONException ex) {}
                }
            }
        }
        for (int i = 0; i < lines.size();i++)
        {
            SpecLine sl = lines.get(i);
            float x = map(sl.wavelength,wlen_min,wlen_max,0,w-1);
            sl.setPaintColor(p);
            drawLine(canvas,x,0,x,h,p);
        }
        if (have_dots)
        {
            drawDot(canvas);
        }
        if (have_divisions)
        {
            for (int i = 0; i < divisions.length; i++)
            {
                float x = map(divisions[i],wlen_min,wlen_max,0,w-1);
                p.setColor(Color.WHITE);
                canvas.drawLine(x,0,x,h,p);
                canvas.drawText(String.valueOf(divisions[i]),x+5,10,p);
            }
        }
    }
}
