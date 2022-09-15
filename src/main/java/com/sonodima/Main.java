package com.sonodima;

import com.sonodima.control.Handler;
import com.sonodima.utilities.Utils;
import com.sonodima.view.Window;

import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {
        FlatDarkLaf.setup();

        Window window = new Window();
        Utils utils = new Utils(window);
        new Handler(window, utils);
    }
}
