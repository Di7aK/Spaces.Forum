package com.di7ak.spaces.forum.api;
import org.json.JSONObject;

public class PaginationData {
    public int count;
    public int currentPage;
    public int lastPage;
    public int itemsOnPage;
    
    public static PaginationData fromJson(JSONObject json) throws SpacesException {
        PaginationData result = new PaginationData();
        try {
            if(json.has("count")) result.count = json.getInt("count");
            if(json.has("current_page")) result.currentPage = json.getInt("current_page");
            if(json.has("last_page")) result.lastPage = json.getInt("last_page");
            if(json.has("items_n_page")) result.itemsOnPage = json.getInt("items_on_page");
        } catch(Exception e) {
            throw new SpacesException(-2);
        }
        return result;
    }
}
