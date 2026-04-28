package factory.details;

import java.io.Serializable;

public abstract class Product implements Serializable {
    private final String id;

    public Product(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}