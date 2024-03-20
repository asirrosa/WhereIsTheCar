package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class EditFolderNameDialog extends AppCompatDialogFragment {
    public EditText nombre;
    private FolderActivity folderActivity;

    public EditFolderNameDialog(FolderActivity folderActivity) {
        this.folderActivity = folderActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(folderActivity);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_something_dialog, null);
        nombre = view.findViewById(R.id.nombre);

        builder.setView(view)
                .setTitle("Editar carpeta")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                }).setCancelable(true)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    String name = nombre.getText().toString();
                    if(name.equals("")) {
                        Toast.makeText(folderActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                        folderActivity.changeNombreUbicacionDialog();
                    }
                    else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(folderActivity);
                        UbicacionItem ubicacionItem = folderActivity.folderAdapter.selectList.get(0);
                        ubicacionItem.setNombre(name);
                        myDB.changeFolderName(name,ubicacionItem.getFolderId());
                        folderActivity.folderAdapter.editSelected(ubicacionItem);
                    }
                });
        return builder.create();
    }
}