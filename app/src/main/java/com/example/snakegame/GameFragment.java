package com.example.snakegame;

import static android.content.Context.SENSOR_SERVICE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class GameFragment extends Fragment {
    private int score = 0;
    private boolean gameOver = false;
    private enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    private Direction direction = Direction.RIGHT;
    private TextView scoreText;
    private View snakeHead;
    private ImageView food;
    private Handler handler = new Handler();
    private boolean snakeCanChangeDirection = true;
    private RelativeLayout gameArea;
    private SensorEventListener EventListener;
    private Sensor sensor;
    private SensorManager sensorManager;
    private int rangeX;
    private int maxRangeX;
    private int minRangeX;
    private int rangeY;
    private int maxRangeY;
    private int minRangeY;
    private Rect foodRect = new Rect();
    private float deplace = 2f;
    private float speedUp = 0.5f;
    private float sensibility = 1.5f;
    private Rect gameAreaRect = new Rect();

    private ArrayList<SnakePart> snakeParts = new ArrayList<>();
    private ArrayList<ImageView> snakePartsView = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        scoreText = view.findViewById(R.id.score);
        scoreText.setText(String.valueOf(score));

        // Détect la direction à prendre en fonction du capteur GRAVTITY du téléphone
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // Crée le listener
        EventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                // Limite le changement de direction toutes les 200 milliseconds
                if (snakeCanChangeDirection) {
                    handler.postDelayed(runnableChangeDirection, 200);

                    if (event.values[0]*-1 > sensibility && direction != Direction.LEFT) {
                        direction = Direction.RIGHT;
                    } else if (event.values[0]*-1 < -sensibility && direction != Direction.RIGHT) {
                        direction = Direction.LEFT;
                    } else if (event.values[1]*-1 > sensibility && direction != Direction.DOWN) {
                        direction = Direction.UP;
                    } else if (event.values[1]*-1 < -sensibility && direction != Direction.UP) {
                        direction = Direction.DOWN;
                    }
                    snakeCanChangeDirection = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameArea = view.findViewById(R.id.game_area);

        // Utilisation d'un treeObserver pour récupérer les dimensions du jeu une fois charger
        gameArea.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                snakeHead = view.findViewById(R.id.snake_head);
                snakeParts.add(new SnakePart(snakeHead.getX() + snakeHead.getWidth()/2, snakeHead.getY() + snakeHead.getHeight()/2));
                snakePartsView.add((ImageView) snakeHead);

                snakeHead.bringToFront();

                ImageView snakeBody = view.findViewById(R.id.snake_body);
                snakeParts.add(new SnakePart(snakeBody.getX() + snakeBody.getWidth()/2, snakeBody.getY() + snakeBody.getHeight()/2));
                snakePartsView.add(snakeBody);

                food = view.findViewById(R.id.food);
                removeFood();

                gameAreaRect.set((int) 0, (int) 0, (int) gameArea.getWidth(), (int) gameArea.getHeight());

                maxRangeX = (int) gameArea.getWidth() - food.getWidth();
                minRangeX = (int) 0;
                rangeX = maxRangeX - minRangeX;

                maxRangeY = (int) gameArea.getHeight() - food.getHeight();
                minRangeY = (int) 0;
                rangeY = maxRangeY - minRangeY;

                gameArea.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Désactive toutes les actions en cours
        handler.removeCallbacks(runnableMove);
        handler.removeCallbacks(runnableChangeDirection);
        handler.removeCallbacks(runnableSpawnFood);
        handler.removeCallbacks(runnableTestFood);
        handler.removeCallbacks(runnableTestBorder);
        handler.removeCallbacks(runnableTestPart);
        sensorManager.unregisterListener(EventListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Réactive toutes les actions en cours
        handler.postDelayed(runnableMove, 1000);
        handler.postDelayed(runnableSpawnFood, 5000);
        sensorManager.registerListener(EventListener, sensor, 1);
        handler.postDelayed(runnableTestBorder, 1000);
        handler.postDelayed(runnableTestPart, 1000);

        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Déplace la tête du serpent
     */
    private void move() {
        snakeParts.get(0).addPosition(snakeHead.getX() + snakeHead.getWidth()/2, snakeHead.getY() + snakeHead.getHeight()/2);

        switch (direction) {
            case UP:
                snakeHead.setRotation(270);
                snakeHead.setY(snakeHead.getY() - deplace);
                break;
            case DOWN:

                snakeHead.setRotation(90);
                snakeHead.setY(snakeHead.getY() + deplace);
                break;
            case LEFT:
                snakeHead.setRotation(180);
                snakeHead.setX(snakeHead.getX() - deplace);
                break;
            case RIGHT:

                snakeHead.setRotation(0);
                snakeHead.setX(snakeHead.getX() + deplace);
                break;
        }
    }

    /**
     * Déplace les parties du serpent
     */
    private Runnable runnableMove = new Runnable() {
        @Override
        public void run() {
            move();
            moveParts();
            handler.postDelayed(this, 5);
        }
    };

    /**
     * Reset la valeur de snakeCanChangeDirection
     */
    private Runnable runnableChangeDirection = new Runnable() {
        @Override
        public void run() {
            snakeCanChangeDirection = true;
        }
    };

    /**
     * Fait apparaitre la nourriture
     */
    private Runnable runnableSpawnFood = new Runnable() {
        @Override
        public void run() {
            spawnFood();
        }
    };

    /**
     *  Set la position de la nourriture et l'affiche a l'écran
     */
    private void spawnFood() {
        int x = (int) ((Math.random() * rangeX) + minRangeX);
        int y = (int) ((Math.random() * rangeY) + minRangeY);

        food.setX(x);
        food.setY(y);

        foodRect.set((int) food.getX(), (int) food.getY(), (int) food.getX() + food.getWidth(), (int) food.getY() + food.getHeight());

        handler.postDelayed(runnableTestFood, 100);
    }

    /**
     * Test si la tête du serpent touche la nourriture
     */
    private Runnable runnableTestFood = new Runnable() {
        @Override
        public void run() {
            if (foodRect.contains((int) snakeHead.getX() + snakeHead.getWidth()/2, (int) snakeHead.getY() + snakeHead.getHeight()/2)) {
                removeFood();
                score++;
                scoreText.setText(String.valueOf(score));
                addPart();
                updatePart();
                deplace +=  speedUp;
                handler.postDelayed(runnableSpawnFood, 500);
            } else {
                handler.postDelayed(this, 50);
            }
        }
    };

    /**
     * Runnable qui test si la tête du serpent touche une partie du corps
     */
    private Runnable runnableTestPart = new Runnable() {
        @Override
        public void run() {
            for (ImageView part : snakePartsView) {
                if (part != snakeHead && score > 1) {
                    // met un rectangle autour de la part
                    Rect partRect = new Rect((int) part.getX(), (int) part.getY(), (int) part.getX() + part.getWidth(), (int) part.getY() + part.getHeight());

                    if (partRect.contains((int) snakeHead.getX() + snakeHead.getWidth()/2, (int) snakeHead.getY() + snakeHead.getHeight()/2)) {
                        endGame();
                        handler.removeCallbacks(this);
                    }
                }
            }
            handler.postDelayed(this, 50);
        }
    };

    /**
     * Runnable qui test si la tête du serpent sort de gameArea
     */
    private Runnable runnableTestBorder = new Runnable() {
        @Override
        public void run() {
            if (!gameAreaRect.contains((int) snakeHead.getX(), (int) snakeHead.getY()) || !gameAreaRect.contains((int) snakeHead.getX() + snakeHead.getWidth(), (int) snakeHead.getY() + snakeHead.getHeight())) {
                endGame();
                handler.removeCallbacks(this);
            }
            handler.postDelayed(this, 50);
        }
    };

    /**
     * Enleve la nourriture de l'écran
     */
    private void removeFood() {
        food.setX(-100);
        food.setY(-100);
        foodRect.set(-100, -100, -100, -100);
    }

    /**
     * Déplace les parties du serpent
     */
    private void moveParts() {
        int index = 0;
        for (ImageView part : snakePartsView) {
            if (part != snakeHead) {
                snakeParts.get(index).addPosition(part.getX() + part.getWidth()/2, part.getY() + part.getHeight()/2);
                part.setX(snakeParts.get(index - 1).getPosition().x - part.getWidth()/2);
                part.setY(snakeParts.get(index - 1).getPosition().y - part.getHeight()/2);
            }
            index++;
        }
    }

    /**
     * Met à jour la taille des blancs qui sépare les parties du serpent
     */
    private void updatePart() {
        for (SnakePart part : snakeParts) {
            part.setSize(1);
        }
    }

    /**
     * Ajoute une partie au serpent
     */
    private void addPart() {
        ImageView part = new ImageView(getContext());
        part.setImageResource(R.drawable.snake_body);
        part.setLayoutParams(new ViewGroup.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics())
        ));
        part.setX(snakePartsView.get(snakePartsView.size() - 1).getX());
        part.setY(snakePartsView.get(snakePartsView.size() - 1).getY());
        gameArea.addView(part);
        snakePartsView.add(part);
        snakeParts.add(new SnakePart(part.getX(), part.getY()));
        for (int i = 0; i < score; i++) {
            snakeParts.get(snakeParts.size() - 1).addPosition(part.getX() + part.getWidth()/2, part.getY() + part.getHeight()/2);
        }
        snakeParts.get(snakeParts.size() - 1).setSize(score);
        bringToFrontParts();
    }

    /**
     * Remet l'index z des parties du serpent
     */
    private void bringToFrontParts() {
        for (int i = snakePartsView.size() - 1; i >= 0; i--) {
            snakePartsView.get(i).bringToFront();
        }
    }

    /**
     * Termine la partie
     */
    private void endGame() {
        if (!gameOver) {
            gameOver = true;
            // Désactive toutes les actions en cours
            handler.removeCallbacks(runnableMove);
            handler.removeCallbacks(runnableChangeDirection);
            handler.removeCallbacks(runnableSpawnFood);
            handler.removeCallbacks(runnableTestFood);
            handler.removeCallbacks(runnableTestBorder);
            handler.removeCallbacks(runnableTestPart);
            sensorManager.unregisterListener(EventListener);

            // Affiche le dialog de fin de partie
            handler.postDelayed(runnableEndDialog, 1000);
        }
    }

    /**
     * Runnable qui test si la tête du serpent sort de gameArea
     */
    private Runnable runnableEndDialog = new Runnable() {
        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            // Set the title for the dialog
            builder.setTitle("Perdu !");

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

                    ((MainActivity) requireActivity()).updateFragment(new EndFragment(score));
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();

            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

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
    };
}