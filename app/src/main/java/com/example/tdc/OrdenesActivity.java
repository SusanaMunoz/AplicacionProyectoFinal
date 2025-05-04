package com.example.tdc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OrdenesActivity extends AppCompatActivity {
    private LinearLayout linearLayoutMeses;
    private FirestoreHelper firestoreHelper;
    private Map<String, CardView> gruposPorMes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordenes);

        // Inicializa el botón de atrás
        ImageView botonAtras = findViewById(R.id.botonAtras);
        botonAtras.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        linearLayoutMeses = findViewById(R.id.linearLayoutMeses);
        firestoreHelper = new FirestoreHelper();

        cargarOrdenes();
    }

    private void cargarOrdenes() {
        firestoreHelper.obtenerTodasLasOrdenes(new FirestoreHelper.OrdenesCallback() {
            @Override
            public void onOrdenesLoaded(QuerySnapshot queryDocumentSnapshots) {
                Map<String, Map<String, CardView>> diasPorMes = new HashMap<>();
                Map<String, Map<String, Double>> sumaPuntosPorDia = new HashMap<>();
                Map<String, Integer> contadorDiasPorMes = new HashMap<>();
                Map<String, Double> totalPuntosPorMes = new HashMap<>();
                Map<String, Set<String>> diasUnicosPorMes = new HashMap<>();

                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    String idOrden = document.getId();
                    String ordenTrabajo = document.getString("ordenTrabajo");
                    String codigo = document.getString("codigo");
                    double puntos = document.getDouble("puntos");
                    String fechaStr = document.getString("fecha");

                    try {
                        Date fecha = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).parse(fechaStr);
                        String mesNombre = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(fecha);
                        mesNombre = mesNombre.substring(0, 1).toUpperCase() + mesNombre.substring(1).toLowerCase();
                        String diaStr = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES")).format(fecha);

                        diasPorMes.putIfAbsent(mesNombre, new HashMap<>());
                        sumaPuntosPorDia.putIfAbsent(mesNombre, new HashMap<>());
                        contadorDiasPorMes.putIfAbsent(mesNombre, 0);
                        totalPuntosPorMes.putIfAbsent(mesNombre, 0.0);
                        diasUnicosPorMes.putIfAbsent(mesNombre, new HashSet<>());

                        sumaPuntosPorDia.get(mesNombre).put(diaStr, sumaPuntosPorDia.get(mesNombre).getOrDefault(diaStr, 0.0) + puntos);
                        totalPuntosPorMes.put(mesNombre, totalPuntosPorMes.get(mesNombre) + puntos);

                        if (diasUnicosPorMes.get(mesNombre).add(diaStr)) {
                            contadorDiasPorMes.put(mesNombre, contadorDiasPorMes.get(mesNombre) + 1);
                        }

                        CardView mesCard = gruposPorMes.get(mesNombre);
                        if (mesCard == null) {
                            mesCard = crearGrupoDesplegableMes(mesNombre);
                            linearLayoutMeses.addView(mesCard);
                            gruposPorMes.put(mesNombre, mesCard);
                        }

                        Map<String, CardView> dias = diasPorMes.get(mesNombre);
                        CardView diaCard = dias.get(diaStr);
                        if (diaCard == null) {
                            diaCard = crearGrupoDesplegableDia(diaStr);
                            LinearLayout diasLayout = mesCard.findViewById(R.id.diasLayout);
                            diasLayout.addView(diaCard);
                            dias.put(diaStr, diaCard);
                        }

                        LinearLayout ordenesLayout = diaCard.findViewById(R.id.ordenesLayout);
                        agregarOrdenUI(ordenesLayout, idOrden, ordenTrabajo, codigo, puntos, diaStr,
                                sumaPuntosPorDia.get(mesNombre), dias, mesNombre, totalPuntosPorMes,
                                contadorDiasPorMes);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                for (Map.Entry<String, Map<String, CardView>> entryMes : diasPorMes.entrySet()) {
                    String mesNombre = entryMes.getKey();
                    double totalPuntosMes = totalPuntosPorMes.get(mesNombre);
                    int diasTrabajados = contadorDiasPorMes.get(mesNombre);
                    double mediaPuntosMes = diasTrabajados > 0 ? totalPuntosMes / diasTrabajados : 0;

                    CardView mesCard = gruposPorMes.get(mesNombre);
                    TextView textViewMediaPuntos = mesCard.findViewById(R.id.textViewMediaPuntos);
                    textViewMediaPuntos.setText("Media: " + String.format("%.1f", mediaPuntosMes));

                    for (Map.Entry<String, CardView> entryDia : entryMes.getValue().entrySet()) {
                        String fecha = entryDia.getKey();
                        CardView diaCard = entryDia.getValue();
                        TextView textViewSumaPuntos = diaCard.findViewById(R.id.textViewSumaPuntos);
                        textViewSumaPuntos.setText("Total Puntos: " + String.format("%.1f", sumaPuntosPorDia.get(mesNombre).get(fecha)));
                    }
                }
            }
        });
    }

    private CardView crearGrupoDesplegableMes(String mes) {
        CardView mesCard = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_mes, linearLayoutMeses, false);
        TextView textViewMesGrupo = mesCard.findViewById(R.id.textViewMesGrupo);
        textViewMesGrupo.setText(mes);

        TextView textViewMediaPuntos = mesCard.findViewById(R.id.textViewMediaPuntos);

        ImageView imageViewFlechaMes = mesCard.findViewById(R.id.imageViewFlechaMes);
        LinearLayout diasLayout = mesCard.findViewById(R.id.diasLayout);

        mesCard.findViewById(R.id.headerLayoutMes).setOnClickListener(v -> {
            if (diasLayout.getVisibility() == View.GONE) {
                diasLayout.setVisibility(View.VISIBLE);
                imageViewFlechaMes.setImageResource(android.R.drawable.arrow_up_float);
            } else {
                diasLayout.setVisibility(View.GONE);
                imageViewFlechaMes.setImageResource(android.R.drawable.arrow_down_float);
            }
        });

        return mesCard;
    }

    private CardView crearGrupoDesplegableDia(String fecha) {
        CardView diaCard = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_orden, linearLayoutMeses, false);
        TextView textViewFechaGrupo = diaCard.findViewById(R.id.textViewFechaGrupo);
        textViewFechaGrupo.setText(fecha);

        ImageView imageViewFlecha = diaCard.findViewById(R.id.imageViewFlecha);
        LinearLayout ordenesLayout = diaCard.findViewById(R.id.ordenesLayout);

        diaCard.findViewById(R.id.headerLayout).setOnClickListener(v -> {
            if (ordenesLayout.getVisibility() == View.GONE) {
                ordenesLayout.setVisibility(View.VISIBLE);
                imageViewFlecha.setImageResource(android.R.drawable.arrow_up_float);
            } else {
                ordenesLayout.setVisibility(View.GONE);
                imageViewFlecha.setImageResource(android.R.drawable.arrow_down_float);
            }
        });

        return diaCard;
    }

    private void agregarOrdenUI(LinearLayout ordenesLayout, String idOrden, String ordenTrabajo, String codigo, double puntos, String fecha, Map<String, Double> sumaPuntosPorDia, Map<String, CardView> dias, String mesNombre, Map<String, Double> totalPuntosPorMes, Map<String, Integer> contadorDiasPorMes) {
        View ordenView = LayoutInflater.from(this).inflate(R.layout.item_orden, ordenesLayout, false);

        TextView textViewTDC = ordenView.findViewById(R.id.textViewTDC);
        TextView textViewCodigo = ordenView.findViewById(R.id.textViewCodigo);
        TextView textViewPuntosOrden = ordenView.findViewById(R.id.textViewPuntosOrden);

        textViewTDC.setText("TDC: " + ordenTrabajo);
        textViewCodigo.setText("Código: " + codigo);
        textViewPuntosOrden.setText("Puntos: " + puntos);

        Button buttonEliminarOrden = ordenView.findViewById(R.id.buttonEliminarOrden);
        buttonEliminarOrden.setOnClickListener(v -> {
            firestoreHelper.eliminarOrden(idOrden);
            ordenesLayout.removeView(ordenView);

            sumaPuntosPorDia.put(fecha, sumaPuntosPorDia.get(fecha) - puntos);
            totalPuntosPorMes.put(mesNombre, totalPuntosPorMes.get(mesNombre) - puntos);

            CardView diaCard = dias.get(fecha);
            TextView textViewSumaPuntos = diaCard.findViewById(R.id.textViewSumaPuntos);
            textViewSumaPuntos.setText("Total Puntos: " + String.format("%.1f", sumaPuntosPorDia.get(fecha)));

            CardView mesCard = gruposPorMes.get(mesNombre);
            TextView textViewMediaPuntos = mesCard.findViewById(R.id.textViewMediaPuntos);
            double mediaPuntosMes = contadorDiasPorMes.get(mesNombre) > 0 ? totalPuntosPorMes.get(mesNombre) / contadorDiasPorMes.get(mesNombre) : 0;
            textViewMediaPuntos.setText("Media: " + String.format("%.1f", mediaPuntosMes));
        });

        ordenesLayout.addView(ordenView);
    }
}
