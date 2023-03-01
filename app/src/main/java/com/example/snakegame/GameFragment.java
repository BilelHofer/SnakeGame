package com.example.snakegame;

import static android.content.Context.SENSOR_SERVICE;

        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

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
    private float deplace = 2f;
    private float speedUp = 0.5f;
    private float sensibility = 1.5f;
    private Rect gameAreaRect = new Rect();

    private ArrayList<SnakePart> snakeParts = new ArrayList<>();
    private ArrayList<ImageView> snakePartsView = new ArrayList<>();
    private int foodNumber = 1;
    private int foodLeft = 1;
    private PointF snakeHeadPoint = new PointF();
    private Rect snakeHeadRect = new Rect();
    private ArrayList<Food> foodPart = new ArrayList<>();
    private ArrayList<ImageView> foodPartView = new ArrayList<>();

    private DatabaseHelper dbHelper;
    private DatabaseHelperParameters dbHelperParameters;

    private ArrayList<PointF> snakePartsPoint = new ArrayList<>();
    public GameFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelperParameters = new DatabaseHelperParameters(getContext());
        speedUp = dbHelperParameters.getParameter().first;
        foodNumber = dbHelperParameters.getParameter().second;
        foodLeft = foodNumber;

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

                snakeHeadPoint.set((int) snakeHead.getX() + snakeHead.getWidth()/2, (int) snakeHead.getY() + snakeHead.getHeight()/2);

                addFood();
                snakeHead.bringToFront();

                ImageView snakeBody = view.findViewById(R.id.snake_body);
                snakeParts.add(new SnakePart(snakeBody.getX() + snakeBody.getWidth()/2, snakeBody.getY() + snakeBody.getHeight()/2));
                snakePartsView.add(snakeBody);

                gameAreaRect.set((int) 0, (int) 0, (int) gameArea.getWidth(), (int) gameArea.getHeight());

                maxRangeX = (int) gameArea.getWidth() - 100;
                minRangeX = (int) 0;
                rangeX = maxRangeX - minRangeX;

                maxRangeY = (int) gameArea.getHeight() - 100;
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
    private void moveHead() {
        snakeParts.get(0).addPosition(snakeHead.getX() + snakeHead.getWidth()/2, snakeHead.getY() + snakeHead.getHeight()/2);
        snakeHeadPoint.set((int) snakeHead.getX() + snakeHead.getWidth()/2, (int) snakeHead.getY() + snakeHead.getHeight()/2);
        snakeHeadRect = new Rect((int) snakeHead.getX(), (int) snakeHead.getY(), (int) snakeHead.getX() + snakeHead.getWidth(), (int) snakeHead.getY() + snakeHead.getHeight());

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
            moveHead();
            moveParts();
            getAllDeadPos();
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
        for (int i = 0; i < foodNumber; i++) {
            int x = 0;
            int y = 0;

            boolean isFoodOnSnake = false;
            boolean isFoodOnFood = false;

            do {
                x = (int) ((Math.random() * rangeX) + minRangeX);
                y = (int) ((Math.random() * rangeY) + minRangeY);

                // pour éviter que la nourriture apparaisse sur le serpent
                for (int j = 0; j < snakePartsPoint.size() - 1; j++) {
                    Rect tempFoodRect = new Rect(x, y, x + foodPartView.get(i).getWidth(), y + foodPartView.get(i).getHeight());
                    if (tempFoodRect.contains((int) snakePartsPoint.get(j).x, (int) snakePartsPoint.get(j).y)) {
                        isFoodOnSnake = true;
                    }
                }

                // pour éviter que la nourriture apparaisse sur une autre nourriture
                for (int j = 0; j < foodPart.size(); j++) {
                    if (foodPart.get(j).getRect().contains(x, y)) {
                        isFoodOnFood = true;
                    }
                }

            }while (isFoodOnSnake || isFoodOnFood);

            foodPart.get(i).setPos(x, y);
            foodPartView.get(i).setX(x);
            foodPartView.get(i).setY(y);

            handler.postDelayed(runnableTestFood, 10);
        }

        foodLeft = foodNumber;
    }

    /**
     * Test si la tête du serpent touche la nourriture
     */
    private Runnable runnableTestFood = new Runnable() {
        @Override
        public void run() {
            boolean noFood = false;
            for (int i = 0; i < foodNumber; i++) {
                if (foodPart.get(i).getRect().contains((int) snakeHeadPoint.x, (int) snakeHeadPoint.y)) {
                    removeFood(i);
                    upgradeGame();
                    foodLeft--;
                    if (foodLeft == 0) {
                        noFood = true;
                        handler.postDelayed(runnableSpawnFood, 500);
                    }
                }
            }
            if (!noFood)
                handler.postDelayed(this, 50);
        }
    };

    /**
     * Améliore les stats de la partie
     */
    private void upgradeGame() {
        score++;
        scoreText.setText(String.valueOf(score));
        addPart();
        updatePart();
        deplace += speedUp;
    }

    /**
     * Runnable qui test si la tête du serpent touche une partie du corps
     */
    private Runnable runnableTestPart = new Runnable() {
        @Override
        public void run() {
            for (PointF pos : snakePartsPoint) {
                if (snakeHeadRect.contains((int) pos.x, (int) pos.y)) {
                        endGame();
                        handler.removeCallbacks(this);
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
    private void removeFood(int i) {
        foodPart.get(i).setPos(-100, -100);
        foodPartView.get(i).setX(-100);
        foodPartView.get(i).setY(-100);
    }

    /**
     *  Crée toutes les nourritures
     */
    private void addFood(){
        for (int i = 0; i < foodNumber; i++) {

            ImageView part = new ImageView(getContext());
            part.setImageResource(R.drawable.apple);
            part.setLayoutParams(new ViewGroup.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics())
            ));


            foodPart.add(new Food(-100, -100));
            part.setX(-100);
            part.setY(-100);
            gameArea.addView(part);
            foodPartView.add(part);
        }
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
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());

            builder.setTitle(getString(R.string.end_game_title));

            // Create an EditText widget
            final EditText input = new EditText(requireActivity());
            input.setHint(getString(R.string.end_game_hint));
            input.setPadding(20, 20, 20, 20);

            builder.setView(input);

            builder.setPositiveButton(getString(R.string.parameter_btn_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });

            builder.setNegativeButton(getString(R.string.end_game_guest), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.addScore("Guest", score);

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
                        Toast.makeText(requireActivity(), getString(R.string.save_game_error), Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.addScore(inputText, score);

                        ((MainActivity) requireActivity()).updateFragment(new EndFragment(score));
                        dialog.dismiss();
                    }
                }
            });
        }
    };

    /**
     * Récupère tous les points qui tue le serpent
     */
    private void getAllDeadPos() {
        if (score > 2) {
            snakePartsPoint.clear();
            for (int i = 2; i < snakeParts.size() - 1; i++) {
                snakePartsPoint.addAll(snakeParts.get(i).getAllPositions());
            }
        }
    }
}