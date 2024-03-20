package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class EditLocationNameDialog extends AppCompatDialogFragment {
    public EditText nombre;
    private ListActivity listActivity;

    public EditLocationNameDialog(ListActivity listActivity) {
        this.listActivity = listActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(listActivity);

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
                    if(name.equals("")) {
                        Toast.makeText(listActivity, "Por favor pon un nombre", Toast.LENGTH_SHORT).show();
                        listActivity.changeNombreUbicacionDialog();
                    }
                    else {
                        MyDatabaseHelper myDB = new MyDatabaseHelper(listActivity);
                        UbicacionItem ubicacionItem = listActivity.ubicacionAdapter.selectList.get(0);
                        ubicacionItem.setNombre(name);
                        myDB.changeLocationName(name, ubicacionItem.getId());
                        listActivity.ubicacionAdapter.editSelected(ubicacionItem);
                    }
                });
        return builder.create();
    }
}