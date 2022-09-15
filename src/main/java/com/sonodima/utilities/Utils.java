package com.sonodima.utilities;

import com.sonodima.view.Window;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private final Window window;

    public Utils(Window window) {
        super();

        this.window = window;
    }

    public static void setTimeout(Runnable runnable, int delay){
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
            } catch (Exception ignored) { }
        }).start();
    }

    public static boolean validateAddress(String address) {
        Pattern expression = Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
        Matcher matcher = expression.matcher(address);

        return matcher.find();
    }

    public static Path createTempDirectory() {
        Path path = null;

        try {
            path = Files.createTempDirectory("clippy");
        } catch (IOException ignored) { }

        return path;
    }

    public void writeStatus(String value, String type, String category) {
        JLabel statusLabel;

        switch (category) {
            case "server":
                statusLabel = window.getServerStatusLabel();
                break;
            case "client":
                statusLabel = window.getClientStatusLabel();
                break;
            default:
                return;
        }

        switch (type) {
            case "error" -> statusLabel.setForeground(new Color(207, 57, 27));
            case "warning" -> statusLabel.setForeground(new Color(219, 143, 37));
            case "success" -> statusLabel.setForeground(new Color(37, 140, 200));
            default -> statusLabel.setForeground(new Color(166, 166, 166));
        }

        statusLabel.setText(value);
        setTimeout(() -> statusLabel.setText(""), 2000);
    }

    public void writeLog(String value, String type) {
        StyledDocument styledDocument = window.getLogField().getStyledDocument();
        Style style = window.getLogField().addStyle("stylish", null);

        switch (type) {
            case "error" -> StyleConstants.setForeground(style, new Color(207, 57, 27));
            case "warning" -> StyleConstants.setForeground(style, new Color(219, 143, 37));
            case "success" -> StyleConstants.setForeground(style, new Color(37, 140, 200));
            default -> StyleConstants.setForeground(style, new Color(166, 166, 166));
        }

        String dateString = String.format("[%d:%d:%d] ", LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(), LocalDateTime.now().getSecond());

        try {
            styledDocument.insertString(styledDocument.getLength(), dateString + value + "\n", style);

            // Limit the maximum amount of lines displayed in the log
            String[] lines = window.getLogField().getText().split("\n");
            if (lines.length > 128) {
                styledDocument.remove(0, lines[0].length() + 1);
            }
        }
        catch (BadLocationException ignored) { }

        // Scroll down
        window.getLogField().setCaretPosition(styledDocument.getLength());
    }
}