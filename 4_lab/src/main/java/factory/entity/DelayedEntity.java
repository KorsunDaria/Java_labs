package factory.entity;

public abstract class DelayedEntity {
    private int delay;

    public DelayedEntity(int delay) {
        this.delay = delay;
    }

    public void setDelay(int delay) {this.delay = delay;}

    public int getDelay() {
        return delay;
    }
}
