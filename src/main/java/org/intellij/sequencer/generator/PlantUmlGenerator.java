package org.intellij.sequencer.generator;

public class PlantUmlGenerator {

    private static final String SEPARATOR = System.lineSeparator();

    private final CallStack stack;

    public PlantUmlGenerator(CallStack stack) {
        this.stack = stack;
    }

    public String generate() {
        StringBuilder result = new StringBuilder();
        result.append("@startuml").append(SEPARATOR);
        result.append(SEPARATOR).append("skinparam SequenceMessageAlign center").append(SEPARATOR);

        renderCalls(stack, result);

        result.append("@enduml").append(SEPARATOR);

        return result.toString();
    }

    private MethodDescription renderCalls(CallStack stack, StringBuilder result) {
        MethodDescription parent = stack.getMethod();
        if (stack.getCalls().isEmpty()) {
            result.append(SEPARATOR).append("activate ").append(parent.getClassDescription().getClassShortName()).append(SEPARATOR);
            return parent;
        }

        result.append(SEPARATOR).append("activate ").append(parent.getClassDescription().getClassShortName()).append(SEPARATOR);

        for (Object call : stack.getCalls()) {
            CallStack stackVal = (CallStack) call;
            render(parent, stackVal.getMethod(), result, true);
            MethodDescription front = renderCalls(stackVal, result);
            render(parent, stackVal.getMethod(), result, false);
            result.append("deactivate ").append(front.getClassDescription().getClassShortName()).append(SEPARATOR).append(SEPARATOR);
        }

        return parent;
    }

    private void render(MethodDescription parent, MethodDescription child, StringBuilder result, boolean isForward) {
        if (isForward) {
            result.append("' ").append(parent.getClassDescription().getClassName()).append(SEPARATOR);
        } else {
            result.append("' ").append(child.getClassDescription().getClassName()).append(SEPARATOR);
        }

        result
                .append(parent.getClassDescription().getClassShortName())
                .append(isForward ? " --> " : " <-- ")
                .append(child.getClassDescription().getClassShortName())
                .append(" : ").append(isForward ? child.getMethodName() : deduceType(child.getReturnType()))
                .append(SEPARATOR);
    }

    private String deduceType(String input) {
        if (input.contains("<")) {
            return deduceType(input.split("<", 2)[0]);
        }

        if (input.startsWith("void")) {
            return " ";
        }

        String[] split = input.split("\\.");
        return split.length > 0 ? split[split.length - 1] : input;
    }
}
