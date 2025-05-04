package com.example.tdc;

import android.content.Context;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResumenDiarioWorker extends Worker {

    public ResumenDiarioWorker(Context context, WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public Result doWork() {
        String correoUsuario = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (correoUsuario != null) {
            String mensaje = obtenerResumen("diario");
            EmailUtil.enviarCorreo(correoUsuario, "Resumen Diario", mensaje);
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    private String obtenerResumen(String tipo) {
        // Obtener los datos del día o del mes desde Firestore y construir el mensaje
        // Puedes personalizar esta parte según lo que necesites
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Lógica para obtener el resumen (dependiendo si es diario o mensual)
        return "Resumen del " + tipo + ": [Datos aquí]";
    }
}
