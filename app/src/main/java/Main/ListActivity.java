package Main;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener{

    RecyclerView courseRV;
    MyDatabaseHelper myDB;
    ImageView empty_imageview;
    AparcamientoAdapter aparcamientoAdapter;
    TextView no_data;
    MenuItem itemSearch;
    MenuItem itemDelete;
    MenuItem itemAddLocation;

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
        storeDataInArrays();
        courseRV.setAdapter(aparcamientoAdapter);
        courseRV.setLayoutManager(new LinearLayoutManager(ListActivity.this));
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        itemSearch = menu.findItem(R.id.search);
        itemSearch.setOnMenuItemClickListener(this);

        itemDelete = menu.findItem(R.id.delete_all);
        itemDelete.setOnMenuItemClickListener(this);

        itemAddLocation = menu.findItem(R.id.add_location);
        itemAddLocation.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.delete_all:
                confirmDialogDeleteAll();
                break;

            case R.id.search:
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String text) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String text) {
                        aparcamientoAdapter.getFilter().filter(text);
                        return false;
                    }
                });
                break;

            case R.id.add_location:
                confirmDialogAddLocation();
                break;
        }
        return false;
    }

    /**
     * Metodo para la funcionalidad de borrar los aparcamientos
     */
    private void confirmDialogDeleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar todo?");
        //builder.setMessage("¿Estas seguro de que quieres borrar todo?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
            myDB.deleteAllData();
            //Refresh Activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.getInstance().getClass());
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    private void confirmDialogAddLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Añadir ubicacion manualmente?");
        //builder.setMessage("¿Estas seguro que quieres añadir una ubicación de manera manual?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            //TODO
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    /**
     * Metodo para crear los arrays con diferentes valores
     */
    private void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        ArrayList<AparcamientoItem> aparcamientoList = new ArrayList<>();
        aparcamientoAdapter = new AparcamientoAdapter(this, aparcamientoList);
        if (cursor.getCount() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                LocalDateTime startDateTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startDateTime = LocalDateTime.parse(cursor.getString(1));
                }
                AparcamientoItem aparcamientoItem = new AparcamientoItem(calculateTimeDiff(startDateTime), cursor.getString(2), cursor.getDouble(3), cursor.getDouble(4));
                aparcamientoAdapter.aparcamientoList.add(aparcamientoItem);
                aparcamientoAdapter.aparcamientoListFull.add(aparcamientoItem);
            }
        }
    }

    private String calculateTimeDiff(LocalDateTime startDateTime) {
        String result = "";
        LocalDateTime endDateTime = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            endDateTime = LocalDateTime.now();
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

            if (years == 0) {
                if (months == 0) {
                    if (days == 0) {
                        if (hours == 0) {
                            if (minutes == 0) {
                                result = "Hace unos instantes";
                            } else if (minutes == 1) {
                                result = "Hace 1 minuto";
                            } else {
                                result = "Hace " + minutes + " minutos";
                            }
                        } else if (hours == 1) {
                            result = "Hace 1 hora";
                        } else {
                            result = "Hace " + hours + " horas";
                        }
                    } else if (days == 1) {
                        result = "Hace 1 día";
                    } else {
                        result = "Hace " + days + " días";
                    }
                } else if (months == 1) {
                    result = "Hace 1 mes";
                } else {
                    result = "Hace " + months + " meses";
                }
            } else if (years == 1) {
                result = "Hace 1 año";
            } else {
                result = "Hace " + years + " años";
            }
        }
            return result;
    }
}

