package org.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Context implements Serializable {


    private final Stack<Double> stack = new Stack<>();
    private final Map<String, Double> parameters = new HashMap<>();

    public Stack<Double> getStack() {
        return stack;
    }

    public Map<String, Double> getMap() {
        return parameters;
    }

    public void updateFrom(Context other) {
        this.stack.clear();
        this.stack.addAll(other.getStack());
        this.parameters.clear();
        this.parameters.putAll(other.getMap());
    }
}