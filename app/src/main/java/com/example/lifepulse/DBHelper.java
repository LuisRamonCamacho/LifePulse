package com.example.lifepulse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "presion.db";
    private static final int DB_VERSION = 2;

    // Constructor utilizado por la aplicación normal.
    public DBHelper(Context context) {
        this(context, DB_NAME);
    }

    // Constructor para utilizar una base de datos independiente en las pruebas.
    DBHelper(Context context, String nombreBaseDatos) {
        super(context, nombreBaseDatos, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Tabla de usuarios
        db.execSQL(
                "CREATE TABLE usuarios (" +
                        "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nombre TEXT NOT NULL," +
                        "correo TEXT NOT NULL UNIQUE," +
                        "password_hash TEXT NOT NULL)"
        );

        // Tabla de registros médicos
        db.execSQL(
                "CREATE TABLE registros (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id_usuario INTEGER NOT NULL," +
                        "sistolica INTEGER," +
                        "diastolica INTEGER," +
                        "pulso INTEGER," +
                        "fecha TEXT," +
                        "estado TEXT," +
                        "recomendacion TEXT," +
                        "FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        // Migración de la versión 1 a la versión 2.
        if (oldVersion < 2) {

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS usuarios (" +
                            "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "nombre TEXT NOT NULL," +
                            "correo TEXT NOT NULL UNIQUE," +
                            "password_hash TEXT NOT NULL)"
            );

            // Conserva los registros existentes.
            db.execSQL(
                    "ALTER TABLE registros ADD COLUMN id_usuario INTEGER"
            );
        }
    }

    // Crear un usuario y asignar registros antiguos al primer usuario.
    public long crearUsuario(String nombre, String correo, String passwordHash) {

        SQLiteDatabase db = this.getWritableDatabase();
        long resultado = -1;

        db.beginTransaction();

        try {
            // Comprobar si será el primer usuario.
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM usuarios",
                    null
            );

            boolean esPrimerUsuario = false;

            if (cursor.moveToFirst()) {
                esPrimerUsuario = cursor.getInt(0) == 0;
            }

            cursor.close();

            // Preparar los datos del nuevo usuario.
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            values.put("correo", correo);
            values.put("password_hash", passwordHash);

            resultado = db.insert("usuarios", null, values);

            if (resultado != -1) {

                // Solo el primer usuario recibe los registros anteriores.
                if (esPrimerUsuario) {
                    ContentValues registrosAntiguos = new ContentValues();
                    registrosAntiguos.put("id_usuario", (int) resultado);

                    db.update(
                            "registros",
                            registrosAntiguos,
                            "id_usuario IS NULL",
                            null
                    );
                }

                db.setTransactionSuccessful();
            }

        } finally {
            db.endTransaction();
        }

        return resultado;
    }

    // Buscar usuario por correo
    public Cursor obtenerUsuarioPorCorreo(String correo) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM usuarios WHERE correo = ?",
                new String[]{correo}
        );
    }

    // Obtener usuario por ID
    public Cursor obtenerUsuarioPorId(int idUsuario) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM usuarios WHERE id_usuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );
    }

    // Guardar una medición asociada al usuario
    public boolean insertar(int idUsuario,
                            int sistolica,
                            int diastolica,
                            int pulso,
                            String fecha,
                            String estado,
                            String recomendacion) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("id_usuario", idUsuario);
        values.put("sistolica", sistolica);
        values.put("diastolica", diastolica);
        values.put("pulso", pulso);
        values.put("fecha", fecha);
        values.put("estado", estado);
        values.put("recomendacion", recomendacion);

        long resultado = db.insert("registros", null, values);

        db.close();

        return resultado != -1;
    }

    // Obtener registros de un usuario
    // Obtener registros de un usuario
    public Cursor obtenerTodos(int idUsuario) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, id_usuario, sistolica, diastolica, " +
                        "pulso, fecha, estado, recomendacion " +
                        "FROM registros " +
                        "WHERE id_usuario = ? " +
                        "ORDER BY id DESC",
                new String[]{String.valueOf(idUsuario)}
        );
    }

    // Obtener último registro de un usuario
    public Cursor obtenerUltimoRegistro(int idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, id_usuario, sistolica, diastolica, " +
                        "pulso, fecha, estado, recomendacion " +
                        "FROM registros " +
                        "WHERE id_usuario = ? " +
                        "ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(idUsuario)}
        );
    }

    // Eliminar un registro verificando su propietario
    public boolean eliminar(int id, int idUsuario) {

        SQLiteDatabase db = this.getWritableDatabase();

        int resultado = db.delete(
                "registros",
                "id = ? AND id_usuario = ?",
                new String[]{
                        String.valueOf(id),
                        String.valueOf(idUsuario)
                }
        );

        db.close();

        return resultado > 0;
    }

    // Actualizar un registro verificando su propietario
    public boolean actualizarRegistro(int id,
                                      int idUsuario,
                                      int sistolica,
                                      int diastolica,
                                      int pulso,
                                      String estado,
                                      String recomendacion) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("sistolica", sistolica);
        values.put("diastolica", diastolica);
        values.put("pulso", pulso);
        values.put("estado", estado);
        values.put("recomendacion", recomendacion);

        int filas = db.update(
                "registros",
                values,
                "id = ? AND id_usuario = ?",
                new String[]{
                        String.valueOf(id),
                        String.valueOf(idUsuario)
                }
        );

        db.close();

        return filas > 0;
    }

    // Asignar registros antiguos al primer usuario creado.
    public void asignarRegistrosAntiguos(int idUsuario) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_usuario", idUsuario);

        db.update(
                "registros",
                values,
                "id_usuario IS NULL",
                null
        );

        db.close();
    }

    // Comprobar si existe un correo
    public boolean existeCorreo(String correo) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id_usuario FROM usuarios WHERE correo = ?",
                new String[]{correo}
        );

        boolean existe = cursor.moveToFirst();

        cursor.close();
        db.close();

        return existe;
    }
}
