package org.example.model.level;

import org.example.model.GameContext;

public abstract class Level {

    protected final int id;
    protected final LevelConfig config;
    protected final GameContext context;

    public Level(int id, GameContext context) {
        this.id = id;
        this.context = context;
        this.config = createConfig();
    }

    protected abstract LevelConfig createConfig();

    public int getId() {
        return id;
    }

    public LevelConfig getConfig() {
        return config;
    }

    public GameContext getContext() {
        return context;
    }

    public abstract boolean isCompleted();
}
