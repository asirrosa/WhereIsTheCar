package Main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    RecyclerView courseRV;
    MyDatabaseHelper myDB;
    ArrayList<String> ubicacionArray, fechaHoraArray;
    ArrayList<Double> latArray, lonArray;
    AparcamientoAdapter customAdapter;
    ImageView empty_imageview;
    TextView no_data;

    /**
     * Metodo onCreate que rellena el layout con los diferentes aparcamientos guardados
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        courseRV = findViewById(R.id.recyclerView);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        //A PARTIR DE AQUI SIRVE PARA RELLENAR LA LISTA CON LOS APARCAMIENTOS GUARDADOS
        myDB = new MyDatabaseHelper(ListActivity.this);
        ubicacionArray = new ArrayList<>();
        fechaHoraArray = new ArrayList<>();
        latArray = new ArrayList<>();
        lonArray = new ArrayList<>();
        storeDataInArrays();
        customAdapter = new AparcamientoAdapter(this, fechaHoraArray, ubicacionArray, latArray, lonArray);
        courseRV.setAdapter(customAdapter);
        courseRV.setLayoutManager(new LinearLayoutManager(ListActivity.this));

    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    public void deleteAll(MenuItem item) {
        confirmDialog();
    }

    /**
     * Metodo para la funcionalidad de borrar los aparcamientos
     */
    private void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Borrar todo?");
        builder.setMessage("Estas seguro de que quieres borrar todo?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
                myDB.deleteAllData();
                //Refresh Activity
                Intent intent = new Intent(ListActivity.this, MainActivity.getInstance().getClass());
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    /**
     * Metodo para crear los arrays con diferentes valores
     */
    private void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                LocalDateTime startDateTime = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startDateTime = LocalDateTime.parse(cursor.getString(1));
                }
                fechaHoraArray.add(calculateTimeDiff(startDateTime));
                ubicacionArray.add(cursor.getString(2));
                latArray.add(cursor.getDouble(3));
                lonArray.add(cursor.getDouble(4));
            }
        }
    }

    private String calculateTimeDiff(LocalDateTime startDateTime){
        String result = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime endDateTime = LocalDateTime.now();
            LocalDateTime tempDateTime = LocalDateTime.from(startDateTime);

            long years = startDateTime.until(endDateTime, ChronoUnit.YEARS);
            tempDateTime = tempDateTime.plusYears(years);
            long months = tempDateTime.until(endDateTime, ChronoUnit.MONTHS);
            tempDateTime = tempDateTime.plusMonths(months);
            long days = tempDateTime.until(endDateTime, ChronoUnit.DAYS);
            tempDateTime = tempDateTime.plusDays(days);
            long hours = tempDateTime.until(endDateTime, ChronoUnit.HOURS);
            tempDateTime = tempDateTime.plusHours(hours);
            long minutes = tempDateTime.until(endDateTime, ChronoUnit.MINUTES);

            if(years == 0){
                if(months == 0){
                    if(days == 0){
                        if(hours == 0){
                            if(minutes == 0){
                                result = "Hace unos instantes.";
                            }
                            else if (minutes == 1){
                                result = "Hace 1 minuto.";
                            }
                            else{
                                result = "Hace "+minutes+" minutos.";
                            }
                        }
                        else if(hours == 1){
                            result = "Hace 1 hora.";
                        }
                        else{
                            result = "Hace "+hours+" horas.";
                        }
                    }
                    else if(days == 1){
                        result = "Hace 1 día.";
                    }
                    else{
                        result = "Hace "+days+" días.";
                    }
                }
                else if (months == 1){
                    result = "Hace 1 mes.";
                }
                else{
                    result = "Hace "+months+" meses.";
                }
            }
            else if(years == 1){
                result = "Hace 1 año.";
            }
            else {
                result = "Hace "+years+" años.";
            }
        }
        return result;
    }
}
