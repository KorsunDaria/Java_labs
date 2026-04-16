package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.InvalidParametrException;

@CommandName(
        value = "PUSH",
        argsCount = 1,
        minStackSize = 0
)
public class PushCommand implements Command {

    @CommandSignature({String.class})
    public void execute(Context contex, String symbol) throws CalculatorException {
        if (contex.getMap().get(symbol)==null){
            throw new InvalidParametrException("Invalid argument to PUSH");
        }
        double arg = contex.getMap().get(symbol);
        contex.getStack().push(arg);
    }

    @CommandSignature({int.class})
    public void execute(Context contex, int number) throws CalculatorException {
        contex.getStack().push((double)number);
    }

    @CommandSignature({double.class})
    public void execute(Context contex, double number) throws CalculatorException {
        contex.getStack().push(number);
    }

    @Override
    public void execute(Context context, String[] args) {}
}
