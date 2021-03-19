package org.goods2go.android.network;

import org.goods2go.android.util.CustomGson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

import static org.goods2go.android.Configuration.CONNECTION_TIMEOUT;
import static org.goods2go.android.Configuration.READ_TIMEOUT;

public class HttpClient {

    public static <T>T get(String path, Class<T> inputType) throws NetworkException {
        return get(path, null, inputType);
    }

    public static <T>T get(String path, String sessionToken, Type inputType) throws NetworkException {
        HttpURLConnection connection;
        try {
            connection = prepareConnection(path, sessionToken);
            connection.setRequestMethod("GET");
            connection.connect();
        } catch (IOException e) {
            throw new NetworkException(e, NetworkException.CONNECTION_TIMEOUT);
        }
        try {
            checkResponseCode(connection.getResponseCode());
            return readStream(connection.getInputStream(), inputType);
        } catch (SocketTimeoutException e){
            throw new NetworkException(e, NetworkException.CONNECTION_TIMEOUT);
        } catch(IOException e){
            throw new NetworkException(e, NetworkException.INPUTSTREAM_ERROR);
        }
    }

    public static <T>T get(String path, Type inputType) throws NetworkException {
        return get(path, null, inputType);
    }

    public static <T>T post(String path, Object object, Type inputType) throws NetworkException {
        return post(path, null, object, inputType);
    }


    public static <T>T post(String path, String sessionToken, Object object, Type inputType)
        throws NetworkException {

        HttpURLConnection connection = prepareConnection(path, sessionToken);
        try{
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            if(object instanceof String){
                connection.setRequestProperty("Content-Type", "text/plain; charset=\"utf-8\"");
            } else {
                connection.setRequestProperty("Content-Type", "application/json; charset=\"utf-8\"");
            }
            connection.connect();
        } catch (IOException e) {
            throw new NetworkException(e, NetworkException.CONNECTION_TIMEOUT);
        }
        try{
            writeStream(connection.getOutputStream(), object);
        } catch (IOException e){
            throw new NetworkException(e, NetworkException.OUTPUTSTREAM_ERROR);
        }
        try {
            checkResponseCode(connection.getResponseCode());
            return readStream(connection.getInputStream(), inputType);
        } catch (SocketTimeoutException e){
            throw new NetworkException(e, NetworkException.CONNECTION_TIMEOUT);
        } catch(IOException e){
            throw new NetworkException(e, NetworkException.INPUTSTREAM_ERROR);
        }
    }

    private static HttpURLConnection prepareConnection(String url, String sessionToken)
            throws NetworkException{
        URL urlObject;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            throw new NetworkException(e, NetworkException.URLFORMAT_ERROR);
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) urlObject.openConnection();
        } catch (IOException e) {
            throw new NetworkException(e, NetworkException.CONNECTION_ERROR);
        }
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        if(sessionToken != null){
            connection.setRequestProperty ("Authorization", sessionToken);
        }
        return connection;
    }

    private static void writeStream(OutputStream stream, Object object) throws NetworkException{
        try{
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
            writer.write(serializeOutput(object));
            writer.flush();
            writer.close();
            stream.close();
        } catch (IOException e){
            throw new NetworkException(e, NetworkException.OUTPUTSTREAM_ERROR);
        }
    }

    private static String serializeOutput(Object object){
        if(object instanceof String){
            return (String)object;
        }
        return CustomGson.build().toJson(object);
    }

    private static void checkResponseCode(int responseCode) throws NetworkException{
        switch(responseCode) {
            case HttpURLConnection.HTTP_OK:
                return;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new NetworkException("Unauthorized", responseCode);
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new NetworkException("NotFound", responseCode);
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new NetworkException("Forbidden", responseCode);
            case HttpURLConnection.HTTP_CONFLICT:
                throw new NetworkException("Conflict", responseCode);
            default:
                if(responseCode/100 == 5){
                    throw new NetworkException(
                            "InternalServerError", HttpURLConnection.HTTP_INTERNAL_ERROR);
                }
                throw new NetworkException("Unknown Error", responseCode);
        }
    }

    private static <T>T readStream(InputStream stream, Type inputType){
        Scanner toString = new Scanner(stream,"UTF-8");
        if(!toString.hasNext()){
            return null;
        }
        String input = toString.useDelimiter("\\A").next();
        toString.close();
        if(inputType.equals(String.class)){
            return (T)input;
        }
        return parseInput(input, inputType);
    }

    private static <T>T parseInput(String input, Type inputType){
        if(inputType.equals(String.class)){
            return (T)input;
        }
        return CustomGson.build().fromJson(input, inputType);
    }
}
