package auctionsniper;

import auctionsniper.ui.MainWindow;

import javax.swing.*;


public class Main {
    public static final String STATUS_JOINING = "Joining";

    private MainWindow ui;

    public Main() throws Exception {
        startUserInterface();
    }

    public static void main(String... args) throws Exception {
        Main main = new Main();
    }

    private void startUserInterface() throws Exception {
        SwingUtilities.invokeAndWait(() -> ui = new MainWindow());
    }
}
