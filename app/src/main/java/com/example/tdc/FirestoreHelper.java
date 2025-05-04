package com.example.tdc;

import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void guardarOrdenTrabajo(Map<String, Object> values) {
        db.collection("ordenes")
                .add(values)
                .addOnSuccessListener(documentReference -> Log.d("FirestoreHelper", "Orden guardada: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al guardar orden: ", e));
    }

    public void obtenerTodasLasOrdenes(OrdenesCallback callback) {
        db.collection("ordenes")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        callback.onOrdenesLoaded(queryDocumentSnapshots);
                    } else {
                        Log.d("FirestoreHelper", "Error al obtener las órdenes: ", task.getException());
                    }
                });
    }

    public void eliminarOrden(String ordenId) {
        db.collection("ordenes").document(ordenId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Orden eliminada"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al eliminar orden: ", e));
    }

    public void actualizarEstadoCobrado(String idOrden, boolean cobrado) {
        Map<String, Object> values = new HashMap<>();
        values.put("cobrado", cobrado);

        db.collection("ordenes").document(idOrden)
                .set(values, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Estado cobrado actualizado"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al actualizar estado: ", e));
    }

    public void guardarAviso(String descripcion, String fecha, boolean cobrado) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> values = new HashMap<>();
        values.put("descripcion", descripcion);
        values.put("fecha", fecha);
        values.put("cobrado", cobrado);
        values.put("userId", userId);

        db.collection("avisos")
                .add(values)
                .addOnSuccessListener(documentReference -> Log.d("FirestoreHelper", "Aviso guardado: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("FirestoreHelper", "Error al guardar aviso: ", e));
    }

    public void obtenerTodosLosAvisos(AvisosCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("avisos")
                .whereEqualTo("userId", userId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        if (queryDocumentSnapshots != null) {
                            callback.onAvisosLoaded(queryDocumentSnapshots);
                        } else {
                            Log.e("FirestoreHelper", "QuerySnapshot es nulo");
                        }
                    } else {
                        Log.e("FirestoreHelper", "Error al obtener los avisos: ", task.getException());
                    }
                });
    }

    public void eliminarAviso(String idAviso) {
        db.collection("avisos").document(idAviso)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Aviso eliminado"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al eliminar aviso: ", e));
    }

    public void actualizarEstadoCobradoAviso(String idAviso, boolean cobrado) {
        Map<String, Object> values = new HashMap<>();
        values.put("cobrado", cobrado);

        db.collection("avisos").document(idAviso)
                .set(values, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Estado cobrado actualizado"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al actualizar estado: ", e));
    }

    public void guardarTrabajoEspecial(String descripcion, String fecha, boolean cobrado, double cantidad, OnTrabajoEspecialGuardadoListener listener) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> values = new HashMap<>();
        values.put("descripcion", descripcion);
        values.put("fecha", fecha);
        values.put("cobrado", cobrado);
        values.put("cantidad", cantidad);
        values.put("userId", userId);

        db.collection("trabajos_especiales")
                .whereEqualTo("descripcion", descripcion)
                .whereEqualTo("fecha", fecha)
                .whereEqualTo("userId", userId)
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        if (queryDocumentSnapshots.isEmpty()) {
                            db.collection("trabajos_especiales")
                                    .add(values)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("FirestoreHelper", "Trabajo especial guardado: " + documentReference.getId());
                                        if (listener != null) {
                                            listener.onTrabajoEspecialGuardado(true);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("FirestoreHelper", "Error al guardar trabajo especial: ", e);
                                        if (listener != null) {
                                            listener.onTrabajoEspecialGuardado(false);
                                        }
                                    });
                        } else {
                            Log.d("FirestoreHelper", "El trabajo especial ya existe.");
                            if (listener != null) {
                                listener.onTrabajoEspecialGuardado(false);
                            }
                        }
                    } else {
                        Log.d("FirestoreHelper", "Error al verificar trabajo especial: ", task.getException());
                        if (listener != null) {
                            listener.onTrabajoEspecialGuardado(false);
                        }
                    }
                });
    }

    public void obtenerTodosLosTrabajos(TrabajosEspecialesCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("trabajos_especiales")
                .whereEqualTo("userId", userId)
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryDocumentSnapshots = task.getResult();
                        callback.onTrabajosEspecialesLoaded(queryDocumentSnapshots);
                    } else {
                        Log.d("FirestoreHelper", "Error al obtener los trabajos especiales: ", task.getException());
                    }
                });
    }

    public void eliminarTrabajoEspecial(String idTrabajo) {
        db.collection("trabajos_especiales").document(idTrabajo)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Trabajo especial eliminado"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al eliminar trabajo especial: ", e));
    }

    public void actualizarCobradoTrabajoEspecial(String idTrabajo, boolean cobrado) {
        Map<String, Object> values = new HashMap<>();
        values.put("cobrado", cobrado);

        db.collection("trabajos_especiales").document(idTrabajo)
                .set(values, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("FirestoreHelper", "Estado cobrado actualizado"))
                .addOnFailureListener(e -> Log.d("FirestoreHelper", "Error al actualizar estado cobrado: ", e));
    }

    public interface OrdenesCallback {
        void onOrdenesLoaded(QuerySnapshot queryDocumentSnapshots);
    }

    public interface AvisosCallback {
        void onAvisosLoaded(QuerySnapshot queryDocumentSnapshots);
    }

    public interface TrabajosEspecialesCallback {
        void onTrabajosEspecialesLoaded(QuerySnapshot queryDocumentSnapshots);
    }

    public interface OnTrabajoEspecialGuardadoListener {
        void onTrabajoEspecialGuardado(boolean success);
    }
}