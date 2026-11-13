package com.example.lifepulse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button btnGuardar, btnMostrar, btnGrafica, btnCerrarSesion;

    TextView tvPresion, tvPulso, tvEstado, tvRecomendacion,
            tvRiesgo, tvFecha, tvNombreUsuario;

    DBHelper db;
    SesionManager sesion;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        // 1. Cargar primero el diseño
        setContentView(R.layout.activity_main);
        tvFecha = findViewById(R.id.tvFecha);
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario);

        // 2. Inicializar sesión
        sesion = new SesionManager(this);

        // 3. Verificar sesión activa
        if (!sesion.haySesionActiva()) {
            irAlLogin();
            return;
        }

        idUsuario = sesion.obtenerIdUsuario();
        String nombreUsuario = sesion.obtenerNombreUsuario();

        if (nombreUsuario != null && !nombreUsuario.trim().isEmpty()) {
            tvNombreUsuario.setText("Bienvenido, " + nombreUsuario);
        } else {
            tvNombreUsuario.setText("Bienvenido");
        }

        // 4. Inicializar elementos visuales
        btnGuardar = findViewById(R.id.btnGuardar);
        btnMostrar = findViewById(R.id.btnMostrar);
        btnGrafica = findViewById(R.id.btnGrafica);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        tvPresion = findViewById(R.id.tvPresion);
        tvPulso = findViewById(R.id.tvPulso);
        tvEstado = findViewById(R.id.tvEstado);
        tvRecomendacion = findViewById(R.id.tvRecomendacion);
        tvRiesgo = findViewById(R.id.tvRiesgo);

        // 5. Inicializar base de datos
        db = new DBHelper(this);

        // 6. Configurar botón registrar
        btnGuardar.setOnClickListener(v -> {
            startActivity(new Intent(
                    MainActivity.this,
                    RegistroActivity.class
            ));
        });

        // 7. Configurar botón historial
        btnMostrar.setOnClickListener(v -> {
            startActivity(new Intent(
                    MainActivity.this,
                    HistorialActivity.class
            ));
        });

        // 8. Configurar botón gráfica
        btnGrafica.setOnClickListener(v -> {
            startActivity(new Intent(
                    MainActivity.this,
                    GraficaActivity.class
            ));
        });

        // 9. Configurar botón cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> mostrarConfirmacionCierre());

        // 10. Actualizar Dashboard
        actualizarDashboard();
    }

    // Mostrar confirmación para cerrar sesión
    private void mostrarConfirmacionCierre() {

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas salir de LifePulse?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {

                    sesion.cerrarSesion();

                    Intent intent = new Intent(
                            MainActivity.this,
                            LoginActivity.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Regresar al Login
    private void irAlLogin() {

        Intent intent = new Intent(
                this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sesion == null || !sesion.haySesionActiva()) {
            irAlLogin();
            return;
        }

        idUsuario = sesion.obtenerIdUsuario();

        if (db != null && tvPresion != null) {
            actualizarDashboard();
        }
    }

    private void actualizarDashboard() {
        cargarUltimoRegistro();
        actualizarRiesgo();
    }

    // Obtener última medición

    private void cargarUltimoRegistro() {

        Cursor cursor = db.obtenerUltimoRegistro(idUsuario);

        try {

            if (cursor != null && cursor.moveToFirst()) {

                int sistolica = cursor.getInt(
                        cursor.getColumnIndexOrThrow("sistolica"));

                int diastolica = cursor.getInt(
                        cursor.getColumnIndexOrThrow("diastolica"));

                int pulso = cursor.getInt(
                        cursor.getColumnIndexOrThrow("pulso"));

                String fecha = cursor.getString(
                        cursor.getColumnIndexOrThrow("fecha"));

                String estado = cursor.getString(
                        cursor.getColumnIndexOrThrow("estado"));

                String recomendacion = cursor.getString(
                        cursor.getColumnIndexOrThrow("recomendacion"));

                tvPresion.setText(sistolica + "/" + diastolica);

                tvPulso.setText("Pulso: " + pulso + " bpm");

                tvFecha.setText("Última medición: " + fecha);

                tvEstado.setText(estado);

                tvRecomendacion.setText(recomendacion);

                if ("Óptima".equals(estado) || "Normal".equals(estado)) {

                    tvEstado.setTextColor(Color.parseColor("#4CAF50"));

                } else if ("Elevada".equals(estado)) {

                    tvEstado.setTextColor(Color.parseColor("#FFC107"));

                } else {

                    tvEstado.setTextColor(Color.parseColor("#F44336"));
                }

            } else {

                tvPresion.setText("--/--");
                tvPulso.setText("Pulso: -- bpm");
                tvFecha.setText("Última medición: --");
                tvEstado.setText("Sin registros");
                tvRecomendacion.setText("Registra tu primera medición.");

                tvEstado.setTextColor(Color.GRAY);
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }


    // Analizar riesgo de acuerdo con las mediciones
    private void actualizarRiesgo() {

        Cursor cursor = db.obtenerTodos(idUsuario);

        try {

            String riesgo = SaludHelper.analizarRiesgo(cursor);

            tvRiesgo.setText(riesgo);

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
            db = null;
        }
    }
}
