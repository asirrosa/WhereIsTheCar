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

public class ListActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    RecyclerView recyclerUbicaciones;
    MyDatabaseHelper myDB;
    ImageView empty_imageview;
    UbicacionAdapter ubicacionAdapter;
    TextView no_data;
    MenuItem itemSearch, itemAddLocation, itemDeleteSelected, itemArchiveSelected, itemArchived, itemUnarchived, itemUnarchiveSelected;
    public Toolbar toolbar;
    public TextView toolbarTitle;
    public boolean archiveMode;
    private String folderName;


    /**
     * Metodo onCreate que rellena el layout con los diferentes ubicaciones guardadas
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.original_list_layout);

        archiveMode = getIntent().getBooleanExtra("archiveMode", false);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Ubicaciones");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUbicaciones = findViewById(R.id.recyclerUbicaciones);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        myDB = new MyDatabaseHelper(this);

        if (archiveMode) {
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
        } else {
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
        if (!archiveMode) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.list_menu, menu);

            itemArchived = menu.findItem(R.id.archived);
            itemArchived.setOnMenuItemClickListener(this);
        } else {
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

            case R.id.add_location:
                if (isNetworkAvailable()) {
                    confirmDialogAddLocation();
                } else {
                    confirmDialogNoInternetNoApiRes();
                }
                break;

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

            case R.id.unarchived:
                intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.unarchiveSelected:
                if (ubicacionAdapter.selectList.size() < 1) {
                    Toast.makeText(this, "Selecciona al menos una ubicacion", Toast.LENGTH_SHORT).show();
                } else {
                    confirmDialogUnarchiveSelected();
                }
                break;
        }
        return true;
    }

    private void unarchiveSelected() {
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        //para guardar las ubicaciones seleccionadas en la página principal
        for(int i = 0;i<ubicacionAdapter.selectList.size();i++){
            myDB.addUbicacion(
                    ubicacionAdapter.selectList.get(i).getFechaHora(),
                    ubicacionAdapter.selectList.get(i).getNombre(),
                    ubicacionAdapter.selectList.get(i).getDescripcion(),
                    ubicacionAdapter.selectList.get(i).getLat(),
                    ubicacionAdapter.selectList.get(i).getLon());
        }

        //para borrarlos de la lista del grupo en concreto de los archivados
        String deleteListString = ubicacionAdapter.deleteSelectedFromScreen();
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

    public void elegirCarpetaDialog(){
        ArrayList<String> folderList = getSelectedFoldersName();
        ChooseFolderDialog chooseFolderDialog = new ChooseFolderDialog(this, folderList);
        chooseFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    public void añadirCarpetaDialog(){
        AddFolderDialog addFolderDialog = new AddFolderDialog(this,null);
        addFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private ArrayList<String> getSelectedFoldersName() {
        ArrayList<String> folderList = new ArrayList<>();
        MyDatabaseHelper myDB = new MyDatabaseHelper(this);
        Cursor cursor = myDB.readAllArchivedFolders();
        while (cursor.moveToNext()) {
            folderList.add(cursor.getString(0));
        }
        return folderList;
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

    private void confirmDialogDeleteSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Borrar seleccionados?");
        builder.setPositiveButton("Si", (dialogInterface, i) -> {
            String deleteListString = ubicacionAdapter.deleteSelectedFromScreen();
            MyDatabaseHelper myDB = new MyDatabaseHelper(ListActivity.this);
            if (archiveMode) {
                myDB.deleteSelectedArchivedData(deleteListString);
            } else {
                myDB.deleteSelectedData(deleteListString);
            }
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
            intent.putExtra("archiveMode", archiveMode);
            intent.putExtra("folderName", folderName);
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
        ubicacionAdapter = new UbicacionAdapter(this, ubicacionList, new ArrayList<>());
        if (cursor.getCount() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0), 0, cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getDouble(5));
                ubicacionAdapter.ubicacionList.add(ubicacionItem);
                ubicacionAdapter.ubicacionListFull.add(ubicacionItem);
            }
        }

        cursor = myDB.readAllArchivedFolders();
        while (cursor.moveToNext()) {
            ubicacionAdapter.folderList.add(cursor.getString(0));
        }
    }

    private void storeArchivedDataInArrays() {
        Cursor cursor = myDB.readAllArchivedData(folderName);
        ArrayList<UbicacionItem> ubicacionList = new ArrayList<>();
        ubicacionAdapter = new UbicacionAdapter(this, ubicacionList, new ArrayList<>());
        if (cursor.getCount() <= 1) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                //miramos si el nombre = null si es asi esque es una row para identificar carpetas
                if (!(cursor.getString(3) == null)) {
                    UbicacionItem ubicacionItem = new UbicacionItem(cursor.getInt(0), 0, cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6));
                    ubicacionAdapter.ubicacionList.add(ubicacionItem);
                    ubicacionAdapter.ubicacionListFull.add(ubicacionItem);
                }
            }
        }
    }
}

