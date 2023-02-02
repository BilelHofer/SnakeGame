package com.example.snakegame;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class GameFragment extends Fragment {

    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // TODO faire que le builder ne se ferme pas si le nom est trop long
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        // Set the title for the dialog
        builder.setTitle("Enter text");

        // Create an EditText widget
        final EditText input = new EditText(requireActivity());

        // Set the EditText as the view for the dialog
        builder.setView(input);

        // Add positive button
        builder.setPositiveButton("Sauvez", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the input text
                String inputText = input.getText().toString();

                if (inputText.length() > 12) {
                    Toast.makeText(requireActivity(), "Le nom ne doit pas dépasser 12 caractères", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO l'ajouter dans la db
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("Abandonner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog
                dialog.dismiss();
            }
        });
        builder.show();
    }
}