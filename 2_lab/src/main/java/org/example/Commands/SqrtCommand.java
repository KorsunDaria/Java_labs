package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.InvalidParametrException;
import org.example.Exception.StackSizeException;

import static java.lang.Math.sqrt;

@CommandName(
        value = "SQRT",
        argsCount = 0,
        minStackSize = 1
)
public class SqrtCommand implements Command {

    @CommandSignature({})
    public void execute(Context contex) throws CalculatorException {

        double a = contex.getStack().pop();

        contex.getStack().push(sqrt(a));

    }


    @Override
    public void execute(Context context, String[] args) {}
}
