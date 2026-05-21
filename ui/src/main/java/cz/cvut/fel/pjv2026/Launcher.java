package cz.cvut.fel.pjv2026;

/**
 * Plain (non-{@code Application}) entry point used as the jar's
 * {@code Main-Class}. When a {@code javafx.application.Application} subclass
 * is launched directly from a jar whose runtime does not have the JavaFX
 * modules on the module path, the JVM rejects the start. Delegating through
 * this class works around that, because the JavaFX runtime is initialized
 * only inside {@link MainApp#main(String[])}.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
