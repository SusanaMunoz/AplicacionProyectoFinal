package com.example.tdc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText editTextOrdenTrabajo, editTextCodigo;
    private TextView textViewFecha;
    private Button buttonGuardar, buttonAñadirAviso, buttonVerOrdenes,
            buttonVerAvisos, buttonTrabajoEspecial, buttonVerTrabajosEspeciales;
    private FirestoreHelper firestoreHelper;
    private FirebaseAuth mAuth;

    private static final HashMap<String, Double> TABLA_PUNTOS = new HashMap<String, Double>() {{
        put("741", 1.73); put("742", 2.80); put("743", 3.24); put("745", 0.90); put("746", 1.70);
        put("747", 2.00); put("711", 2.99); put("660", 1.00); put("717", 1.20); put("661", 2.00);
        put("5745", 0.60); put("662", 0.20); put("663", 0.90); put("715", 1.73); put("719", 0.90);
        put("723", 0.30); put("724", 2.70); put("070", 1.50); put("712", 1.00); put("713", 1.00);
        put("714", 1.50); put("716", 1.20); put("718", 0.10); put("720", 1.00); put("721", 1.00);
        put("722", 1.00); put("725", 0.50); put("726", 0.90); put("071", 0.60); put("072", 10.00);
        put("882", 0.10); put("757", 0.10); put("756", 0.30); put("5740", 1.20); put("5885", 1.00);
        put("5742", 4.00); put("5743", 2.50); put("5879", 0.90); put("699", 1.98); put("700", 2.30);
        put("701", 0.12); put("702", 4.60); put("703", 1.50); put("704", 2.19); put("705", 0.50);
        put("706", 1.12); put("707", 2.00); put("708", 2.00); put("709", 1.60); put("710", 0.60);
        put("673", 3.45); put("674", 4.00); put("680", 3.45); put("681", 3.50); put("759", 2.20);
        put("683", 3.11); put("684", 1.21); put("685", 1.21); put("760", 1.50); put("761", 2.00);
        put("762", 0.60); put("763", 1.20); put("764", 0.40); put("765", 1.30); put("766", 0.50);
        put("688", 1.10); put("689", 30.00); put("690", 20.00); put("691", 4.60); put("692", 1.34);
        put("732", 1.00); put("731", 2.00); put("733", 0.50); put("734", 6.00); put("735", 2.00);
        put("738", 1.50); put("739", 2.50); put("080", 1.00); put("081", 0.50); put("330", 0.30);
        put("5744", 0.50); put("0049", 3.00); put("5879", 3.5);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firestoreHelper = new FirestoreHelper();
        mAuth = FirebaseAuth.getInstance();

        inicializarVistas();
        configurarFechaYReinicios();
        configurarBotones();
        programarTareasPeriodicas();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void inicializarVistas() {
        editTextOrdenTrabajo = findViewById(R.id.editTextOrdenTrabajo);
        editTextCodigo = findViewById(R.id.editTextCodigo);
        textViewFecha = findViewById(R.id.textViewFecha);

        int[] botonesIds = {
                R.id.buttonGuardar, R.id.buttonAñadirAviso, R.id.buttonVerOrdenes,
                R.id.buttonVerAvisos, R.id.buttonAñadirTrabajosEspeciales,
                R.id.buttonVerTrabajosEspeciales
        };

        Button[] botones = new Button[botonesIds.length];
        for (int i = 0; i < botonesIds.length; i++) {
            botones[i] = findViewById(botonesIds[i]);
        }

        buttonGuardar = botones[0];
        buttonAñadirAviso = botones[1];
        buttonVerOrdenes = botones[2];
        buttonVerAvisos = botones[3];
        buttonTrabajoEspecial = botones[4];
        buttonVerTrabajosEspeciales = botones[5];
    }

    private void configurarBotones() {
        buttonGuardar.setOnClickListener(v -> guardarOrdenTrabajo());
        buttonAñadirAviso.setOnClickListener(v -> startActivityForResult(
                new Intent(this, AñadirAvisoActivity.class), 1));
        buttonVerOrdenes.setOnClickListener(v -> startActivityForResult(
                new Intent(this, OrdenesActivity.class), 2));
        buttonVerAvisos.setOnClickListener(v -> startActivity(
                new Intent(this, AvisosGuardadosActivity.class)));
        buttonTrabajoEspecial.setOnClickListener(v -> startActivity(
                new Intent(this, TrabajoEspecialActivity.class)));
        buttonVerTrabajosEspeciales.setOnClickListener(v -> startActivity(
                new Intent(this, TrabajosEspecialesGuardadosActivity.class)));
    }

    private void guardarOrdenTrabajo() {
        String orden = editTextOrdenTrabajo.getText().toString().trim();
        String codigoInput = editTextCodigo.getText().toString().trim();

        if (orden.isEmpty() || codigoInput.isEmpty()) {
            mostrarError("Complete todos los campos", editTextCodigo);
            return;
        }

        StringBuilder codigosValidos = new StringBuilder();
        double total = 0;

        for (String codigo : codigoInput.split("\\+")) {
            codigo = codigo.trim();
            Double puntos = TABLA_PUNTOS.get(codigo);

            if (puntos == null) {
                mostrarError("Código inválido: " + codigo, editTextCodigo);
                return;
            }

            total += puntos;
            if (codigosValidos.length() > 0) codigosValidos.append("+");
            codigosValidos.append(codigo);
        }

        guardarEnBD(orden, codigosValidos.toString(), total);

        ocultarTeclado();
    }

    private void ocultarTeclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void guardarEnBD(String orden, String codigos, double puntos) {
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> values = new HashMap<>();
        values.put("ordenTrabajo", orden);
        values.put("codigo", codigos);
        values.put("puntos", puntos);
        values.put("fecha", fecha);

        firestoreHelper.guardarOrdenTrabajo(values);

        editTextOrdenTrabajo.setText("");
        editTextCodigo.setText("");
        Toast.makeText(this, "Orden guardada", Toast.LENGTH_SHORT).show();
    }

    private void mostrarError(String mensaje, EditText campo) {
        campo.setError(mensaje);
        campo.requestFocus();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void configurarFechaYReinicios() {
        Calendar cal = Calendar.getInstance();
        textViewFecha.setText(String.format("Fecha: %td/%tm/%tY", cal, cal, cal));

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String fechaActual = fechaActual();

        if (!fechaActual.equals(prefs.getString("ultimaFechaReinicio", ""))) {
            reiniciarPuntos();
            prefs.edit().putString("ultimaFechaReinicio", fechaActual).apply();
        }
    }

    private void reiniciarPuntos() {
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
            Toast.makeText(this, "Reinicio mensual completado", Toast.LENGTH_SHORT).show();
        }
    }

    private String fechaActual() {
        Calendar cal = Calendar.getInstance();
        return String.format("%td/%tm/%tY", cal, cal, cal);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Toast.makeText(this, "Aviso guardado correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void reiniciarTrabajosEspeciales() {
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
            Toast.makeText(this, "Reinicio mensual de trabajos especiales completado", Toast.LENGTH_SHORT).show();
        }
    }

    private void programarTareasPeriodicas() {
        WorkManager workManager = WorkManager.getInstance(this);

        PeriodicWorkRequest resumenDiarioRequest = new PeriodicWorkRequest.Builder(
                ResumenDiarioWorker.class, 24, TimeUnit.HOURS)
                .build();
        workManager.enqueueUniquePeriodicWork("resumenDiario", ExistingPeriodicWorkPolicy.REPLACE, resumenDiarioRequest);

        PeriodicWorkRequest resumenMensualRequest = new PeriodicWorkRequest.Builder(
                ResumenMensualWorker.class, 30, TimeUnit.DAYS)
                .build();
        workManager.enqueueUniquePeriodicWork("resumenMensual", ExistingPeriodicWorkPolicy.REPLACE, resumenMensualRequest);
    }

    private void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", destinatario, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        emailIntent.putExtra(Intent.EXTRA_TEXT, cuerpo);

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No tienes un cliente de correo instalado.", Toast.LENGTH_SHORT).show();
        }
    }
}
