package org.example;

import org.example.Commands.Command;
import org.example.Commands.CommandName;
import org.example.Exception.CalculatorException;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Calculator {

    private static final Logger logger = LogManager.getLogger(Calculator.class);


    private final Context context = new Context();
    private final CommandFactory factory;

    public Calculator(CommandFactory factory) {
        this.factory = factory;
    }

    public List<ExecutionResult> run(List<String[]> commandLines) throws Exception{
        List<ExecutionResult> results = new ArrayList<>();

        for (String[] line : commandLines) {
            try{
                results.add(executeLine(line));
            } catch(Exception e) {
                logger.warn("Invalid Command " + line, e);
                throw e;
            }
        }

        return results;
    }

    private ExecutionResult executeLine(String[] args) throws Exception {
        String originalInput = String.join(" ", args);

        if (args.length == 0) {
            logger.warn("Empty command received.");
            return new ExecutionResult(originalInput, "Empty command");
        }

        String commandName = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        logger.info("Executing command: " + commandName + " with args: " + Arrays.toString(commandArgs));
        CommandName meta = factory.getMeta(commandName);
        if (meta.minStackSize()>context.getStack().size()){
            return new ExecutionResult(originalInput, "To less arg in stack");
        }
        Command command = factory.create(commandName);
        if (command == null) {
            logger.warn("Unknown command: " + commandName);
            return new ExecutionResult(originalInput, "Unknown command");
        }

        try {
            CommandValidator validator = new CommandValidator(command);
            CommandValidator.MethodResult result = validator.validate(commandArgs);

            invoke(command, result);

            logger.info("Command executed successfully: " + commandName);
            return new ExecutionResult(originalInput, null);

        } catch (CalculatorException e) {
            logger.warn("CalculatorException in command '" + commandName + "': " + e.getMessage());
            return new ExecutionResult(originalInput, e.getMessage());
        } catch (Exception e) {
            logger.error("Exception in command '" + commandName + "': " + e.getMessage());
            return new ExecutionResult(originalInput, e.getMessage());
        }
    }

    private void invoke(Command command, CommandValidator.MethodResult result) throws Exception {
        Method method = result.method;

        Object[] invokeArgs = new Object[result.args.length + 1];
        invokeArgs[0] = context;
        System.arraycopy(result.args, 0, invokeArgs, 1, result.args.length);

        method.invoke(command, invokeArgs);
    }

    public Context getContext() {
        return context;
    }
}
