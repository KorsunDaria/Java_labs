package org.example.Commands;

import org.example.Context;
import org.example.Exception.CalculatorException;

public interface Command {
    void execute(Context contex, String[] args) throws CalculatorException;
}
