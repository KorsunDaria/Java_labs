package org.example;

import org.example.Commands.*;
import org.example.Exception.StackSizeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {
    private Context context;

    @BeforeEach
    void setUp() {
        context = new Context();
    }

    @Test
    void testAdd() throws Exception {
        context.getStack().push(10.0);
        context.getStack().push(5.0);
        new AddCommand().execute(context, new String[]{"ADD"});
        // проверяем что стек не пустой
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testSub() throws Exception {
        context.getStack().push(10.0);
        context.getStack().push(3.0);
        new SubCommand().execute(context, new String[]{"SUB"});
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testMul() throws Exception {
        context.getStack().push(4.0);
        context.getStack().push(3.0);
        new MulCommand().execute(context, new String[]{"MUL"});
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testDiv() throws Exception {
        context.getStack().push(20.0);
        context.getStack().push(5.0);
        new DivCommand().execute(context, new String[]{"DIV"});
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testSqrt() throws Exception {
        context.getStack().push(25.0);
        new SqrtCommand().execute(context, new String[]{"SQRT"});
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testDefine() throws Exception {
        String[] args = {"DEFINE", "a", "10"};
        new DefineCommand().execute(context, args);
        assertTrue(context.getMap().containsKey("a"));
    }

    @Test
    void testPushVariable() throws Exception {
        context.getMap().put("varX", 42.0);
        String[] args = {"PUSH", "varX"};
        new PushCommand().execute(context, args);
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testPop() throws Exception {
        context.getStack().push(1.0);
        context.getStack().push(2.0);
        new PopCommand().execute(context, new String[]{"POP"});
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testStackUnderflow() {
        context.getStack().push(1.0);
        assertThrows(StackSizeException.class, () -> new AddCommand().execute(context, new String[]{"ADD"}));
    }

    @Test
    void testPushNumber() throws Exception {
        String[] args = {"PUSH", "7.5"};
        new PushCommand().execute(context, args);
        assertFalse(context.getStack().isEmpty());
    }

    @Test
    void testClearContext() {
        context.getStack().push(1.0);
        context.getStack().clear();
        assertTrue(context.getStack().isEmpty());
    }
}
