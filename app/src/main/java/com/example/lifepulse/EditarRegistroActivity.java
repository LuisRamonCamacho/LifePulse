package com.example.lifepulse;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditarRegistroActivity extends AppCompatActivity {

    EditText etSistolica, etDiastolica, etPulso;
    Button btnGuardarCambios;

    DBHelper db;
    SesionManager sesion;

    int idRegistro;
    int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_registro);

        sesion = new SesionManager(this);

        if (!sesion.haySesionActiva()) {
            Toast.makeText(this,
                    "Inicia sesión para continuar",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        idUsuario = sesion.obtenerIdUsuario();

        etSistolica = findViewById(R.id.etSistolica);
        etDiastolica = findViewById(R.id.etDiastolica);
        etPulso = findViewById(R.id.etPulso);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        db = new DBHelper(this);

        idRegistro = getIntent().getIntExtra("id", -1);

        int sistolica = getIntent().getIntExtra("sistolica", 0);
        int diastolica = getIntent().getIntExtra("diastolica", 0);
        int pulso = getIntent().getIntExtra("pulso", 0);

        if (idRegistro == -1) {
            Toast.makeText(this,
                    "Registro no válido",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etSistolica.setText(String.valueOf(sistolica));
        etDiastolica.setText(String.valueOf(diastolica));
        etPulso.setText(String.valueOf(pulso));

        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {

        String textoSis = etSistolica.getText().toString().trim();
        String textoDia = etDiastolica.getText().toString().trim();
        String textoPulso = etPulso.getText().toString().trim();

        if (textoSis.isEmpty() ||
                textoDia.isEmpty() ||
                textoPulso.isEmpty()) {

            Toast.makeText(this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int nuevaSis;
        int nuevaDia;
        int nuevoPulso;

        try {
            nuevaSis = Integer.parseInt(textoSis);
            nuevaDia = Integer.parseInt(textoDia);
            nuevoPulso = Integer.parseInt(textoPulso);

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Ingresa valores numéricos válidos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaSis <= 0 || nuevaDia <= 0 || nuevoPulso <= 0) {
            Toast.makeText(this,
                    "Los valores deben ser mayores que cero",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String estado = SaludHelper.obtenerEstado(
                nuevaSis,
                nuevaDia
        );

        String recomendacion = SaludHelper.obtenerRecomendacion(
                nuevaSis,
                nuevaDia
        );

        boolean actualizado = db.actualizarRegistro(
                idRegistro,
                idUsuario,
                nuevaSis,
                nuevaDia,
                nuevoPulso,
                estado,
                recomendacion
        );

        if (actualizado) {

            Utils.mostrarMensaje(
                    this,
                    "Éxito",
                    "Registro actualizado correctamente"
            );

            setResult(RESULT_OK);
            finish();

        } else {

            Utils.mostrarMensaje(
                    this,
                    "Error",
                    "No se pudo actualizar el registro. " +
                            "Verifica que pertenezca a tu cuenta."
            );
        }
    }
}
