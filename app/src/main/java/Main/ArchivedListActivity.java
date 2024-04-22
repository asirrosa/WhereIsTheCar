package Main;

import android.app.Activity;
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

public class ArchivedListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    RecyclerView recyclerUbicaciones;
    ImageView empty_imageview;
    ArchivedUbicacionAdapter archivedUbicacionAdapter;
    TextView no_data;
    MenuItem itemSearch, itemAddLocation, itemDeleteSelected, itemUnarchiveSelected, itemEditSelected;
    public Toolbar toolbar;
    public TextView toolbarTitle;
    private int folderId;
    public String folderName;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUbicaciones = findViewById(R.id.recyclerUbicaciones);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        folderName = getIntent().getStringExtra("folderName");
        folderId = getIntent().getIntExtra("folderId",-1);
        initialize();
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_archive_menu, menu);

        itemSearch = menu.findItem(R.id.searchUbicaciones);
        itemSearch.setOnMenuItemClickListener(this);

        /*itemAddLocation = menu.findItem(R.id.add_location);
        itemAddLocation.setOnMenuItemClickListener(this);*/
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.searchUbicaciones:
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String text) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String text) {
                        archivedUbicacionAdapter.getFilter().filter(text);
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
                if (archivedUbicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    confirmDialogDeleteSelected();
                }
                break;

            case R.id.unarchiveSelected:
                if (archivedUbicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    confirmDialogUnarchiveSelected();
                }
                break;
            case R.id.editSelected:
                if (archivedUbicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    changeNombreUbicacionDialog();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Write your code if there's no result
                finish();
                startActivity(getIntent());
            }
        }
    }

    /**
     * METODO IMPORTANTE: Hace que al volver a la pantalla del list activity esta se refresque de nuevo
     */
    @Override
    public void onResume() {
        super.onResume();
        initialize();
        archivedUbicacionAdapter.disableContextualActionMode();
    }

    private void initialize(){
        toolbarTitle.setText(folderName);
        storeArchivedDataInArrays();
        recyclerUbicaciones.setAdapter(archivedUbicacionAdapter);
        recyclerUbicaciones.setLayoutManager(new LinearLayoutManager(ArchivedListActivity.this));


        //para gestionar el back button
        toolbar.setNavigationOnClickListener(v -> {
            if (archivedUbicacionAdapter.isEnable) {
                archivedUbicacionAdapter.disableContextualActionMode();
            } else {
                finish();
            }
        });
    }

    private void unarchiveSelected() {
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        //para guardar las ubicaciones seleccionadas en la página principal
        for (int i = 0; i < archivedUbicacionAdapter.selectList.size(); i++) {
            myDB.addUbicacion(
                    archivedUbicacionAdapter.selectList.get(i).getFechaHora(),
                    archivedUbicacionAdapter.selectList.get(i).getNombre(),
                    archivedUbicacionAdapter.selectList.get(i).getDescripcion(),
                    archivedUbicacionAdapter.selectList.get(i).getLat(),
                    archivedUbicacionAdapter.selectList.get(i).getLon());
        }

        //para borrarlos de la lista del grupo en concreto de los archivados
        String deleteListString = archivedUbicacionAdapter.deleteSelectedFromScreen();
        myDB.deleteSelectedArchivedData(deleteListString);
        Toast.makeText(this, "Se han desarchivado las ubicaciones seleccionadas", Toast.LENGTH_SHORT).show();
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

    private void confirmDialogUnarchiveSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Desarchivar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            unarchiveSelected();
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    public void changeNombreUbicacionDialog(){
        EditArchivedLocationNameDialog editArchivedLocationNameDialog = new EditArchivedLocationNameDialog(this);
        editArchivedLocationNameDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void confirmDialogDeleteSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            String deleteListString = archivedUbicacionAdapter.deleteSelectedFromScreen();
            MyDatabaseHelper myDB = new MyDatabaseHelper(ArchivedListActivity.this);
            myDB.deleteSelectedArchivedData(deleteListString);
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
            intent.putExtra("archiveMode", true);
            intent.putExtra("folderId", folderId);
            intent.putExtra("folderName",folderName);
            startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.create().show();
    }

    private void storeArchivedDataInArrays() {
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        Cursor cursor = myDB.readAllArchivedDataFromCarpeta(folderId);
        ArrayList<UbicacionItem> ubicacionList = new ArrayList<>();
        archivedUbicacionAdapter = new ArchivedUbicacionAdapter(this, ubicacionList, new ArrayList<>());
        if (cursor.getCount() < 1) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            empty_imageview.setVisibility(View.INVISIBLE);
            no_data.setVisibility(View.INVISIBLE);
            while (cursor.moveToNext()) {
                UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0), 0,cursor.getInt(1),cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6));
                archivedUbicacionAdapter.ubicacionList.add(ubicacionItem);
                archivedUbicacionAdapter.ubicacionListFull.add(ubicacionItem);
            }
        }
    }
}

