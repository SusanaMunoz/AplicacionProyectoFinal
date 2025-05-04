package com.example.tdc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AñadirAvisoActivity extends AppCompatActivity {
    private EditText editTextDescripcion;
    private Button buttonGuardarAviso;
    private FirestoreHelper firestoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anadir_aviso);

        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        buttonGuardarAviso = findViewById(R.id.buttonGuardarAviso);
        firestoreHelper = new FirestoreHelper();

        buttonGuardarAviso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descripcion = editTextDescripcion.getText().toString().trim();
                if (descripcion.isEmpty()) {
                    Toast.makeText(AñadirAvisoActivity.this, "Por favor, introduce una descripción", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                firestoreHelper.guardarAviso(descripcion, fecha, false);

                Toast.makeText(AñadirAvisoActivity.this, "Aviso guardado correctamente", Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
