package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.InvalidParametrException;
import org.example.Exception.StackSizeException;

@CommandName(
        value = "SUB",
        argsCount = 0,
        minStackSize = 2
)
public class SubCommand implements Command {

    @CommandSignature({})
    public void execute(Context contex) throws CalculatorException {
        double a = contex.getStack().pop();
        double b = contex.getStack().pop();

        contex.getStack().push(b-a);

    }

    @Override
    public void execute(Context context, String[] args) {}

}
