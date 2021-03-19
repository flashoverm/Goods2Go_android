package org.goods2go.android.util;

import com.goods2go.models.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CustomGson {

    public static Gson build(){
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement j, Type t, JsonDeserializationContext ctx)
                            throws JsonParseException {
                        return j == null ? null : new Date(j.getAsLong());
                    }
                })
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date src, Type t, JsonSerializationContext ctx) {
                        return src == null ? null : new JsonPrimitive(DateTime.JSON_FORMAT.format(src));
                    }
                })
                .create();
    }
}
