package Main;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FolderActivity extends AppCompatActivity implements View.OnClickListener,MenuItem.OnMenuItemClickListener{

    RecyclerView recyclerUbicaciones;
    MyDatabaseHelper myDB;
    ImageView empty_imageview;
    FolderAdapter folderAdapter;
    TextView no_data;
    MenuItem itemSearch,itemDeleteSelected;
    public Toolbar toolbar;
    public TextView toolbarTitle;
    private FloatingActionButton btnAñadir;


    /**
     * Metodo onCreate que rellena el layout con los diferentes ubicaciones guardadas
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive_folder_list_layout);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Carpetas archivadas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerUbicaciones = findViewById(R.id.recyclerUbicaciones);
        empty_imageview = findViewById(R.id.empty_imageview);
        no_data = findViewById(R.id.no_data);

        btnAñadir = findViewById(R.id.btnAñadir);
        btnAñadir.setOnClickListener(this);

        myDB = new MyDatabaseHelper(this);
        storeDataInArrays();
        recyclerUbicaciones.setAdapter(folderAdapter);
        recyclerUbicaciones.setLayoutManager(new LinearLayoutManager(this));

        //para gestionar el back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(folderAdapter.isEnable){
                    folderAdapter.disableContextualActionMode();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * Metodo para crear el inflater con el menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_folder_menu, menu);

        itemSearch = menu.findItem(R.id.searchUbicaciones);
        itemSearch.setOnMenuItemClickListener(this);

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAñadir:
                añadirCarpetaDialog();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.searchUbicaciones:
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String text) {
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String text) {
                        folderAdapter.getFilter().filter(text);
                        return false;
                    }
                });
                break;

            case R.id.deleteSelected:
                if (folderAdapter.selectList.size()<1){
                    Toast.makeText(this, "Selecciona al menos uno", Toast.LENGTH_SHORT).show();
                }
                else {
                    confirmDialogDeleteSelected();
                }
                break;

        }
        return true;
    }

    //creo esto para volverlo a llamar cuando haya algun error
    public void añadirCarpetaDialog(){
        AddFolderDialog addFolderDialog = new AddFolderDialog(null,this);
        addFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    private void confirmDialogDeleteSelected(){
        ArrayList<String> deleteList = folderAdapter.getSelectedFoldersName();
        DeleteFolderDialog deleteFolderDialog = new DeleteFolderDialog(this,deleteList);
        deleteFolderDialog.show(getSupportFragmentManager(), "example dialog");
    }

    /**
     * Metodo para crear los arrays con diferentes valores
     */
    private void storeDataInArrays() {
        Cursor cursor = myDB.readAllArchivedFolders();
        ArrayList<UbicacionItem> folderList = new ArrayList<>();
        folderAdapter = new FolderAdapter(this,folderList);
        if (cursor.getCount() == 0) {
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                UbicacionItem ubicacionItem = new UbicacionItem(0,0,null,cursor.getString(0),null,null,null);
                folderAdapter.folderList.add(ubicacionItem);
                folderAdapter.folderListFull.add(ubicacionItem);
            }
        }
    }
}

