//package org.example.benchmark;
//
//import org.example.Calculator;
//import org.example.CommandFactory;
//import org.example.Context;
//import org.example.ExecutionResult;
//import org.openjdk.jmh.annotations.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//@BenchmarkMode(Mode.Throughput)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@State(Scope.Thread)
//public class CalculatorBenchmark {
//
//    private Calculator calculator;
//    private List<String[]> commands;
//    private List<String[]> pushCommands;
//
//    @Setup(Level.Iteration)
//    public void setup() throws Exception {
//        CommandFactory factory = new CommandFactory();
//        calculator = new Calculator(factory);
//
//        commands = new ArrayList<>();
//        pushCommands = new ArrayList<>();
//
//        for (int i = 0; i < 1000; i++) {
//            String[] push1 = new String[]{"PUSH", String.valueOf(i)};
//            String[] push2 = new String[]{"PUSH", "10"};
//            String[] add = new String[]{"ADD"};
//
//            commands.add(push1);
//            commands.add(push2);
//            commands.add(add);
//
//            pushCommands.add(push1);
//            pushCommands.add(push2);
//        }
//    }
//
//
//    @Benchmark
//    @Fork(value = 1, warmups = 1)
//    @Warmup(iterations = 3, time = 1)
//    @Measurement(iterations = 5, time = 1)
//    public List<ExecutionResult> runCalculatorBenchmark() throws Exception {
//        return calculator.run(commands);
//    }
//
//    @Benchmark
//    @Fork(value = 1, warmups = 1)
//    @Warmup(iterations = 3, time = 1)
//    @Measurement(iterations = 5, time = 1)
//    public List<ExecutionResult> runPushOnly() throws Exception {
//        return calculator.run(pushCommands);
//    }
//
//    @Benchmark
//    @Threads(4)
//    @Fork(value = 1, warmups = 1)
//    @Warmup(iterations = 3, time = 1)
//    @Measurement(iterations = 5, time = 1)
//    public List<ExecutionResult> runMultiThreadedStress() throws Exception {
//        return calculator.run(commands);
//    }
//}