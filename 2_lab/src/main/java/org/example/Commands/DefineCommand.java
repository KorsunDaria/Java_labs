package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.InvalidParametrException;

@CommandName(
        value = "DEFINE",
        argsCount = 2,
        minStackSize = 0
)
public class DefineCommand implements Command{

    @CommandSignature({String.class, double.class})
    public void execute(Context contex, String arg, double number) throws CalculatorException {
        contex.getMap().put(arg,number);
    }

    @CommandSignature({String.class, int.class})
    public void execute(Context contex, String arg, int number) throws CalculatorException {
        contex.getMap().put(arg,(double)number);
    }

    @Override
    public void execute(Context contex, String[] args) {}
}
