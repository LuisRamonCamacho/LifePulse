package com.example.lifepulse;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo, etPassword;
    private Button btnIniciarSesion, btnIrRegistro;

    private DBHelper db;
    private SesionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(this);
        sesion = new SesionManager(this);

        // Si ya existe una sesión, entrar directamente.
        if (sesion.haySesionActiva()) {
            irAlInicio();
            return;
        }

        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnIniciarSesion.setOnClickListener(v -> iniciarSesion());

        btnIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegistroUsuarioActivity.class
            );
            startActivity(intent);
        });
    }

    private void iniciarSesion() {

        String correo = etCorreo.getText().toString()
                .trim().toLowerCase();

        String password = etPassword.getText().toString();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo electrónico no válido");
            return;
        }

        Cursor cursor = null;

        try {
            cursor = db.obtenerUsuarioPorCorreo(correo);

            if (cursor != null && cursor.moveToFirst()) {

                int idUsuario = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id_usuario")
                );

                String nombre = cursor.getString(
                        cursor.getColumnIndexOrThrow("nombre")
                );

                String hashGuardado = cursor.getString(
                        cursor.getColumnIndexOrThrow("password_hash")
                );

                boolean passwordCorrecta =
                        PasswordUtils.verificarPassword(
                                password,
                                hashGuardado
                        );

                if (passwordCorrecta) {

                    sesion.guardarSesion(idUsuario, nombre);

                    Toast.makeText(this,
                            "Bienvenido a LifePulse, " + nombre,
                            Toast.LENGTH_SHORT).show();

                    irAlInicio();

                } else {
                    Toast.makeText(this,
                            "Correo o contraseña incorrectos",
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this,
                        "Correo o contraseña incorrectos",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this,
                    "No fue posible iniciar sesión",
                    Toast.LENGTH_LONG).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void irAlInicio() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }
}

