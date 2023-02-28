package com.example.snakegame;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

public class ParameterFragment extends Fragment {
    private TextView speedUpValue;
    private Slider speedUpSlider;
    private View contextView;
    private TextView numAppleValue;
    private Slider numAppleSlider;
    private Button saveButton;

    private DatabaseHelperParameters dbParameters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbParameters = new DatabaseHelperParameters(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parameter, container, false);
        contextView = view.findViewById(R.id.parameter_view);

        speedUpValue = view.findViewById(R.id.parameter_speed_up_value);
        speedUpSlider = view.findViewById(R.id.slider_speed_up);

        numAppleValue = view.findViewById(R.id.parameter_num_apple_value);
        numAppleSlider = view.findViewById(R.id.slider_num_apple);

        saveButton = view.findViewById(R.id.btn_save);

        // Mets à jour la valeur du slider et de la textView en fonction de la valeur de la base de données
        Pair<Float, Integer> parameter = dbParameters.getParameter();
        speedUpSlider.setValue(parameter.first);
        speedUpValue.setText(String.valueOf(parameter.first));

        numAppleSlider.setValue(parameter.second);
        numAppleValue.setText(String.valueOf(parameter.second));

        // Désactive le value label du slider
        speedUpSlider.setLabelBehavior(LabelFormatter.LABEL_GONE);
        numAppleSlider.setLabelBehavior(LabelFormatter.LABEL_GONE);

        // Mets à jour la valeur du testview en fonction de la valeur du slider
        numAppleSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                // limite 0 chiffre après la virgule
                numAppleValue.setText(String.format("%.0f", value));
            }
        });
        // Mets à jour la valeur du testview en fonction de la valeur du slider
        speedUpSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                // limite à deux chiffres après la virgule
                speedUpValue.setText(String.format("%.2f", value));
            }
        });

        // Sauvegarde les paramètres dans la base de données
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pour le speedUp limite le nombre de chiffre après la virgule à 2
                boolean sucess = dbParameters.updateParameter(Float.parseFloat((String) speedUpValue.getText()), (int) numAppleSlider.getValue());

                if (sucess) {
                    Snackbar.make(contextView, getString(R.string.parameter_save_message), Snackbar.LENGTH_LONG)
                            .setBackgroundTint(getResources().getColor(R.color.teal_700))
                            .show();
                } else {
                    Snackbar.make(contextView, getString(R.string.parameter_save_error), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }
}