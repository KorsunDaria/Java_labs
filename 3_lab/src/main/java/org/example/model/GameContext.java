package org.example.model;

import org.example.util.Vector2D;
import org.example.model.entity.GameObject;

import java.io.Serializable;
import java.util.*;


public class GameContext implements Serializable {

    private final List<GameObject> allObjects = new ArrayList<>();

    private final Map<GameObject, Vector2D> oldPosition = new HashMap<>();
    private final Map<GameObject, Vector2D> newPosition = new HashMap<>();

    public void addObject(GameObject obj, double x, double y) {
        Vector2D pos = new Vector2D(x, y);

        allObjects.add(obj);
        oldPosition.put(obj, pos);
        newPosition.put(obj, pos);
    }

    public void removeObject(GameObject obj) {
        allObjects.remove(obj);
        oldPosition.remove(obj);
        newPosition.remove(obj);
    }


    public List<GameObject> getAllObjects() {
        return allObjects;
    }

    public Vector2D getNewPosition(GameObject obj) {
        return newPosition.get(obj);
    }

    public Vector2D getOldPosition(GameObject obj) {
        return oldPosition.get(obj);
    }

    public void setNewPosition(GameObject obj, Vector2D pos) {
        newPosition.put(obj, pos);
    }

    public void saveNewToOldPositions() {
        for (GameObject obj : allObjects) {
            oldPosition.put(obj, newPosition.get(obj));
        }
    }

    public void rollback(GameObject obj) {
        newPosition.put(obj, oldPosition.get(obj));
    }

    public void cleanup() {
        Iterator<GameObject> iterator = allObjects.iterator();

        while (iterator.hasNext()) {
            GameObject obj = iterator.next();

            if (obj.isPendingRemoval()) {
                iterator.remove();
                oldPosition.remove(obj);
                newPosition.remove(obj);
            }
        }
    }

    public List<GameObject> getNearby(GameObject source, double radius) {
        List<GameObject> result = new ArrayList<>();

        Vector2D sourcePos = getNewPosition(source);

        for (GameObject obj : allObjects) {
            if (obj == source) continue;

            Vector2D pos = getNewPosition(obj);

            double dx = pos.x() - sourcePos.x();
            double dy = pos.y() - sourcePos.y();

            if (dx * dx + dy * dy <= radius * radius) {
                result.add(obj);
            }
        }

        return result;
    }

    public List<GameObject> getMovedObjects() {
        List<GameObject> moved = new ArrayList<>();
        for (GameObject obj : allObjects) {
            Vector2D oldPos = oldPosition.get(obj);
            Vector2D newPos = newPosition.get(obj);

            if (oldPos != null && newPos != null && !oldPos.equals(newPos)) {
                moved.add(obj);
            }
        }
        return moved;
    }
}
