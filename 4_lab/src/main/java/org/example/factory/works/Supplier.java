package org.example.factory.works;

import org.example.factory.Storage;
import org.example.factory.details.Product;

import java.lang.reflect.Constructor;
import java.security.Provider;

public class Supplier<T extends Product> extends Provider.Service implements Runnable {
    private final Storage<T> storage;
    private final Class<T> detailClass;
    private final Constructor<T> constructor;
}
