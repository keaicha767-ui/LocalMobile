package ca.cuniversel.localmobile.reseau;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClientApi {
    private static final String URL_BASE = "http://10.0.2.2:8080/";
    private static LocaMobileApi instance;

    public static LocaMobileApi get() {
        if (instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL_BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(LocaMobileApi.class);
        }
        return instance;
    }
}