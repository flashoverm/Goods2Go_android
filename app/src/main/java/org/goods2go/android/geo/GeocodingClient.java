package org.goods2go.android.geo;

import android.util.Log;

import com.goods2go.models.Address;
import com.google.android.gms.maps.model.LatLng;

import org.goods2go.android.network.HttpClient;
import org.goods2go.android.network.NetworkException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeocodingClient {

    private static final String GEO_API_KEY = "AIzaSyCnrwzfpzdp5av5VDxRvif3kfm0gEyLVz4";
    private static final String RESPONSE_TYPE = "json";

    public static LatLng getCoordinatesFromAddress(Address address) throws NetworkException {
        String addressString = address.getStreet() + "+" + address.getStreetno() + ",+"
                    + address.getPostcode() + "+" + address.getCity();
        return getCoordinates(addressString);
    }

    public static LatLng getCoordinatesFromStreet(Address address) throws NetworkException{
        String addressString = address.getStreet() + "+" +
                address.getPostcode() + "+" + address.getCity();
        return getCoordinates(addressString);
    }

    public static LatLng getCoordinatesFromPostcode(Address address) throws NetworkException{
        String addressString = address.getPostcode() + "+" + address.getCity();
        return getCoordinates(addressString);
    }

    public static LatLng getCoordinatesFromCity(Address address) throws NetworkException{
        String addressString = address.getCity();
        return getCoordinates(addressString);
    }

    public static Address getPostcodeFromCoordinates(LatLng latLng) throws NetworkException{
        JSONArray address = getRawAddressFromCoordinates(latLng);
        try{
            String postcode = "";
            String city = "";
            String country = "";
            for(int i = 0; i<address.length(); i++) {
                JSONObject object = address.getJSONObject(i);
                String types = object.getString("types");
                String text = object.getString("long_name");

                if (types.contains("postal_code")) {
                    postcode = text;
                } else if(types.contains("locality")) {
                    city = text;
                } else if (types.contains("country")) {
                    country = text;
                }
            }
            return new Address(postcode, city, country);
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
        }
        return null;
    }

    public static Address getAddressFromCoordinates(LatLng latLng) throws NetworkException{
        JSONArray address = getRawAddressFromCoordinates(latLng);
        try{
            String street = "";
            String streetno = "";
            String postcode = "";
            String city = "";
            String country = "";
            for(int i = 0; i<address.length(); i++) {
                JSONObject object = address.getJSONObject(i);
                String types = object.getString("types");
                String text = object.getString("long_name");

                if (types.contains("route")) {
                    street = text;
                } else if (types.contains("street_number")) {
                    streetno = text;
                } else if (types.contains("postal_code")) {
                    postcode = text;
                } else if(types.contains("locality")) {
                    city = text;
                } else if (types.contains("country")) {
                    country = text;
                }
            }
            return new Address(streetno, street, postcode, city, country);
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
        }
        return null;
    }

    private static JSONArray getRawAddressFromCoordinates(LatLng latLng) throws NetworkException{
        String url = "https://maps.googleapis.com/maps/api/geocode/"
                + RESPONSE_TYPE + "?"
                + "latlng=" + latLng.latitude + "," + latLng.longitude
                + "&"
                + "key=" + GEO_API_KEY;

        try{
            JSONObject result = new JSONObject(HttpClient.get(url, String.class));
            return result.getJSONArray("results").getJSONObject(0)
                    .getJSONArray("address_components");
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
        }
        return null;
    }


    private static LatLng getCoordinates(String address) throws NetworkException{
        String url = "https://maps.googleapis.com/maps/api/geocode/"
                + RESPONSE_TYPE + "?"
                + "address=" + address
                + "&"
                + "key=" + GEO_API_KEY;

        try{
            JSONObject result = new JSONObject(HttpClient.get(url, String.class));
            JSONObject coordinates = result.getJSONArray("results").getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location");
            return new LatLng(coordinates.getDouble("lat"), coordinates.getDouble("lng"));
        } catch (JSONException e){
            Log.e("JSON", e.getMessage());
        }
        return null;
    }

}
