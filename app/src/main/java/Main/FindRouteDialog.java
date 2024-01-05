package Main;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class FindRouteDialog extends AppCompatDialogFragment {

    private CheckBox checkBoxPeaje, checkBoxAutopista, checkBoxFerri;
    private RadioButton btnWalking, btnDriving, btnCycling;
    private FindRouteActivity findRouteActivity;

    public FindRouteDialog(FindRouteActivity findRouteActivity) {
        this.findRouteActivity = findRouteActivity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.find_route_dialog, null);

        checkBoxPeaje = view.findViewById(R.id.checkBoxPeaje);
        checkBoxAutopista = view.findViewById(R.id.checkBoxAutopista);
        checkBoxFerri = view.findViewById(R.id.checkBoxFerri);

        btnDriving = view.findViewById(R.id.btnDriving);
        btnWalking = view.findViewById(R.id.btnWalking);
        btnCycling = view.findViewById(R.id.btnCycling);

        if (findRouteActivity.exclude == null && findRouteActivity.transporte == null) {
            //para que el modo default sea el de driving
            btnDriving.setChecked(true);
        } else {
            //checkbox
            if (findRouteActivity.exclude[0] != null) {
                checkBoxPeaje.setChecked(true);
            }
            if (findRouteActivity.exclude[1] != null) {
                checkBoxAutopista.setChecked(true);
            }
            if (findRouteActivity.exclude[2] != null) {
                checkBoxFerri.setChecked(true);
            }
            //radio button
            if (findRouteActivity.transporte.equals("walking")) {
                btnWalking.setChecked(true);
                btnDriving.setChecked(false);
                btnCycling.setChecked(false);
                checkBoxPeaje.setEnabled(false);
                checkBoxAutopista.setEnabled(false);
                checkBoxFerri.setEnabled(false);
            } else if(findRouteActivity.transporte.equals("driving")) {
                btnWalking.setChecked(false);
                btnDriving.setChecked(true);
                btnCycling.setChecked(false);
            }
            else if(findRouteActivity.transporte.equals("cycling")) {
                btnWalking.setChecked(false);
                btnDriving.setChecked(false);
                btnCycling.setChecked(true);
                checkBoxPeaje.setEnabled(false);
                checkBoxAutopista.setEnabled(false);
            }
        }

        btnWalking.setOnClickListener(v -> {
            btnWalking.setChecked(true);
            btnDriving.setChecked(false);
            btnCycling.setChecked(false);
            findRouteActivity.transporte = "walking";
            //
            checkBoxPeaje.setEnabled(false);
            checkBoxAutopista.setEnabled(false);
            checkBoxFerri.setEnabled(false);
            //
            checkBoxPeaje.setChecked(false);
            checkBoxAutopista.setChecked(false);
            checkBoxFerri.setChecked(false);
        });

        btnDriving.setOnClickListener(v -> {
            btnWalking.setChecked(false);
            btnDriving.setChecked(true);
            btnCycling.setChecked(false);
            findRouteActivity.transporte = "driving";

            checkBoxPeaje.setEnabled(true);
            checkBoxAutopista.setEnabled(true);
            checkBoxFerri.setEnabled(true);

            checkBoxPeaje.setChecked(false);
            checkBoxAutopista.setChecked(false);
            checkBoxFerri.setChecked(false);
        });

        btnCycling.setOnClickListener(v -> {
            btnWalking.setChecked(false);
            btnDriving.setChecked(false);
            btnCycling.setChecked(true);
            findRouteActivity.transporte = "cycling";

            checkBoxPeaje.setEnabled(false);
            checkBoxAutopista.setEnabled(false);
            checkBoxFerri.setEnabled(true);

            checkBoxPeaje.setChecked(false);
            checkBoxAutopista.setChecked(false);
            checkBoxFerri.setChecked(false);
        });

        builder.setView(view)
                .setTitle("Opciones de ruta")
                .setPositiveButton("ok", (dialogInterface, i) -> {
                    if (checkBoxPeaje.isChecked()) {
                        findRouteActivity.exclude[0] = "toll";
                    } else {
                        findRouteActivity.exclude[0] = null;
                    }
                    if (checkBoxAutopista.isChecked()) {
                        findRouteActivity.exclude[1] = "motorway";
                    } else {
                        findRouteActivity.exclude[1] = null;
                    }
                    if (checkBoxFerri.isChecked()) {
                        findRouteActivity.exclude[2] = "ferry";
                    } else {
                        findRouteActivity.exclude[2] = null;
                    }

                    if (findRouteActivity.currentRoute != null) {
                        findRouteActivity.getRoutes();
                    }
                });
        return builder.create();
    }
}