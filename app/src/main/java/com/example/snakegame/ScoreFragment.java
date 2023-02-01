package com.example.snakegame;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Random;

public class ScoreFragment extends Fragment {

    private ListView mScoresListView;
    private SimpleCursorAdapter mAdapter;
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

                // Chargez les données depuis la base de données
                loadScores();
            }
        });

        mScoresListView = view.findViewById(R.id.list_scores);

        // Spécifiez les colonnes à afficher dans la liste
        String[] columns = { DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_APPLES};

        // Spécifiez les vues dans lesquelles les données doivent être affichées
        int[] viewIds = { android.R.id.text1, android.R.id.text2 };

        mAdapter = new SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_2, null, columns, viewIds, 0);
        mScoresListView.setAdapter(mAdapter);

        // Chargez les données depuis la base de données
        loadScores();

        return view;
    }

    /**
     * Charge les scores depuis la base de données
     */
    private void loadScores() {
        new LoadScoresTask().execute();
    }

    /**
     * Tâche asynchrone pour charger les scores depuis la base de données
     */
    private class LoadScoresTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return dbHelper.getAllScores();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            mAdapter.swapCursor(cursor);
        }
    }
}