package factory.details;

public class Car extends Product {
    private final Body body;
    private final Motor motor;
    private final Accessory accessory;

    public Car(String id, Body body, Motor motor, Accessory accessory) {
        super(id);
        this.body = body;
        this.motor = motor;
        this.accessory = accessory;
    }

    public String getBodyId() {
        return body.getId();
    }

    public String getMotorId() {
        return motor.getId();
    }

    public String getAccessoryId() {
        return accessory.getId();
    }

}
