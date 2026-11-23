package ca.cuniversel.localmobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import ca.cuniversel.localmobile.reseau.ClientApi;
import ca.cuniversel.localmobile.reseau.Logement;
import ca.cuniversel.localmobile.reseau.LogementRequete;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AjoutLogementActivity extends AppCompatActivity {

    private EditText champTitre, champVille, champPrix, champVoyageurs;
    private TextView resultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajout_logement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        champTitre = findViewById(R.id.champTitre);
        champVille = findViewById(R.id.champVilleAjout);
        champPrix = findViewById(R.id.champPrix);
        champVoyageurs = findViewById(R.id.champVoyageurs);
        resultat = findViewById(R.id.resultatAjout);
        Button boutonPublier = findViewById(R.id.boutonPublier);

        boutonPublier.setOnClickListener(v -> publier());
    }

    private void publier() {
        String titre = champTitre.getText().toString().trim();
        String ville = champVille.getText().toString().trim();
        String textePrix = champPrix.getText().toString().trim().replace(',', '.');
        String texteVoyageurs = champVoyageurs.getText().toString().trim();

        // 1. Champs vides : vérifiés AVANT toute conversion numérique
        if (titre.isEmpty()) { champTitre.setError("Titre requis"); return; }
        if (ville.isEmpty()) { champVille.setError("Ville requise"); return; }
        if (textePrix.isEmpty()) { champPrix.setError("Prix requis"); return; }
        if (texteVoyageurs.isEmpty()) { champVoyageurs.setError("Nombre requis"); return; }

        // 2. Jeton : sans lui, on n'appelle pas l'API
        String jeton = Session.jeton(this);
        if (jeton == null) {
            resultat.setText("Connectez-vous d'abord.");
            return;
        }

        // 3. Conversion numérique, seulement maintenant
        double prix;
        int voyageurs;
        try {
            prix = Double.parseDouble(textePrix);
            voyageurs = Integer.parseInt(texteVoyageurs);
        } catch (NumberFormatException e) {
            resultat.setText("Prix ou nombre de voyageurs invalide.");
            return;
        }

        resultat.setText("Publication en cours...");
        LogementRequete requete = new LogementRequete(titre, ville, prix, voyageurs);
        ClientApi.get().creerLogement("Bearer " + jeton, requete)
                .enqueue(new Callback<Logement>() {
                    @Override
                    public void onResponse(Call<Logement> call, Response<Logement> reponse) {
                        int code = reponse.code();
                        if (code == 201 && reponse.body() != null) {
                            resultat.setText("201 : logement n° " + reponse.body().id + " créé.");
                        } else if (code == 401) {
                            resultat.setText("401 : jeton absent ou expiré, reconnectez-vous.");
                        } else if (code == 403) {
                            resultat.setText("403 : seul un propriétaire peut publier un logement.");
                        } else if (code == 400) {
                            resultat.setText("400 : " + lireChamps(reponse));
                        } else {
                            resultat.setText("Erreur " + code);
                        }
                    }

                    @Override
                    public void onFailure(Call<Logement> call, Throwable t) {
                        resultat.setText("Serveur injoignable : " + t.getMessage());
                    }
                });
    }

    // Lit le champ "champs" du JSON d'erreur 400 renvoyé par l'API
    private String lireChamps(Response<?> reponse) {
        try {
            if (reponse.errorBody() == null) return "(aucun message)";
            String texte = reponse.errorBody().string();
            if (texte.isEmpty()) return "(aucun message)";
            JsonObject json = new Gson().fromJson(texte, JsonObject.class);
            if (json != null && json.has("champs")) {
                JsonElement champs = json.get("champs");
                if (champs.isJsonObject()) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, JsonElement> e : champs.getAsJsonObject().entrySet()) {
                        sb.append(e.getKey()).append(" : ").append(e.getValue().getAsString()).append("\n");
                    }
                    return sb.toString().trim();
                }
                return champs.toString();
            }
            if (json != null && json.has("message")) return json.get("message").getAsString();
            return texte;
        } catch (Exception e) {
            return "(message illisible)";
        }
    }
}