package com.example.lab21_spectrakashitsin.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.lab21_spectrakashitsin.SpectraView;
import com.example.lab21_spectrakashitsin.model.ChemElement;
import com.example.lab21_spectrakashitsin.model.Experiment;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sqlCommand = "CREATE TABLE ChemElement(" +
                "id int PRIMARY KEY NOT NULL," +
                "atomicNumber int NOT NULL," +
                "name varchar(30) NOT NULL," +
				"UNIQUE(atomicNumber));";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "CREATE TABLE Display(" +
                "id int NOT NULL," +
                "wlenMin real NOT NULL," +
                "wlenMax real NOT NULL," +
                "lastX real NOT NULL);";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand =
                "CREATE TABLE Settings(" +
                "id int NOT NULL,"+
                "address varchar(100) NOT NULL," +
                "element int NULL," +
                "bgLum real NOT NULL," +
                "haveDevisions bit NOT NULL," +
                "haveLuminance bit NOT NULL," +
                "haveRProfile bit NOT NULL," +
                "haveGProfile bit NOT NULL," +
                "haveBProfile bit NOT NULL," +
                "FOREIGN KEY(element) REFERENCES ChemElement(id));";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "CREATE TABLE Status (" +
                "id int PRIMARY KEY NOT NULL," +
                "name varchar(10) NOT NULL);";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "CREATE TABLE Experiment (" +
                "id int PRIMARY KEY NOT NULL," +
                "element int NULL," +
                "status int NOT NULL," +
                "FOREIGN KEY(element) REFERENCES ChemElement(id)," +
                "FOREIGN KEY(status) REFERENCES Status(id));";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "INSERT INTO Settings(id,address,bgLum,haveDevisions,haveLuminance,haveRProfile," +
                "haveGProfile,haveBProfile) VALUES" +
                " (1,'http://labs-api.spbcoit.ru:80/lab/spectra/api',0.25,0,0,0,0,0);";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "INSERT INTO Status VALUES (1,'created');";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "INSERT INTO Status VALUES (2,'running');";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "INSERT INTO Status VALUES (3,'done');";
        sqLiteDatabase.execSQL(sqlCommand);
        sqlCommand = "INSERT INTO Display VALUES (1,380,780,0)";
        sqLiteDatabase.execSQL(sqlCommand);

    }

    public Boolean getDivisions()
    {
        String sql = "SELECT haveDevisions FROM Settings WHERE id = 1;";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            boolean divisions = convertBitToBool(cursor.getInt(0));
            cursor.close();
            return divisions;
        }
        cursor.close();
        return false;
    }

    public Boolean[] getGraphics()
    {
        Boolean[] graphics = new Boolean[4];
        String sql = "SELECT haveLuminance,haveRProfile,haveGProfile,haveBProfile FROM Settings WHERE id = 1;";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            for (int i = 0; i < 4; i++)
            {
                graphics[i] = convertBitToBool(cursor.getInt(i));
            }
            cursor.close();
            return graphics;
        }
        cursor.close();
        return null;
    }

    public ChemElement getLastSelectedElement()
    {
        int id;
        String sql = "SELECT element FROM Settings WHERE id = 1;";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
            id = cursor.getInt(0);
        else
        {
            cursor.close();
            return null;
        }
        cursor.close();
        return getElementFromID(id);
    }

    public ChemElement getElementFromID(int id)
    {
        ChemElement chemElement = null;
        String sql = "SELECT * FROM ChemElement WHERE id = "+id + ";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            chemElement = new ChemElement(cursor.getInt(1),cursor.getString(2));
        }
        cursor.close();
        return chemElement;
    }

    public void updateLastSelectedElement(int id)
    {
        String sql = "UPDATE Settings SET element = " + id + " WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public int getElementFromExperiment(int id)
    {
        String sql = "SELECT element FROM Experiment WHERE id = "+id+";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            return cursor.getInt(0);
        }
        cursor.close();
        return 0;
    }

    public void insertExperiment(int id, String status)
    {
        int idExperiment = checkExistingOfExperiment(id);
        if (idExperiment == 0)
        {
            String sql = "INSERT INTO Experiment (id,status) VALUES ("+id+",'"+ Experiment.getStatus(status) +"');";
            SQLiteDatabase sqlDB = getWritableDatabase();
            sqlDB.execSQL(sql);
        }
    }

    public void updateExperiment(int id, int element)
    {
        String sql = "UPDATE Experiment SET element = "+element+" WHERE id = "+id+";";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public int checkExistingOfExperiment(int id)
    {
        String sql = "SELECT id FROM Experiment WHERE id = "+id+";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            int idExperiment = cursor.getInt(0);
            return idExperiment;
        }
        return 0;
    }

    public int insertElement(int atomicNum, String name)
    {
        int elementID = getElementID(atomicNum);
        if (elementID > 0)
            return elementID;
        int id = getMaxId("ChemElement") + 1;
        String sql = "INSERT INTO ChemElement VALUES ("+id+","+atomicNum+",'"+name+"');";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
        return id;
    }

    public int getElementID(int atomicNum)
    {
        String sql = "SELECT id FROM ChemElement WHERE atomicNumber = "+atomicNum+";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            int elementID = cursor.getInt(0);
            return elementID;
        }
        return 0;
    }

    public int getMaxId(String table)
    {
        String sql = "SELECT MAX(id) FROM " + table + ";";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            int max = cursor.getInt(0);
            return max;
        }
        return 0;
    }

    public float[] getDisplay()
    {
       float[] display = new float[3];
        String sql = "SELECT * FROM Display;";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            display[0] = cursor.getFloat(1);
            display[1] = cursor.getFloat(2);
            display[2] = cursor.getFloat(3);
        }
        cursor.close();
        return display;
    }

    public void getSettings()
    {
        String sql = "SELECT address,bgLum FROM Settings WHERE id = 1;";
        SQLiteDatabase sqlDB = getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery(sql,null);
        if (cursor.moveToFirst())
        {
            ApiHelper.address = cursor.getString(0);
            SpectraView.bg_lum = cursor.getFloat(1);
        }
        float[] display = getDisplay();
        SpectraView.wlen_min = display[0];
        SpectraView.wlen_max = display[1];
        SpectraView.last_x = display[2];
    }

    public void updateR(boolean have)
    {
        int haveR = convertBoolToBit(have);
        String sql = "UPDATE Settings SET haveRProfile = "+haveR+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateG(boolean have)
    {
        int haveG = convertBoolToBit(have);
        String sql = "UPDATE Settings SET haveGProfile = "+haveG+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateB(boolean have)
    {
        int haveB = convertBoolToBit(have);
        String sql = "UPDATE Settings SET haveBProfile = "+haveB+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateLuminance(boolean have)
    {
        int haveLum = convertBoolToBit(have);
        String sql = "UPDATE Settings SET haveLuminance = "+haveLum+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateLum(float bgLum)
    {
        String sql = "UPDATE Settings SET bgLum = "+bgLum+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateDivisions(boolean haveDivisions)
    {
        int divisions = convertBoolToBit(haveDivisions);
        String sql = "UPDATE Settings SET haveDevisions = "+divisions+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateWlen(float wlenMin,float wlenMax)
    {
        String sql = "UPDATE Display SET wlenMin = "+wlenMin+", wlenMax = "+wlenMax+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateX(float lastX)
    {
        String sql = "UPDATE Display SET lastX = "+lastX+" WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    public void updateAddress(String address)
    {
        String sql = "UPDATE Settings SET address = '"+address+"' WHERE id = 1;";
        SQLiteDatabase sqlDB = getWritableDatabase();
        sqlDB.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    Boolean convertBitToBool(int bit)
    {
        return bit != 0;
    }

    int convertBoolToBit(boolean bool)
    {
        if (bool)
            return 1;
        else
            return 0;
    }
}
