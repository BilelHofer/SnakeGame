package com.example.snakegame;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    private Button ButtonPlay;
    private Button ButtonScore;
    private Button ButtonExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialise les boutons
        ButtonPlay = view.findViewById(R.id.btn_play);
        ButtonScore = view.findViewById(R.id.btn_score);
        ButtonExit = view.findViewById(R.id.btn_exit);

        // Ajoute les listeners
        ButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).updateFragment(new GameFragment());
            }
        });
        ButtonScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) requireActivity()).updateFragment(new ScoreFragment());
            }
        });
        ButtonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        return view;
    }
}