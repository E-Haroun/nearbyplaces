package com.example.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String,String> parserJsonObject(JSONObject object){
        // initialize hash map
        HashMap<String,String> dataList =  new HashMap<>();
        try {
            // get name from object
            String name = object.getString("name");
            // get latitude from obj
            String latitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            //Longitude from objet
            String longitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");
            //pull all value in hash map
            dataList.put("name",name);
            dataList.put("lat",latitude);
            dataList.put("lng",longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // hashumapu
        return dataList;
    }
    private List<HashMap<String,String>> parserJsonArray(JSONArray jsonArray){
        //Initialize hash map list
        List<HashMap<String,String>> dataList =  new ArrayList<>();
        for (int i = 0; i< jsonArray.length();i++){
            try {
                // Initialize hash map
                HashMap<String,String> data = parserJsonObject((JSONObject) jsonArray.get(i));
                //Add data in hash map list
                dataList.add(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //hash map list
        return dataList;
    }
    public List<HashMap<String,String>> parseResult(JSONObject object){
        // Initialize json array
        JSONArray jsonArray = null ;

        try {
            // get result
            jsonArray = object.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // return array
        return parserJsonArray(jsonArray);
    }
}
