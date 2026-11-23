package ca.cuniversel.localmobile;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {
    private static SharedPreferences prefs(Context contexte) {
        return contexte.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public static void enregistrer(Context contexte, String jeton, String nom) {
        prefs(contexte).edit()
                .putString("jeton", jeton)
                .putString("nom", nom)
                .apply();
    }

    public static String jeton(Context contexte) {
        return prefs(contexte).getString("jeton", null);
    }

    public static String nom(Context contexte) {
        return prefs(contexte).getString("nom", null);
    }
}