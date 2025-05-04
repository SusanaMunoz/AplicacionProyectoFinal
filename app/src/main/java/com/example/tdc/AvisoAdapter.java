package com.example.tdc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;

public class AvisoAdapter extends ArrayAdapter<Aviso> {
    private FirestoreHelper firestoreHelper;

    public AvisoAdapter(Context context, List<Aviso> avisos, FirestoreHelper firestoreHelper) {
        super(context, 0, avisos);
        this.firestoreHelper = firestoreHelper;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Aviso aviso = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView textViewDescripcion = convertView.findViewById(android.R.id.text1);
        TextView textViewFecha = convertView.findViewById(android.R.id.text2);

        textViewDescripcion.setText(aviso.getDescripcion());
        textViewFecha.setText(aviso.getFecha());

        return convertView;
    }
}
