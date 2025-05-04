package com.example.tdc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AvisosGuardadosActivity extends AppCompatActivity {
    private LinearLayout linearLayoutMeses;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos_guardados);

        linearLayoutMeses = findViewById(R.id.linearLayoutMesesAvisos);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        findViewById(R.id.botonAtras).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        cargarAvisos();
    }

    private void cargarAvisos() {
        db.collection("avisos")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Map<String, List<DocumentSnapshot>>> agrupados = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String fechaStr = doc.getString("fecha");
                        try {
                            Date fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaStr);
                            String mesNombre = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(fecha);
                            String diaStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fecha);

                            mesNombre = mesNombre.substring(0, 1).toUpperCase() + mesNombre.substring(1).toLowerCase();

                            agrupados.putIfAbsent(mesNombre, new HashMap<>());
                            agrupados.get(mesNombre).putIfAbsent(diaStr, new ArrayList<>());
                            agrupados.get(mesNombre).get(diaStr).add(doc);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    for (String mesNombre : agrupados.keySet()) {
                        CardView mesCard = crearGrupoMes(mesNombre);
                        LinearLayout diasLayout = mesCard.findViewById(R.id.diasLayout);

                        for (String diaStr : agrupados.get(mesNombre).keySet()) {
                            CardView diaCard = crearGrupoDia(diaStr);
                            LinearLayout avisosLayout = diaCard.findViewById(R.id.ordenesLayout);

                            for (DocumentSnapshot aviso : agrupados.get(mesNombre).get(diaStr)) {
                                View avisoView = LayoutInflater.from(this).inflate(R.layout.item_aviso, avisosLayout, false);
                                TextView textViewDescripcion = avisoView.findViewById(R.id.textViewDescripcionAviso);
                                TextView textViewFecha = avisoView.findViewById(R.id.textViewFechaAviso);
                                CheckBox checkBoxCobrado = avisoView.findViewById(R.id.checkBoxCobradoAviso);
                                Button buttonEliminarAviso = avisoView.findViewById(R.id.buttonEliminarAviso);

                                textViewDescripcion.setText("Aviso: " + aviso.getString("descripcion"));
                                textViewFecha.setText("Fecha: " + aviso.getString("fecha"));
                                checkBoxCobrado.setChecked(aviso.getBoolean("cobrado"));

                                checkBoxCobrado.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    actualizarEstadoCobrado(aviso.getId(), isChecked);
                                });

                                buttonEliminarAviso.setOnClickListener(v -> {
                                    eliminarAviso(aviso.getId(), avisoView, avisosLayout);
                                });

                                avisosLayout.addView(avisoView);
                            }

                            diasLayout.addView(diaCard);
                        }

                        linearLayoutMeses.addView(mesCard);
                    }
                });
    }

    private void actualizarEstadoCobrado(String avisoId, boolean cobrado) {
        db.collection("avisos").document(avisoId)
                .update("cobrado", cobrado)
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private void eliminarAviso(String avisoId, View avisoView, LinearLayout avisosLayout) {
        db.collection("avisos").document(avisoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    avisosLayout.removeView(avisoView);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private CardView crearGrupoMes(String mesNombre) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_mes, linearLayoutMeses, false);
        TextView textView = cardView.findViewById(R.id.textViewMesGrupo);
        textView.setText(mesNombre);
        cardView.findViewById(R.id.textViewMediaPuntos).setVisibility(View.GONE);

        ImageView flecha = cardView.findViewById(R.id.imageViewFlechaMes);
        LinearLayout layout = cardView.findViewById(R.id.diasLayout);
        cardView.findViewById(R.id.headerLayoutMes).setOnClickListener(v -> {
            layout.setVisibility(layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            flecha.setImageResource(layout.getVisibility() == View.VISIBLE ?
                    android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        return cardView;
    }

    private CardView crearGrupoDia(String fecha) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_orden, linearLayoutMeses, false);
        TextView textView = cardView.findViewById(R.id.textViewFechaGrupo);
        textView.setText(fecha);
        cardView.findViewById(R.id.textViewSumaPuntos).setVisibility(View.GONE);

        ImageView flecha = cardView.findViewById(R.id.imageViewFlecha);
        LinearLayout layout = cardView.findViewById(R.id.ordenesLayout);
        cardView.findViewById(R.id.headerLayout).setOnClickListener(v -> {
            layout.setVisibility(layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            flecha.setImageResource(layout.getVisibility() == View.VISIBLE ?
                    android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        return cardView;
    }
}
