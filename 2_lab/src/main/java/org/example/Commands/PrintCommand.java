package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;

@CommandName(
        value = "PRINT",
        argsCount = 0,
        minStackSize = 1
)
public class PrintCommand  implements Command {

    @CommandSignature({})
    public void execute(Context contex) throws CalculatorException {
        System.out.println(contex.getStack().peek());
    }

    @Override
    public void execute(Context contex, String[] args) {}
}
