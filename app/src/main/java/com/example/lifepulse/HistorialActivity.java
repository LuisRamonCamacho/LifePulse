package com.example.lifepulse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class HistorialActivity extends AppCompatActivity {

    RecyclerView recyclerHistorial;
    Button btnPDF;

    ArrayList<Registro> lista;
    RegistroAdapter adapter;

    DBHelper db;
    SesionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerHistorial = findViewById(R.id.recyclerHistorial);
        btnPDF = findViewById(R.id.btnPDF);

        recyclerHistorial.setLayoutManager(
                new LinearLayoutManager(this)
        );

        lista = new ArrayList<>();

        db = new DBHelper(this);
        sesion = new SesionManager(this);

        if (!sesion.haySesionActiva()) {
            Toast.makeText(this,
                    "Debes iniciar sesión primero",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnPDF.setOnClickListener(v -> generarPDF());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && sesion != null &&
                sesion.haySesionActiva()) {
            cargarRegistros();
        }
    }

    private void cargarRegistros() {

        lista.clear();

        int idUsuario = sesion.obtenerIdUsuario();

        Cursor cursor = db.obtenerTodos(idUsuario);

        try {
            while (cursor.moveToNext()) {

                // Orden de columnas:
                // 0 id
                // 1 id_usuario
                // 2 sistolica
                // 3 diastolica
                // 4 pulso
                // 5 fecha
                // 6 estado
                // 7 recomendacion

                lista.add(
                        new Registro(
                                cursor.getInt(0),
                                cursor.getInt(2),
                                cursor.getInt(3),
                                cursor.getInt(4),
                                cursor.getString(5),
                                cursor.getString(6),
                                cursor.getString(7)
                        )
                );
            }
        } finally {
            cursor.close();
        }

        adapter = new RegistroAdapter(this, lista);
        recyclerHistorial.setAdapter(adapter);
    }


    private void generarPDF() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this,
                    "Esta versión de guardado requiere Android 10 o superior.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        int idUsuario = sesion.obtenerIdUsuario();
        String nombreUsuario = sesion.obtenerNombreUsuario();

        Uri uriArchivo = null;
        Document document = null;
        Cursor cursor = null;

        try {

            // Nombre del archivo PDF
            String fechaActual = new SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
            ).format(new Date());

            String nombreArchivo = "Reporte_LifePulse_Usuario_"
                    + idUsuario + "_" + fechaActual + ".pdf";

            // Datos del archivo
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            // Crear el archivo en Descargas
            uriArchivo = getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
            );

            if (uriArchivo == null) {
                throw new Exception("No se pudo crear el archivo PDF.");
            }

            // Abrir el archivo para escribir
            OutputStream outputStream =
                    getContentResolver().openOutputStream(uriArchivo);

            if (outputStream == null) {
                throw new Exception("No se pudo abrir el archivo.");
            }

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);

            // Encabezado
            document.add(new Paragraph("LIFEPULSE"));
            document.add(new Paragraph("REPORTE DE PRESIÓN ARTERIAL"));
            document.add(new Paragraph("Usuario: " + nombreUsuario));
            document.add(new Paragraph("\n"));

            // Obtener registros del usuario
            cursor = db.obtenerTodos(idUsuario);

            if (cursor == null || !cursor.moveToFirst()) {

                document.add(
                        new Paragraph("No existen mediciones registradas.")
                );

            } else {

                // Recorrer todas las mediciones
                do {

                    document.add(
                            new Paragraph(
                                    "Fecha: " + cursor.getString(5)
                            )
                    );

                    document.add(
                            new Paragraph(
                                    "Presión arterial: " +
                                            cursor.getInt(2) + "/" +
                                            cursor.getInt(3) + " mmHg"
                            )
                    );

                    document.add(
                            new Paragraph(
                                    "Pulso: " + cursor.getInt(4) + " bpm"
                            )
                    );

                    document.add(
                            new Paragraph(
                                    "Estado: " + cursor.getString(6)
                            )
                    );

                    document.add(
                            new Paragraph(
                                    "Recomendación: " + cursor.getString(7)
                            )
                    );

                    document.add(
                            new Paragraph("----------------------------")
                    );

                } while (cursor.moveToNext());
            }

            // Cerrar cursor
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }

            // Finalizar y guardar el PDF
            document.close();
            document = null;

            // Confirmar que el archivo ya está disponible
            ContentValues finalValues = new ContentValues();
            finalValues.put(MediaStore.MediaColumns.IS_PENDING, 0);

            getContentResolver().update(
                    uriArchivo,
                    finalValues,
                    null,
                    null
            );

            Toast.makeText(this,
                    "PDF guardado correctamente en Descargas.",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            // Eliminar archivo incompleto si ocurrió un error
            if (uriArchivo != null) {
                getContentResolver().delete(uriArchivo, null, null);
            }

            Toast.makeText(this,
                    "Error al generar PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (document != null) {
                document.close();
            }
        }
    }
}
