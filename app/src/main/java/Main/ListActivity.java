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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    RecyclerView courseRV;
    Button add_button;

    MyDatabaseHelper myDB;
    ArrayList<String> ubicacionArray, fechaHoraArray;

    ArrayList<Double> latArray, lonArray;
    CustomAdapter customAdapter;
    ImageView empty_imageview;
    TextView no_data;

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
        customAdapter = new CustomAdapter(this, fechaHoraArray, ubicacionArray, latArray, lonArray);
        courseRV.setAdapter(customAdapter);
        courseRV.setLayoutManager(new LinearLayoutManager(ListActivity.this));

    }

    //Creamos un inflater
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    public void deleteAll(MenuItem item) {
        confirmDialog();
    }

    //El dialog para borrarlo
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

    private void storeDataInArrays(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            empty_imageview.setVisibility(View.VISIBLE);
            no_data.setVisibility(View.VISIBLE);
        }else{
            while (cursor.moveToNext()){
                fechaHoraArray.add(cursor.getString(1));
                ubicacionArray.add(cursor.getString(2));
                latArray.add(cursor.getDouble(3));
                lonArray.add(cursor.getDouble(4));
            }
        }
    }
}
