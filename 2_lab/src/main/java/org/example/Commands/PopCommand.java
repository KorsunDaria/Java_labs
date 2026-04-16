package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.StackSizeException;

@CommandName(
        value = "POP",
        argsCount =0 ,
        minStackSize = 1
)
public class PopCommand implements Command{
    @CommandSignature({})
    public void execute(Context contex) throws CalculatorException {
        contex.getStack().pop();
    }

    @Override
    public void execute(Context contex, String[] args) {}
}
