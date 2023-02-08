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

    private int score = 0;

    private DatabaseHelper dbHelper;
    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());
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

        endGame();
    }

    private void endGame() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        // Set the title for the dialog
        builder.setTitle("Sauvegarder votre score");

        // Create an EditText widget
        final EditText input = new EditText(requireActivity());
        input.setHint("Votre nom");
        input.setPadding(20, 20, 20, 20);

        builder.setView(input);

        builder.setPositiveButton("Sauvez", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setNegativeButton("Anonmye", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.addScore("Anonyme", score);

                ((MainActivity) requireActivity()).updateFragment(new EndFragment(0));
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String inputText = input.getText().toString();

                if (inputText.length() > 12 || inputText.length() == 0) {
                    Toast.makeText(requireActivity(), "Le nom dois être entre 0 et 12 caractères", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.addScore(inputText, score);

                    ((MainActivity) requireActivity()).updateFragment(new EndFragment(score));
                    dialog.dismiss();
                }
            }
        });
    }
}