package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private Boolean mIsBarVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Force l'affichage en mode portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Planifie l'exécution du Runnable dans 1 secondes
        mHandler.postDelayed(mRunnableTestBar, 1000);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
    }

    /**
     * Met à jour le fragment affiché
     * @param fragment Le fragment à afficher
     */
    public void updateFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    /**
     * Gère le bouton retour
     * Ferme l'application si le fragment actuel est HomeFragment, sinon affiche le fragment HomeFragment
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        // Si le fragment actuel est HomeFragment, ferme l'application
        if (fragment instanceof HomeFragment) {
            finish();
        } else {
            // Sinon, affiche le fragment HomeFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
        }
    }

    // Runnable qui cache la barre de navigation
    private Runnable mRunnableHideBar = new Runnable() {
        @Override
        public void run() {
            // Cache la barre de navigation
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            mIsBarVisible = true;
        }
    };
    // Runnable qui teste si la barre de navigation est visible
    private Runnable mRunnableTestBar = new Runnable() {
        @Override
        public void run() {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            if((uiOptions & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                if (mIsBarVisible) {
                    // Si elle est visible, Cache la barre de navigation dans 3 secondes
                    mHandler.postDelayed(mRunnableHideBar, 3000);
                    mIsBarVisible = false;
                }
            }
            mHandler.postDelayed(mRunnableTestBar, 500);
        }
    };
}