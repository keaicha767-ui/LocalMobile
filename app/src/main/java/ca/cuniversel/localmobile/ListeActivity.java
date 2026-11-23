package ca.cuniversel.localmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

import ca.cuniversel.localmobile.reseau.ClientApi;
import ca.cuniversel.localmobile.reseau.Logement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeActivity extends AppCompatActivity {

    private LinearLayout conteneurLogements;
    private TextView messageErreur;
    private EditText champVille;
    private Button boutonChercher;
    private Button boutonConnexion;
    private Button boutonAjouter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_liste);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        conteneurLogements = findViewById(R.id.conteneurLogements);
        messageErreur = findViewById(R.id.messageErreur);
        champVille = findViewById(R.id.champVille);
        boutonChercher = findViewById(R.id.boutonChercher);
        boutonConnexion = findViewById(R.id.boutonConnexion);
        boutonAjouter = findViewById(R.id.boutonAjouter);

        boutonChercher.setOnClickListener(v -> charger());
        boutonConnexion.setOnClickListener(v ->
                startActivity(new Intent(ListeActivity.this, ConnexionActivity.class)));
        boutonAjouter.setOnClickListener(v ->
                startActivity(new Intent(ListeActivity.this, AjoutLogementActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        charger();
    }

    private void charger() {
        final String ville = champVille.getText().toString().trim();
        Call<List<Logement>> appel;
        if (ville.isEmpty()) {
            appel = ClientApi.get().listerLogements();
        } else {
            appel = ClientApi.get().chercherParVille(ville);
        }

        appel.enqueue(new Callback<List<Logement>>() {
            @Override
            public void onResponse(Call<List<Logement>> call, Response<List<Logement>> reponse) {
                Log.d("LocaMobile", "code " + reponse.code() + " " + call.request().url());
                if (reponse.isSuccessful() && reponse.body() != null) {
                    if (reponse.body().isEmpty() && !ville.isEmpty()) {
                        afficherErreur("Aucun logement à " + ville + ".");
                    } else {
                        afficher(reponse.body());
                    }
                } else {
                    afficherErreur("Erreur HTTP " + reponse.code());
                }
            }

            @Override
            public void onFailure(Call<List<Logement>> call, Throwable t) {
                Log.d("LocaMobile", "Echec : " + t.getMessage());
                afficherErreur("Serveur injoignable : " + t.getMessage());
            }
        });
    }

    private void afficher(List<Logement> logements) {
        messageErreur.setVisibility(View.GONE);
        conteneurLogements.removeAllViews();
        for (Logement l : logements) {
            View carte = LayoutInflater.from(this)
                    .inflate(R.layout.item_logement, conteneurLogements, false);
            ((TextView) carte.findViewById(R.id.carteTitre)).setText(l.titre);
            ((TextView) carte.findViewById(R.id.carteVille)).setText(l.ville);
            ((TextView) carte.findViewById(R.id.cartePrix)).setText(
                    String.format(Locale.CANADA_FRENCH, "%.2f $ / nuit", l.prixParNuit));

            final int idLogement = (int) l.id;
            carte.setOnClickListener(v -> {
                Intent intent = new Intent(ListeActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.CLE_ID, idLogement);
                startActivity(intent);
            });

            conteneurLogements.addView(carte);
        }
    }

    private void afficherErreur(String message) {
        conteneurLogements.removeAllViews();
        messageErreur.setText(message);
        messageErreur.setVisibility(View.VISIBLE);
    }
}