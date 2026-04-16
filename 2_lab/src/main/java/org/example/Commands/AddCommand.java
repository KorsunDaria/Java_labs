package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;
import org.example.Exception.InvalidParametrException;
import org.example.Exception.StackSizeException;

@CommandName(
        value = "ADD",
        argsCount = 0,
        minStackSize = 2
)
public class AddCommand implements Command {

    @CommandSignature({})
    public void execute(Context context) {
        double a = context.getStack().pop();
        double b = context.getStack().pop();

        context.getStack().push(a + b);
    }

    AddCommand (int a, int b){
        System.out.println("Test");
    }

    @Override
    public void execute(Context context, String[] args) {}
}
