package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class EditArchivedLocationNameDialog extends AppCompatDialogFragment {
    public EditText nombre;
    private ArchivedListActivity archivedListActivity ;

    public EditArchivedLocationNameDialog(ArchivedListActivity archivedListActivity) {
        this.archivedListActivity = archivedListActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(archivedListActivity);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_something_dialog, null);

        nombre = view.findViewById(R.id.nombre);

        builder.setView(view)
                .setTitle("Editar ubicación")
                .setNegativeButton("cancelar", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                }).setCancelable(true)
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    String name = nombre.getText().toString();
                    if(name == "") {
                        Toast.makeText(archivedListActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                        archivedListActivity.changeNombreUbicacionDialog();
                    }
                    else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(archivedListActivity);
                        UbicacionItem ubicacionItem = archivedListActivity.archivedUbicacionAdapter.selectList.get(0);
                        ubicacionItem.setNombre(name);
                        myDB.changeArchivedLocationName(name, ubicacionItem.getId());
                        archivedListActivity.archivedUbicacionAdapter.editSelected(ubicacionItem);
                    }
                });
        return builder.create();
    }
}