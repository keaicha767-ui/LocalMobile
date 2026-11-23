package ca.cuniversel.localmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import ca.cuniversel.localmobile.reseau.ClientApi;
import ca.cuniversel.localmobile.reseau.Logement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    public static final String CLE_ID = "id_logement";

    private TextView etat, detailTitre, detailVille, detailPrix, detailVoyageurs, detailProprio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etat = findViewById(R.id.etat);
        detailTitre = findViewById(R.id.detailTitre);
        detailVille = findViewById(R.id.detailVille);
        detailPrix = findViewById(R.id.detailPrix);
        detailVoyageurs = findViewById(R.id.detailVoyageurs);
        detailProprio = findViewById(R.id.detailProprio);

        int id = getIntent().getIntExtra(CLE_ID, -1);
        // Pour le test du 404, remplacer temporairement la ligne ci-dessus par : int id = 999;

        ClientApi.get().trouverLogement(id).enqueue(new Callback<Logement>() {
            @Override
            public void onResponse(Call<Logement> call, Response<Logement> reponse) {
                Log.d("LocaMobile", "code " + reponse.code() + " " + call.request().url());
                if (reponse.isSuccessful() && reponse.body() != null) {
                    afficher(reponse.body());
                } else if (reponse.code() == 404) {
                    afficherEtat("Ce logement n'existe plus (404).");
                } else {
                    afficherEtat("Erreur HTTP " + reponse.code());
                }
            }

            @Override
            public void onFailure(Call<Logement> call, Throwable t) {
                Log.d("LocaMobile", "Echec : " + t.getMessage());
                afficherEtat("Serveur injoignable : " + t.getMessage());
            }
        });
    }

    private void afficher(Logement l) {
        etat.setVisibility(View.GONE);
        detailTitre.setText(l.titre);
        detailVille.setText(l.ville);
        detailPrix.setText(String.format(Locale.CANADA_FRENCH, "%.2f $ / nuit", l.prixParNuit));
        detailVoyageurs.setText("Voyageurs : " + l.nbVoyageurs);
        detailProprio.setText("Propriétaire : " + l.proprietaireNom);
   }

    private void afficherEtat(String message) {
        etat.setText(message);
        etat.setVisibility(View.VISIBLE);
    }
}