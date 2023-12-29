package Main;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener{

    RecyclerView recyclerUbicaciones;
    MyDatabaseHelper myDB;
    ImageView empty_imageview;
    UbicacionAdapter ubicacionAdapter;
    TextView no_data;
    MenuItem itemSearch,itemAddLocation,itemDeleteSelected,itemArchiveSelected,itemArchived, itemUnarchived, itemAddArchivedLocation;
    public Toolbar toolbar;
    public TextView toolbarTitle;
    private boolean archiveMode;
    private String folderName;


    /**
     * Metodo onCreate que rellena el layout con los diferentes ubicaciones guardadas
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original_list_layout);

        archiveMode = getIntent().getBooleanExtra("archiveMode",false);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Ubicaciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUbicaciones = findViewById(R.id.recyclerUbicaciones);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        myDB = new MyDatabaseHelper(this);

        if(archiveMode) {
            folderName = getIntent().getStringExtra("folderName");
            toolbarTitle.setText(folderName);

            storeArchivedDataInArrays();
            recyclerUbicaciones.setAdapter(ubicacionAdapter);
            recyclerUbicaciones.setLayoutManager(new LinearLayoutManager(ListActivity.this));


            //para gestionar el back button
            toolbar.setNavigationOnClickListener(v -> {
                if (ubicacionAdapter.isEnable) {
                    ubicacionAdapter.disableContextualActionMode();
                } else {
                    //para que vuelva al list activity
                    finish();
                }
            });
        }
        else{
            toolbarTitle.setText("Ubicaciones");

            //para rellenar la pantalla
            storeOriginalDataInArrays();
            recyclerUbicaciones.setAdapter(ubicacionAdapter);
            recyclerUbicaciones.setLayoutManager(new LinearLayoutManager(ListActivity.this));

            toolbar.setNavigationOnClickListener(v -> {
                if (ubicacionAdapter.isEnable) {
                    ubicacionAdapter.disableContextualActionMode();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.getInstance().getClass());
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!archiveMode) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.list_menu, menu);

            itemArchived = menu.findItem(R.id.archived);
            itemArchived.setOnMenuItemClickListener(this);
        }
        else{
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.list_archive_menu, menu);

            itemUnarchived = menu.findItem(R.id.unarchived);
            itemUnarchived.setOnMenuItemClickListener(this);
        }

        itemSearch = menu.findItem(R.id.searchUbicaciones);
        itemSearch.setOnMenuItemClickListener(this);

        itemAddLocation = menu.findItem(R.id.add_location);
        itemAddLocation.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.archived:
                Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
                startActivity(intent);
                break;

            case R.id.searchUbicaciones:
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String text) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String text) {
                        ubicacionAdapter.getFilter().filter(text);
                        return false;
                    }
                });
                break;

            case R.id.add_location:
                if(isNetworkAvailable()){
                    confirmDialogAddLocation();
                }
                else{
                    confirmDialogNoInternetNoApiRes();
                }
                break;

            case R.id.deleteSelected:
                if (ubicacionAdapter.selectList.size()<1){
                    Toast.makeText(this, "Selecciona al menos uno", Toast.LENGTH_SHORT).show();
                }
                else {
                    confirmDialogDeleteSelected();
                }
                break;

            case R.id.archiveSelected:
                if (ubicacionAdapter.selectList.size()<1){
                    Toast.makeText(this, "Selecciona al menos uno", Toast.LENGTH_SHORT).show();
                }
                else {
                    confirmDialogArchiveSelected();
                }
                break;

            case R.id.unarchived:
                break;

        }
        return true;
    }

    private void confirmDialogNoInternetNoApiRes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Se necesita conexión a internet para esta funcionalidad");
        builder.setPositiveButton("OK", (dialogInterface, i) -> {});
        builder.create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void confirmDialogArchiveSelected(){
        //todo cambiar esto y que el dialog sea de las distintas carpetas que hay
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Archivar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            String deleteListId = ubicacionAdapter.manageSelected();
            MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
            //myDB.archiveSelected(asf);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    private void confirmDialogDeleteSelected(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            String deleteListId = ubicacionAdapter.manageSelected();
            MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
            if(archiveMode){
                myDB.deleteSelectedArchivedData(deleteListId);
            }
            else {
                myDB.deleteSelectedData(deleteListId);
            }
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
            Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
            intent.putExtra("archiveMode",archiveMode);
            intent.putExtra("folderName",folderName);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    /**
     * Metodo para crear los arrays con diferentes valores
     */
    private void storeOriginalDataInArrays() {
        Cursor cursor = myDB.readAllOriginalData();
        ArrayList<UbicacionItem> ubicacionList = new ArrayList<>();
        ubicacionAdapter = new UbicacionAdapter(this, ubicacionList);
        if (cursor.getCount() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                LocalDateTime startDateTime = LocalDateTime.parse(cursor.getString(1));
                UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0),0,calculateTimeDiff(startDateTime), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getDouble(5));
                ubicacionAdapter.ubicacionList.add(ubicacionItem);
                ubicacionAdapter.ubicacionListFull.add(ubicacionItem);
            }
        }
    }

    private void storeArchivedDataInArrays() {
        Cursor cursor = myDB.readAllArchivedData(folderName);
        ArrayList<UbicacionItem> ubicacionList = new ArrayList<>();
        ubicacionAdapter = new UbicacionAdapter(this, ubicacionList);
        if (cursor.getCount() <= 1) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                LocalDateTime startDateTime = null;
                String hora = null;
                if(cursor.getString(2) != null) {
                    startDateTime = LocalDateTime.parse(cursor.getString(2));
                    hora = calculateTimeDiff(startDateTime);
                }
                UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0),0,hora, cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6));
                ubicacionAdapter.ubicacionList.add(ubicacionItem);
                ubicacionAdapter.ubicacionListFull.add(ubicacionItem);
            }
        }
    }

    private String calculateTimeDiff(LocalDateTime startDateTime) {
        String result = "";
        LocalDateTime endDateTime;
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
                        } else {
                            result = "Hace " + minutes + " min";
                        }
                    } else {
                        result = "Hace " + hours + " h";
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

        return result;
    }
}

