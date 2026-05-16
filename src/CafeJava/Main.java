package CafeJava;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        System.out.println("====================================");
        System.out.println("  Cafe Java — Sistem Kasir Café");
        System.out.println("====================================");
        Database.get();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[APP] Menutup...");
            Database.get().close();
            System.out.println("[APP] Sampai jumpa!");
        }));
    }
}
