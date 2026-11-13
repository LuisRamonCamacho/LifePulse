package com.example.lifepulse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Registro> lista;
    private final DBHelper db;
    private final SesionManager sesion;

    private final int idUsuario;

    public RegistroAdapter(
            Context context,
            ArrayList<Registro> lista
    ) {
        this.context = context;
        this.lista = lista;
        this.db = new DBHelper(context);
        this.sesion = new SesionManager(context);
        this.idUsuario = sesion.obtenerIdUsuario();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context).inflate(
                R.layout.item_registro,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Registro registro = lista.get(position);

        holder.tvFecha.setText(registro.getFecha());

        holder.tvPresion.setText(
                registro.getSistolica() + "/" +
                        registro.getDiastolica() + " mmHg"
        );

        holder.tvPulso.setText(
                "Pulso: " + registro.getPulso()
        );

        holder.tvEstado.setText(registro.getEstado());

        // Color del estado
        String estado = registro.getEstado();

        if ("Óptima".equals(estado) ||
                "Normal".equals(estado)) {

            holder.tvEstado.setTextColor(
                    Color.parseColor("#4CAF50")
            );

        } else if ("Elevada".equals(estado)) {

            holder.tvEstado.setTextColor(
                    Color.parseColor("#FFC107")
            );

        } else {

            holder.tvEstado.setTextColor(
                    Color.parseColor("#F44336")
            );
        }

        // ELIMINAR REGISTRO
        holder.btnEliminar.setOnClickListener(v -> {

            int posicionActual = holder.getBindingAdapterPosition();

            if (posicionActual == RecyclerView.NO_POSITION) {
                return;
            }

            Registro registroActual = lista.get(posicionActual);

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar registro")
                    .setMessage(
                            "¿Deseas eliminar esta medición?"
                    )
                    .setPositiveButton("Sí", (dialog, which) -> {

                        if (!sesion.haySesionActiva()) {
                            Toast.makeText(
                                    context,
                                    "La sesión ha terminado",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        boolean eliminado = db.eliminar(
                                registroActual.getId(),
                                idUsuario
                        );

                        if (eliminado) {

                            lista.remove(posicionActual);

                            notifyItemRemoved(posicionActual);

                            Toast.makeText(
                                    context,
                                    "Registro eliminado",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    context,
                                    "No se pudo eliminar el registro",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // EDITAR REGISTRO
        holder.btnEditar.setOnClickListener(v -> {

            int posicionActual = holder.getBindingAdapterPosition();

            if (posicionActual == RecyclerView.NO_POSITION) {
                return;
            }

            if (!sesion.haySesionActiva()) {
                Toast.makeText(
                        context,
                        "Inicia sesión para continuar",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Registro registroActual = lista.get(posicionActual);

            Intent intent = new Intent(
                    context,
                    EditarRegistroActivity.class
            );

            intent.putExtra("id", registroActual.getId());
            intent.putExtra(
                    "sistolica",
                    registroActual.getSistolica()
            );
            intent.putExtra(
                    "diastolica",
                    registroActual.getDiastolica()
            );
            intent.putExtra(
                    "pulso",
                    registroActual.getPulso()
            );

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvFecha, tvPresion, tvPulso, tvEstado;
        Button btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvPresion = itemView.findViewById(R.id.tvPresion);
            tvPulso = itemView.findViewById(R.id.tvPulso);
            tvEstado = itemView.findViewById(R.id.tvEstado);

            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
