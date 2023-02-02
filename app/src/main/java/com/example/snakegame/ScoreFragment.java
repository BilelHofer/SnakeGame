package com.example.snakegame;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;

public class ScoreFragment extends Fragment {

    private TableLayout tableScores;
    //TODO button temporaire
    private Button ButtonAdd;
    private DatabaseHelper dbHelper;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_score, container, false);

        ButtonAdd = view.findViewById(R.id.btn_add);

        ButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dbHelper.resetDatabase(); // reset de la db

                // Ajout de données
                Random rn = new Random();
                dbHelper.addScore("TEST", rn.nextInt(10) + 1);

            }
        });

        tableScores = view.findViewById(R.id.table_scores);
        loadScores();

        return view;
    }

    /**
     * Charge les scores dans la table
     */
    private void loadScores() {
        Cursor cursor = dbHelper.getAllScores();

        while (cursor.moveToNext()) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            TextView userName = new TextView(getContext());
            userName.setText(cursor.getString(
                    cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
            // ajoute le layout_weight pour que les colonnes prennent la même largeur
            userName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f));
            userName.setGravity(Gravity.CENTER_HORIZONTAL);
            userName.setPadding(0, 0, 0, 40);

            tableRow.addView(userName);

            TextView applesEaten = new TextView(getContext());
            applesEaten.setText(String.valueOf(cursor.getInt(
                    cursor.getColumnIndex(DatabaseHelper.COLUMN_APPLES))));
            // ajoute le layout_weight pour que les colonnes prennent la même largeur
            applesEaten.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f));
            applesEaten.setGravity(Gravity.CENTER_HORIZONTAL);
            applesEaten.setPadding(0, 0, 0, 40);

            tableRow.addView(applesEaten);

            tableScores.addView(tableRow);
        }
        cursor.close();
    }
}