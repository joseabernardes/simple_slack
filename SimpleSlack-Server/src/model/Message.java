/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.time.LocalDateTime;
import org.json.simple.JSONObject;

/**
 *
 * @author José Bernardes
 */
public class Message implements Comparable<Message> {

    private final int id;
    private final String username;
    private final LocalDateTime date;
    private final String message;
    private final boolean file;

    public Message(int id,String username, LocalDateTime date, String message) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;
        file = false;
    }

    public Message(int id, String username,LocalDateTime date, String message, boolean file) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;
        this.file = file;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("username", username);
        obj.put("date", date);
        obj.put("message", message);
        return obj.toJSONString();
    }

//    public static Message getInstance(String json) {
//
//    }

    /*
    public CuisineManagementContract StringToCuisines(String string) {
        JSONParser parser = new JSONParser();
        
        Object jsonObject;
        try {
            jsonObject = parser.parse(string);
            JSONArray jsonArray = (JSONArray) jsonObject;
            Cuisine[] cuisinesObj = new Cuisine[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject newObj = (JSONObject) jsonArray.get(i);
                if (newObj.get("cuisine_type").equals("Country") || newObj.get("cuisine_type").equals("Food")) {
                    cuisinesObj[i] = new RestaurantCuisine((int) (long) newObj.get("cuisine_id"), (String) newObj.get("cuisine_name"));
                } else if (newObj.get("cuisine_type").equals("Establishment")) {
                    cuisinesObj[i] = new BakeryCuisine((int) (long) newObj.get("cuisine_id"), (String) newObj.get("cuisine_name"));
                } else if (newObj.get("cuisine_type").equals("Drink")) {
                    cuisinesObj[i] = new NightClubCuisine((int) (long) newObj.get("cuisine_id"), (String) newObj.get("cuisine_name"));
                } else {
                    cuisinesObj[i] = new OtherCuisine((int) (long) newObj.get("cuisine_id"), (String) newObj.get("cuisine_name"));
                }
            }
            return new CuisineManagement(cuisinesObj);

        } catch (ParseException ex) {
            return new CuisineManagement();
        }
     */
    public int getId() {
        return id;
    }

    public boolean isFile() {
        return file;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int compareTo(Message o) {
        return this.date.compareTo(o.date);
    }
}
//
//@Override
//        public int compareTo(Comparator obj){
//        int objNumDias = ((Evento)obj).getNumDias();
//        
//        if (numDias > objNumDias) {
//            return 2;
//        } else if (numDias < objNumDias) {
//            return -2;
//
//        }else{
//            return 0;
//        }
//    }
