package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


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
        View view = inflater.inflate(R.layout.add_something_dialog, null);

        nombreCarpeta = view.findViewById(R.id.nombre);

        builder.setView(view)
                .setTitle("Añadir carpeta")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    if(listActivity != null){
                        listActivity.ubicacionAdapter.disableContextualActionMode();
                    }
                    dialogInterface.cancel();
                })
                .setCancelable(false)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    String nombre = nombreCarpeta.getText().toString().replaceAll("\\s+", " ").replaceAll("\n", "");

                    if (folderActivity != null) {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(folderActivity);
                        if (nombre.equals("")) {
                            Toast.makeText(folderActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                            folderActivity.añadirCarpetaDialog();
                        } else {
                            //se pone en invisible porque vas a meter el elemento
                            if (folderActivity.folderAdapter.folderList.isEmpty()) {
                                folderActivity.empty_imageview.setVisibility(View.INVISIBLE);
                                folderActivity.no_data.setVisibility(View.INVISIBLE);
                            }
                            UbicacionItem ubicacionItem = new UbicacionItem(0, 0,0, nombre, null, null, null, null);
                            folderActivity.folderAdapter.folderList.add(ubicacionItem);
                            folderActivity.folderAdapter.folderListFull.add(ubicacionItem);
                            folderActivity.folderAdapter.notifyItemInserted(0);
                            folderActivity.folderAdapter.notifyDataSetChanged();
                            myDB.addFolder(nombre);
                        }
                    } else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(listActivity);
                        if (nombre.equals("")) {
                            Toast.makeText(listActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                            listActivity.añadirCarpetaDialog();
                        } else {
                            UbicacionItem ubicacionItem = new UbicacionItem(0,0,0,nombre,null,null,null,null);
                            listActivity.ubicacionAdapter.folderList.add(ubicacionItem);
                            myDB.addFolder(nombre);
                            dialogInterface.cancel();
                            listActivity.elegirCarpetaDialog();
                        }
                    }
                });
        return builder.create();
    }
}