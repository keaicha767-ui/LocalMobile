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
import com.google.gson.JsonObject;

import ca.cuniversel.localmobile.reseau.ClientApi;
import ca.cuniversel.localmobile.reseau.ConnexionRequete;
import ca.cuniversel.localmobile.reseau.Profil;
import ca.cuniversel.localmobile.reseau.TokenReponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnexionActivity extends AppCompatActivity {

    private EditText champCourriel;
    private EditText champMotDePasse;
    private TextView resultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connexion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        champCourriel = findViewById(R.id.champCourriel);
        champMotDePasse = findViewById(R.id.champMotDePasse);
        resultat = findViewById(R.id.resultat);
        Button boutonConnecter = findViewById(R.id.boutonConnecter);

        boutonConnecter.setOnClickListener(v -> connecter());
    }

    private void connecter() {
        String courriel = champCourriel.getText().toString().trim();
        String motDePasse = champMotDePasse.getText().toString();

        if (courriel.isEmpty()) {
            champCourriel.setError("Courriel requis");
            return;
        }
        if (motDePasse.isEmpty()) {
            champMotDePasse.setError("Mot de passe requis");
            return;
        }

        resultat.setText("Connexion en cours...");
        ClientApi.get().connecter(new ConnexionRequete(courriel, motDePasse))
                .enqueue(new Callback<TokenReponse>() {
                    @Override
                    public void onResponse(Call<TokenReponse> call, Response<TokenReponse> reponse) {
                        if (reponse.isSuccessful() && reponse.body() != null) {
                            TokenReponse t = reponse.body();
                            Session.enregistrer(ConnexionActivity.this, t.token, t.nom);
                            chargerProfil(t.token);
                        } else {
                            resultat.setText("Erreur " + reponse.code() + " : " + lireMessage(reponse));
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenReponse> call, Throwable t) {
                        resultat.setText("Serveur injoignable : " + t.getMessage());
                    }
                });
    }

    private void chargerProfil(String jeton) {
        ClientApi.get().monProfil("Bearer "+ jeton).enqueue(new Callback<Profil>() {
            @Override
            public void onResponse(Call<Profil> call, Response<Profil> reponse) {
                if (reponse.isSuccessful() && reponse.body() != null) {
                    Profil p = reponse.body();
                    resultat.setText("Code HTTP : " + reponse.code()
                            + "\nNom : " + p.nom
                            + "\nRôle : " + p.role
                            + "\nCourriel : " + p.courriel);
                } else {
                    resultat.setText("Erreur " + reponse.code() + " : " + lireMessage(reponse));
                }
            }

            @Override
            public void onFailure(Call<Profil> call, Throwable t) {
                resultat.setText("Serveur injoignable : " + t.getMessage());
            }
        });
    }

    private String lireMessage(Response<?> reponse) {
        try {
            if (reponse.errorBody() == null) {
                return "(aucun message)";
            }
            String texte = reponse.errorBody().string();
            if (texte.isEmpty()) {
                return "(aucun message)";
            }
            JsonObject json = new Gson().fromJson(texte, JsonObject.class);
            if (json != null && json.has("message")) {
                return json.get("message").getAsString();
            }
            return texte;
        } catch (Exception e) {
            return "(message illisible)";
        }
    }
}