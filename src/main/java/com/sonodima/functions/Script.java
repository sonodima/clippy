package com.sonodima.functions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Script {
    public static String parseFiles(List<File> files) {
        StringBuilder list = new StringBuilder();

        for (int i = 0; i < files.size(); i++) {
            list.append(String.format("\"%s\"", files.get(i)));
            if (i != files.size() - 1) {
                list.append(", ");
            }
        }

        return list.toString();
    }

    private static String[] stringListToArray(ArrayList<String> list) {
        String[] result = new String[list.size()];

        for(int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }

        return result;
    }

    private static boolean executeCommand(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(new String[]{ command });
        } catch (IOException exception) {
            return false;
        }

        return true;
    }

    private static boolean executeCommands(String[] commands) {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(commands);
        } catch (IOException exception) {
            return false;
        }

        return true;
    }

    public static boolean runAppleScript(String[] script) {
        ArrayList<String> result = new ArrayList<>();

        result.add("osascript");
        result.add("-e");

        for (int i = 0; i < script.length; i++) {
            result.add(script[i]);
            if (i != script.length - 1) {
                result.add("-e");
            }
        }

        String[] executable = stringListToArray(result);
        return executeCommands(executable);
    }

    public static boolean runPowerShellScript(String[] script) {
        StringBuilder result = new StringBuilder("powershell -ExecutionPolicy Bypass -Command \"");

        for (int i = 0; i < script.length; i++) {
            String sanitized = script[i].replaceAll("\"", "\\\\\"");
            result.append(sanitized);
            if (i != script.length - 1) {
                result.append("; ");
            }
        }

        result.append("\"");

        return executeCommand(result.toString());
    }
}
