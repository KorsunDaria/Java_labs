package utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private static final Map<Class<?>, AtomicInteger> IDCounters = new ConcurrentHashMap<>();

    public static String generateID(Class<?> productClass) {
        ProductType productType = ProductType.getTypeFromClass(productClass);
        IDCounters.putIfAbsent(productClass, new AtomicInteger(0));
        int IDCounter = IDCounters.get(productClass).getAndIncrement();
        return productType.getPrefix() + IDCounter;
    }
}
