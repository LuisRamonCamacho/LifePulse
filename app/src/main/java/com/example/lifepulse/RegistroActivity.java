package com.example.lifepulse;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    EditText etSistolica, etDiastolica, etPulso;
    Button btnGuardarRegistro;

    DBHelper db;
    SesionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etSistolica = findViewById(R.id.etSistolica);
        etDiastolica = findViewById(R.id.etDiastolica);
        etPulso = findViewById(R.id.etPulso);

        btnGuardarRegistro = findViewById(R.id.btnGuardarRegistro);

        db = new DBHelper(this);
        sesion = new SesionManager(this);

        // Verificar que exista una sesión activa.
        if (!sesion.haySesionActiva()) {
            Toast.makeText(this,
                    "Debes iniciar sesión primero",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnGuardarRegistro.setOnClickListener(v -> guardarRegistro());
    }

    private void guardarRegistro() {

        String txtSistolica = etSistolica.getText().toString().trim();
        String txtDiastolica = etDiastolica.getText().toString().trim();
        String txtPulso = etPulso.getText().toString().trim();

        if (txtSistolica.isEmpty()) {
            etSistolica.setError("Ingrese el valor sistólico");
            return;
        }

        if (txtDiastolica.isEmpty()) {
            etDiastolica.setError("Ingrese el valor diastólico");
            return;
        }

        if (txtPulso.isEmpty()) {
            etPulso.setError("Ingrese el pulso");
            return;
        }

        int sistolica;
        int diastolica;
        int pulso;

        try {
            sistolica = Integer.parseInt(txtSistolica);
            diastolica = Integer.parseInt(txtDiastolica);
            pulso = Integer.parseInt(txtPulso);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Ingrese únicamente números enteros",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validaciones básicas de entrada.
        if (sistolica <= 0 || diastolica <= 0 || pulso <= 0) {
            Toast.makeText(this,
                    "Los valores deben ser mayores que cero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String estado = SaludHelper.obtenerEstado(
                sistolica,
                diastolica
        );

        String recomendacion = SaludHelper.obtenerRecomendacion(
                sistolica,
                diastolica
        );

        String alerta = SaludHelper.alertaMedica(
                sistolica,
                diastolica
        );

        String fecha = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        // Obtener el usuario que inició sesión.
        int idUsuario = sesion.obtenerIdUsuario();

        // Guardar la medición asociada a ese usuario.
        boolean insertado = db.insertar(
                idUsuario,
                sistolica,
                diastolica,
                pulso,
                fecha,
                estado,
                recomendacion
        );

        if (insertado) {

            new AlertDialog.Builder(this)
                    .setTitle("Resultado de la medición")
                    .setMessage(
                            "Estado: " + estado +
                                    "\n\n" + recomendacion +
                                    "\n\n" + alerta
                    )
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .show();

        } else {
            Toast.makeText(this,
                    "No fue posible guardar la medición",
                    Toast.LENGTH_LONG).show();
        }
    }
}
