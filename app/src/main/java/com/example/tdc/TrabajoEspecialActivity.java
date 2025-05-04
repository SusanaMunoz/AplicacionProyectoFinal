package com.example.tdc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrabajoEspecialActivity extends Activity {

    private EditText editTextDescripcion;
    private Button buttonGuardarTrabajo;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trabajo_especial);

        editTextDescripcion = findViewById(R.id.editTextDescripcionTrabajo);
        buttonGuardarTrabajo = findViewById(R.id.buttonGuardarTrabajo);
        firestoreHelper = new FirestoreHelper();

        buttonGuardarTrabajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarTrabajoEspecial();
            }
        });
    }

    private void guardarTrabajoEspecial() {
        String descripcion = editTextDescripcion.getText().toString().trim();
        if (descripcion.isEmpty()) {
            Toast.makeText(TrabajoEspecialActivity.this, "Por favor, introduce una descripción", Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        double cantidad = 70;
        firestoreHelper.guardarTrabajoEspecial(descripcion, fecha, false, cantidad, new FirestoreHelper.OnTrabajoEspecialGuardadoListener() {
            @Override
            public void onTrabajoEspecialGuardado(boolean success) {
                if (success) {
                    Toast.makeText(TrabajoEspecialActivity.this, "Trabajo especial guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(TrabajoEspecialActivity.this, "Error al guardar el trabajo especial", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
