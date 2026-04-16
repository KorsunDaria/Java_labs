package org.example;

import org.example.Commands.Command;
import org.example.Commands.CommandSignature;
import org.example.Exception.CalculatorException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class CommandValidator {

    private final Command command;

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS = Map.of(
            int.class, Integer::parseInt,
            double.class, Double::parseDouble,
            String.class, s -> s
    );

    public CommandValidator(Command command) {
        this.command = command;
    }

    public MethodResult validate(String[] args) throws CalculatorException {
        for (Method method : command.getClass().getMethods()) {

            CommandSignature signature = method.getAnnotation(CommandSignature.class);
            if (signature == null) continue;

            Class<?>[] types = signature.value();
            if (types.length != args.length) continue;

            Object[] convertedArgs = tryConvert(types, args);
            if (convertedArgs != null) {
                return new MethodResult(method, convertedArgs);
            }
        }

        throw new CalculatorException("No matching execute method found for given arguments");
    }

    private Object[] tryConvert(Class<?>[] types, String[] args) {
        Object[] result = new Object[args.length];

        for (int i = 0; i < types.length; i++) {
            Object converted = convert(types[i], args[i]);
            if (converted == null && types[i].isPrimitive()) {
                return null;
            }
            result[i] = converted;
        }

        return result;
    }

    private Object convert(Class<?> type, String value) {
        Function<String, Object> converter = CONVERTERS.get(type);
        if (converter == null) return null;

        try {
            return converter.apply(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static class MethodResult {
        public final Method method;
        public final Object[] args;

        public MethodResult(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }
    }
}
