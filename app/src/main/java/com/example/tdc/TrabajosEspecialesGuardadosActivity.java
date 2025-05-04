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
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TrabajosEspecialesGuardadosActivity extends AppCompatActivity {
    private LinearLayout linearLayoutMeses;
    private FirebaseFirestore db;
    private String userId;
    private Map<String, CardView> cardsMeses = new HashMap<>();
    private Map<String, CardView> cardsDias = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabajos_especiales_guardados);

        linearLayoutMeses = findViewById(R.id.linearLayoutTrabajosContainer);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cambiar título
        TextView titulo = findViewById(R.id.textViewTitulo);
        titulo.setText("Trabajos Guardados");

        findViewById(R.id.botonAtras).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        cargarTrabajosEspeciales();
    }

    private void cargarTrabajosEspeciales() {
        db.collection("trabajos_especiales")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Map<String, List<DocumentSnapshot>>> agrupados = new HashMap<>();

                    // 1. Agrupar por mes y día
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String fechaStr = doc.getString("fecha");
                        try {
                            Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse(fechaStr);
                            String mesNombre = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(fecha);
                            String diaStr = new SimpleDateFormat("dd/MM/yyyy").format(fecha);

                            mesNombre = mesNombre.substring(0, 1).toUpperCase() + mesNombre.substring(1);

                            if (!agrupados.containsKey(mesNombre)) {
                                agrupados.put(mesNombre, new HashMap<>());
                            }
                            if (!agrupados.get(mesNombre).containsKey(diaStr)) {
                                agrupados.get(mesNombre).put(diaStr, new ArrayList<>());
                            }
                            agrupados.get(mesNombre).get(diaStr).add(doc);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // 2. Construir la vista
                    for (String mesNombre : agrupados.keySet()) {
                        CardView mesCard = crearGrupoMes(mesNombre);
                        LinearLayout contenedorDias = mesCard.findViewById(R.id.trabajosLayout);
                        int totalMes = 0;

                        for (String diaStr : agrupados.get(mesNombre).keySet()) {
                            CardView diaCard = crearGrupoDia(diaStr);
                            LinearLayout contenedorTrabajos = diaCard.findViewById(R.id.trabajosLayout);
                            int totalDia = 0;

                            for (DocumentSnapshot trabajo : agrupados.get(mesNombre).get(diaStr)) {
                                boolean cobrado = trabajo.getBoolean("cobrado");
                                double cantidad = trabajo.getDouble("cantidad");

                                if (cobrado) {
                                    totalDia += cantidad;
                                    totalMes += cantidad;
                                }

                                agregarTrabajo(contenedorTrabajos, trabajo, mesNombre, diaStr);
                            }

                            // Actualizar total del día
                            TextView tvTotalDia = diaCard.findViewById(R.id.textViewTotalMes);
                            tvTotalDia.setText(totalDia + "€");

                            contenedorDias.addView(diaCard);
                            cardsDias.put(diaStr, diaCard);
                        }

                        // Actualizar total del mes
                        TextView tvTotalMes = mesCard.findViewById(R.id.textViewTotalMes);
                        tvTotalMes.setText(totalMes + "€");

                        linearLayoutMeses.addView(mesCard);
                        cardsMeses.put(mesNombre, mesCard);
                    }
                });
    }

    private void agregarTrabajo(LinearLayout contenedor, DocumentSnapshot trabajo, String mesNombre, String diaStr) {
        View trabajoView = LayoutInflater.from(this).inflate(R.layout.item_trabajo_especial, contenedor, false);

        TextView tvDescripcion = trabajoView.findViewById(R.id.textViewDescripcion);
        TextView tvFecha = trabajoView.findViewById(R.id.textViewFecha);
        CheckBox cbCobrado = trabajoView.findViewById(R.id.checkBoxCobrado);
        TextView tvCantidad = trabajoView.findViewById(R.id.textViewCantidad);
        Button btnEliminar = trabajoView.findViewById(R.id.buttonEliminar);

        String descripcion = trabajo.getString("descripcion");
        String fecha = trabajo.getString("fecha");
        boolean cobrado = Boolean.TRUE.equals(trabajo.getBoolean("cobrado"));
        double cantidad = trabajo.getDouble("cantidad");

        tvDescripcion.setText(descripcion);
        tvFecha.setText(fecha);
        tvCantidad.setText(cantidad + "€");
        cbCobrado.setChecked(cobrado);

        cbCobrado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Actualizar en Firestore
            db.collection("trabajos_especiales").document(trabajo.getId())
                    .update("cobrado", isChecked);

            // Actualizar totales
            double cambio = isChecked ? cantidad : -cantidad;
            actualizarTotales(mesNombre, diaStr, cambio);
        });

        btnEliminar.setOnClickListener(v -> {
            // Eliminar de Firestore
            db.collection("trabajos_especiales").document(trabajo.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Si estaba cobrado, restar del total
                        if (cbCobrado.isChecked()) {
                            actualizarTotales(mesNombre, diaStr, -cantidad);
                        }
                        contenedor.removeView(trabajoView);
                    });
        });

        contenedor.addView(trabajoView);
    }

    private void actualizarTotales(String mesNombre, String diaStr, double cambio) {
        // Actualizar día
        CardView diaCard = cardsDias.get(diaStr);
        if (diaCard != null) {
            TextView tvTotalDia = diaCard.findViewById(R.id.textViewTotalMes);
            try {
                double totalActual = Double.parseDouble(tvTotalDia.getText().toString().replace("€", ""));
                tvTotalDia.setText((totalActual + cambio) + "€");
            } catch (NumberFormatException e) {
                tvTotalDia.setText(cambio + "€");
            }
        }

        // Actualizar mes
        CardView mesCard = cardsMeses.get(mesNombre);
        if (mesCard != null) {
            TextView tvTotalMes = mesCard.findViewById(R.id.textViewTotalMes);
            try {
                double totalActual = Double.parseDouble(tvTotalMes.getText().toString().replace("€", ""));
                tvTotalMes.setText((totalActual + cambio) + "€");
            } catch (NumberFormatException e) {
                tvTotalMes.setText(cambio + "€");
            }
        }
    }

    private CardView crearGrupoMes(String mesNombre) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_mes_trabajo, linearLayoutMeses, false);
        TextView textView = cardView.findViewById(R.id.textViewMesGrupoTrabajo);
        textView.setText(mesNombre);

        ImageView flecha = cardView.findViewById(R.id.imageViewFlechaMesTrabajo);
        LinearLayout layout = cardView.findViewById(R.id.trabajosLayout);
        cardView.findViewById(R.id.headerLayoutMesTrabajo).setOnClickListener(v -> {
            layout.setVisibility(layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            flecha.setImageResource(layout.getVisibility() == View.VISIBLE ?
                    android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        return cardView;
    }

    private CardView crearGrupoDia(String fecha) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.item_grupo_mes_trabajo, linearLayoutMeses, false);
        TextView textView = cardView.findViewById(R.id.textViewMesGrupoTrabajo);
        textView.setText(fecha);

        ImageView flecha = cardView.findViewById(R.id.imageViewFlechaMesTrabajo);
        LinearLayout layout = cardView.findViewById(R.id.trabajosLayout);
        cardView.findViewById(R.id.headerLayoutMesTrabajo).setOnClickListener(v -> {
            layout.setVisibility(layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            flecha.setImageResource(layout.getVisibility() == View.VISIBLE ?
                    android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        });
        return cardView;
    }
}