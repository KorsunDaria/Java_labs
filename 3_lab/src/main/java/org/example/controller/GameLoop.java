package org.example.controller;

import org.example.model.GameContext;
import org.example.model.GameModel;
import org.example.model.GameState;
import org.example.model.entity.GameObject;
import org.example.util.ShapeUtils;

import java.awt.Shape;
import java.awt.geom.Area;
import java.util.List;

public class GameLoop {
    private final GameModel model;

    public GameLoop(GameModel model) {
        this.model = model;
    }

    public void update(double delta) {
        if (model.getGameState() != GameState.PLAYING) return;

        GameContext context = model.getCurrentContext();
        if (context == null) return;

        for (GameObject obj : context.getAllObjects()) {
            obj.update(delta, context);
        }

        List<GameObject> moved = context.getMovedObjects();
        List<GameObject> all = context.getAllObjects();

        for (GameObject objA : moved) {
            for (GameObject objB : all) {
                if (objA == objB) continue;

                if (checkCollision(objA, objB, context)) {

                    objA.onCollision(objB, context);
                    objB.onCollision(objA, context);
                }
            }
        }

        context.cleanup();
        context.saveNewToOldPositions();
    }

    private boolean checkCollision(GameObject a, GameObject b, GameContext context) {

        Area areaA = new Area(getShape(a, context));
        Area areaB = new Area(getShape(b, context));
        areaA.intersect(areaB);
        return !areaA.isEmpty();
    }

    private Shape getShape(GameObject obj, GameContext context) {
        var pos = context.getNewPosition(obj);

        return ShapeUtils.createShape(
                obj.getShapeType(),
                pos.x(),
                pos.y(),
                obj.getWidth(),
                obj.getHeight()
        );
    }
}