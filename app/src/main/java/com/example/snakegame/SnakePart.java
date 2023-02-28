package com.example.snakegame;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Class qui permet de gérer les différentes parties du serpent
 */
public class SnakePart {
    private ArrayList<PointF> lastPositions = new ArrayList<>();
    private int size = 28;
    public SnakePart(float x, float y) {
        lastPositions.add(new PointF(x, y));
    }

    /**
     * Ajoute une position à la liste des positions
     * @param x Coordonnée x
     * @param y Coordonnée y
     */
    public void addPosition(float x, float y) {
        // ajoute une position à la liste à l'index 0
        lastPositions.add(0, new PointF(x, y));

        // supprime la dernière position de la liste si elle est trop longue
        if (lastPositions.size() > size) {
            lastPositions.remove(lastPositions.size() - 1);
        }
    }

    /**
     * Retourne la dernière position de la liste
     * @return Dernière position de la liste
     */
    public PointF getPosition() {
        return lastPositions.get(lastPositions.size()-1);
    }

    /**
     * Modifie la taille des blancs entre les différentes parties du serpent
     * @param size Taille des blancs
     */
    public void setSize(int size) {
        this.size -= size;
        lastPositions.remove(lastPositions.size() - 1);
    }

    /**
     * Retourne la liste des positions en entier
     * @return Liste des positions
     */
    public ArrayList<PointF> getAllPositions() {
        return lastPositions;
    }
}
