package com.example.lifepulse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistroUsuarioActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etCorreo;
    private EditText etPassword;
    private EditText etConfirmarPassword;

    private Button btnRegistrar;
    private Button btnVolverLogin;

    private DBHelper db;
    private SesionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        db = new DBHelper(this);
        sesion = new SesionManager(this);

        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        btnVolverLogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {

        String nombre = etNombre.getText().toString().trim();

        String correo = etCorreo.getText().toString()
                .trim().toLowerCase();

        String password = etPassword.getText().toString();

        String confirmarPassword =
                etConfirmarPassword.getText().toString();

        if (nombre.isEmpty() || correo.isEmpty()
                || password.isEmpty() || confirmarPassword.isEmpty()) {

            Toast.makeText(this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingresa un correo válido");
            return;
        }

        if (password.length() < 8) {
            etPassword.setError(
                    "La contraseña debe tener al menos 8 caracteres"
            );
            return;
        }

        if (!password.equals(confirmarPassword)) {
            etConfirmarPassword.setError(
                    "Las contraseñas no coinciden"
            );
            return;
        }

        try {

            if (db.existeCorreo(correo)) {
                etCorreo.setError("Este correo ya está registrado");
                return;
            }

            String passwordHash =
                    PasswordUtils.generarHash(password);

            long idUsuario = db.crearUsuario(
                    nombre,
                    correo,
                    passwordHash
            );

            if (idUsuario != -1) {

                sesion.guardarSesion((int) idUsuario, nombre);

                Toast.makeText(this,
                        "Cuenta creada correctamente",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(
                        RegistroUsuarioActivity.this,
                        MainActivity.class
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this,
                        "No se pudo crear la cuenta. Verifica el correo.",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "Ocurrió un error al registrar el usuario",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }
}

