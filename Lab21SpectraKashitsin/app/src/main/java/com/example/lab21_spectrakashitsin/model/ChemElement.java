package com.example.lab21_spectrakashitsin.model;

import org.json.JSONException;
import org.json.JSONObject;


public class ChemElement {
    /** Номер элемента */
    public int atomic_num;
    /** Название элемента */
    public String full_name;

    /**
     * Химический элемент
     * @param object - JSON объект, содержащий номер элемента и его название
     * @throws JSONException
     */
    public ChemElement(JSONObject object) throws JSONException
    {
        atomic_num = object.getInt("atomic_num");
        full_name = object.getString("full_name");
    }

    public ChemElement(int atomic_num, String full_name)
    {
        this.atomic_num = atomic_num;
        this.full_name = full_name;
    }

    @Override
    public String toString(){
        return full_name;
    }
}
