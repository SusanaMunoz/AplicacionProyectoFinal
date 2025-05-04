package com.example.tdc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AvisoActivity extends Activity {

    private EditText editTextDescripcion;
    private CheckBox checkBoxCobrado;
    private Button buttonGuardarAviso;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_aviso);

        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        checkBoxCobrado = findViewById(R.id.checkBoxCobrado);
        buttonGuardarAviso = findViewById(R.id.buttonGuardarAviso);
        db = FirebaseFirestore.getInstance();

        buttonGuardarAviso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarAviso();
            }
        });
    }

    private void guardarAviso() {
        String descripcion = editTextDescripcion.getText().toString().trim();
        boolean cobrado = checkBoxCobrado.isChecked();
        String fecha = fechaActual();

        // Crear un nuevo objeto de tipo Aviso y guardar en Firestore
        Aviso aviso = new Aviso(descripcion, fecha, cobrado);

        db.collection("avisos")
                .add(aviso)
                .addOnSuccessListener(documentReference -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("descripcion", descripcion);
                    resultIntent.putExtra("fecha", fecha);
                    resultIntent.putExtra("cobrado", cobrado);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }

    private String fechaActual() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day + "/" + month + "/" + year;
    }
}
