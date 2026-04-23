package factory;

public record FactoryStat(int queueSize,
        int bodiesOnStorage,
        int bodiesProduced,
        int motorsOnStorage,
        int motorsProduced,
        int accessoriesOnStorage,
        int accessoriesProduced,
        int carsOnStorage,
        int carsProduced) {}
