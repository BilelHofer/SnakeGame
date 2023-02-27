package com.example.snakegame;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class EndFragment extends Fragment {

    private int score = 0;

    public EndFragment(int score) {
        this.score = score;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_end, container, false);

        TextView scoreText = view.findViewById(R.id.score);
        scoreText.setText(String.valueOf(score));

        Button restart = view.findViewById(R.id.btn_restart);

        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).updateFragment(new GameFragment());
            }
        });

        Button score = view.findViewById(R.id.btn_score);

        score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).updateFragment(new ScoreFragment());
            }
        });

        return view;
    }
}