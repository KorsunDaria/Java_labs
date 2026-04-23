package utilities;

import factory.details.Accessory;
import factory.details.Body;
import factory.details.Car;
import factory.details.Motor;

public enum ProductType {
    ACCESSORY(Accessory.class, "ACS"),
    BODY(Body.class, "BDY"),
    CAR(Car.class, "CAR"),
    MOTOR(Motor.class, "MTR");

    private final Class<?> productClass;
    private final String prefix;

    ProductType(Class<?> productClass, String prefix) {
        this.productClass = productClass;
        this.prefix = prefix;
    }

    public static ProductType getTypeFromClass(Class<?> productClass) {
        for (ProductType type : values()) {
            if (type.productClass.equals(productClass)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + productClass.getSimpleName());
    }

    public String getPrefix() {
        return prefix;
    }
}