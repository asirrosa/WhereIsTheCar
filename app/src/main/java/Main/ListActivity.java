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

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    RecyclerView recyclerUbicaciones;
    ImageView empty_imageview;
    UbicacionAdapter ubicacionAdapter;
    TextView no_data;
    MenuItem itemSearch, itemAddLocation, itemDeleteSelected, itemArchiveSelected, itemArchived, itemEditSelected;
    public Toolbar toolbar;
    public TextView toolbarTitle;
    private int LAUNCH_SECOND_ACTIVITY = 1;

    /**
     * Metodo onCreate que rellena el layout con los diferentes ubicaciones guardadas
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original_list_layout);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Ubicaciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUbicaciones = findViewById(R.id.recyclerUbicaciones);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        initialize();
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);

        itemArchived = menu.findItem(R.id.archived);
        itemArchived.setOnMenuItemClickListener(this);

        itemSearch = menu.findItem(R.id.searchUbicaciones);
        itemSearch.setOnMenuItemClickListener(this);

        /*itemAddLocation = menu.findItem(R.id.add_location);
        itemAddLocation.setOnMenuItemClickListener(this);*/
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

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

/*            case R.id.add_location:
                if (isNetworkAvailable()) {
                    confirmDialogAddLocation();
                } else {
                    confirmDialogNoInternetNoApiRes();
                }
                break;*/

            case R.id.deleteSelected:
                if (ubicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    confirmDialogDeleteSelected();
                }
                break;

            case R.id.archiveSelected:
                if (ubicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    elegirCarpetaDialog();
                }
                break;

            case R.id.editSelected:
                if (ubicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    changeNombreUbicacionDialog();
                }
                break;
        }
        return true;
    }

    /**
     * METODO IMPORTANTE: Hace que al volver a la pantalla del list activity esta se refresque de nuevo
     */
    @Override
    public void onResume() {
        super.onResume();
        initialize();
        ubicacionAdapter.disableContextualActionMode();
    }

    private void initialize() {
        toolbarTitle.setText("Ubicaciones");

        //para rellenar la pantalla
        storeOriginalDataInArrays();
        recyclerUbicaciones.setAdapter(ubicacionAdapter);
        recyclerUbicaciones.setLayoutManager(new LinearLayoutManager(ListActivity.this));

        toolbar.setNavigationOnClickListener(v -> {
            if (ubicacionAdapter.isEnable) {
                ubicacionAdapter.disableContextualActionMode();
            } else {
                finish();
            }
        });
    }

    private void confirmDialogNoInternetNoApiRes() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Se necesita conexión a internet para esta funcionalidad");
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
        });
        builder.create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void elegirCarpetaDialog() {
        ChooseFolderDialog chooseFolderDialog = new ChooseFolderDialog(this, ubicacionAdapter.folderList);
        chooseFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    public void changeNombreUbicacionDialog(){
        EditLocationNameDialog editLocationNameDialog = new EditLocationNameDialog(this);
        editLocationNameDialog.show(getSupportFragmentManager(), "example dialog");
    }

    public void añadirCarpetaDialog() {
        AddFolderDialog addFolderDialog = new AddFolderDialog(this, null);
        addFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void confirmDialogDeleteSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            String deleteListString = ubicacionAdapter.deleteSelectedFromScreen();
            MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
            myDB.deleteSelectedData(deleteListString);
            Toast.makeText(this, "Se han borrado las ubicaciones seleccionadas", Toast.LENGTH_SHORT).show();
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
            intent.putExtra("archiveMode", false);
            startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
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
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        Cursor cursor = myDB.readAllOriginalData();
        ArrayList<UbicacionItem> ubicacionList = new ArrayList<>();
        ubicacionAdapter = new UbicacionAdapter(this, ubicacionList, new ArrayList<>());
        if (cursor.getCount() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            empty_imageview.setVisibility(View.INVISIBLE);
            no_data.setVisibility(View.INVISIBLE);
            while (cursor.moveToNext()) {
                UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0), 0, 0,cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getDouble(4),cursor.getDouble(5));
                ubicacionAdapter.ubicacionList.add(ubicacionItem);
                ubicacionAdapter.ubicacionListFull.add(ubicacionItem);
            }
        }

        cursor = myDB.readAllArchivedFoldersData();
        while (cursor.moveToNext()) {
            UbicacionItem ubicacionItem = new UbicacionItem(0,0,cursor.getInt(0),cursor.getString(1),null,null,null,null);
            ubicacionAdapter.folderList.add(ubicacionItem);
        }
    }
}

