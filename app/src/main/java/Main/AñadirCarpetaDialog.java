package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class AñadirCarpetaDialog extends AppCompatDialogFragment{
    private EditText nombreCarpeta;
    private FolderActivity folderActivity;

    public AñadirCarpetaDialog(FolderActivity folderActivity){
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
                    String nombre = nombreCarpeta.getText().toString().replace(" ", "").replace("\n","");

                    MyDatabaseHelper myDB = new MyDatabaseHelper(folderActivity);
                    boolean existeLaCarpeta = myDB.folderExistsAlready(nombre);

                    if(nombre.equals("")){
                        Toast.makeText(folderActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                        folderActivity.añadirCarpetaDialog();
                    }
                    else if(existeLaCarpeta){
                        Toast.makeText(folderActivity, "Ya existe una carpeta con ese nombre, cambia el nombre", Toast.LENGTH_SHORT).show();
                        folderActivity.añadirCarpetaDialog();
                    }
                    else{
                        //se pone en invisible porque vas a meter el elemento
                        if(folderActivity.folderAdapter.folderList.isEmpty()) {
                            folderActivity.empty_imageview.setVisibility(View.INVISIBLE);
                            folderActivity.no_data.setVisibility(View.INVISIBLE);
                        }
                        UbicacionItem ubicacionItem = new UbicacionItem(0,0,null,nombre,null,null,null);
                        folderActivity.folderAdapter.folderList.add(ubicacionItem);
                        folderActivity.folderAdapter.folderListFull.add(ubicacionItem);
                        folderActivity.folderAdapter.notifyItemInserted(0);
                        folderActivity.folderAdapter.notifyDataSetChanged();
                        myDB.addFolder(nombre);
                    }
                });
        return builder.create();
    }
}