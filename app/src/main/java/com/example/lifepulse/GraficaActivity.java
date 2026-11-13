package com.example.lifepulse;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class GraficaActivity extends AppCompatActivity {

    private LineChart lineChart;
    private DBHelper db;
    private SesionManager sesion;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafica);

        sesion = new SesionManager(this);

        if (!sesion.haySesionActiva()) {
            finish();
            return;
        }

        idUsuario = sesion.obtenerIdUsuario();

        lineChart = findViewById(R.id.lineChart);
        db = new DBHelper(this);

        if (lineChart == null) {
            Toast.makeText(this,
                    "No se encontró el componente de la gráfica",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cargarGrafica();
    }

    private void cargarGrafica() {

        Cursor cursor = db.obtenerTodos(idUsuario);

        ArrayList<Entry> sistolicaEntries = new ArrayList<>();
        ArrayList<Entry> diastolicaEntries = new ArrayList<>();

        try {

            if (cursor != null && cursor.moveToLast()) {

                int index = 0;

                int columnaSistolica =
                        cursor.getColumnIndexOrThrow("sistolica");

                int columnaDiastolica =
                        cursor.getColumnIndexOrThrow("diastolica");

                // Recorrer desde el registro más antiguo
                // hasta el más reciente.
                do {

                    int sistolica =
                            cursor.getInt(columnaSistolica);

                    int diastolica =
                            cursor.getInt(columnaDiastolica);

                    sistolicaEntries.add(
                            new Entry(index, sistolica)
                    );

                    diastolicaEntries.add(
                            new Entry(index, diastolica)
                    );

                    index++;

                } while (cursor.moveToPrevious());
            }

        } catch (Exception e) {

            Toast.makeText(this,
                    "Error al cargar los registros: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();

            return;

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        if (sistolicaEntries.isEmpty()) {

            lineChart.clear();

            Description description = new Description();
            description.setText(
                    "Aún no hay mediciones para mostrar"
            );

            lineChart.setDescription(description);
            lineChart.invalidate();

            Toast.makeText(this,
                    "Registra una medición para generar la gráfica",
                    Toast.LENGTH_LONG).show();

            return;
        }

        LineDataSet sistolicaDataSet = new LineDataSet(
                sistolicaEntries,
                "Sistólica"
        );

        sistolicaDataSet.setColor(Color.BLUE);
        sistolicaDataSet.setCircleColor(Color.BLUE);
        sistolicaDataSet.setLineWidth(3f);
        sistolicaDataSet.setCircleRadius(5f);
        sistolicaDataSet.setValueTextSize(12f);

        LineDataSet diastolicaDataSet = new LineDataSet(
                diastolicaEntries,
                "Diastólica"
        );

        diastolicaDataSet.setColor(Color.RED);
        diastolicaDataSet.setCircleColor(Color.RED);
        diastolicaDataSet.setLineWidth(3f);
        diastolicaDataSet.setCircleRadius(5f);
        diastolicaDataSet.setValueTextSize(12f);

        LineData lineData = new LineData(
                sistolicaDataSet,
                diastolicaDataSet
        );

        lineChart.setData(lineData);

        Description description = new Description();
        description.setText("Historial de presión arterial");

        lineChart.setDescription(description);

        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setGranularity(1f);

        lineChart.animateX(1500);
        lineChart.invalidate();
    }
}
