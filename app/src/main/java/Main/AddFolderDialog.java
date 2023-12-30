package Main;

import android.app.Dialog;
import android.content.Context;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.List;


public class AddFolderDialog extends AppCompatDialogFragment {
    private EditText nombreCarpeta;
    private ListActivity listActivity;
    private FolderActivity folderActivity;


    public AddFolderDialog(ListActivity listActivity, FolderActivity folderActivity) {
        this.listActivity = listActivity;
        this.folderActivity = folderActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_folder_dialog, null);

        nombreCarpeta = view.findViewById(R.id.nombreCarpeta);

        builder.setView(view)
                .setTitle("Añadir carpeta")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .setCancelable(false)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    String nombre = nombreCarpeta.getText().toString().replace(" ", "").replace("\n", "");

                    if (folderActivity != null) {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(folderActivity);
                        boolean existeLaCarpeta = myDB.folderExistsAlready(nombre);

                        if (nombre.equals("")) {
                            Toast.makeText(folderActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                            folderActivity.añadirCarpetaDialog();
                        } else if (existeLaCarpeta) {
                            Toast.makeText(folderActivity, "Ya existe una carpeta con ese nombre, cambia el nombre", Toast.LENGTH_SHORT).show();
                            folderActivity.añadirCarpetaDialog();
                        } else {
                            //se pone en invisible porque vas a meter el elemento
                            if (folderActivity.folderAdapter.folderList.isEmpty()) {
                                folderActivity.empty_imageview.setVisibility(View.INVISIBLE);
                                folderActivity.no_data.setVisibility(View.INVISIBLE);
                            }
                            UbicacionItem ubicacionItem = new UbicacionItem(0, 0, null, nombre, null, null, null);
                            folderActivity.folderAdapter.folderList.add(ubicacionItem);
                            folderActivity.folderAdapter.folderListFull.add(ubicacionItem);
                            folderActivity.folderAdapter.notifyItemInserted(0);
                            folderActivity.folderAdapter.notifyDataSetChanged();
                            myDB.addFolder(nombre);
                            Toast.makeText(folderActivity, "Se ha añadido la carpeta: " + nombre, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(listActivity);
                        boolean existeLaCarpeta = myDB.folderExistsAlready(nombre);

                        if (nombre.equals("")) {
                            Toast.makeText(listActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                            listActivity.añadirCarpetaDialog();
                        } else if (existeLaCarpeta) {
                            Toast.makeText(listActivity, "Ya existe una carpeta con ese nombre, cambia el nombre", Toast.LENGTH_SHORT).show();
                            listActivity.añadirCarpetaDialog();
                        } else {
                            listActivity.ubicacionAdapter.folderList.add(nombre);
                            myDB.addFolder(nombre);
                            dialogInterface.cancel();
                            listActivity.elegirCarpetaDialog();
                            Toast.makeText(listActivity, "Se ha añadido la carpeta: " + nombre, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return builder.create();
    }
}