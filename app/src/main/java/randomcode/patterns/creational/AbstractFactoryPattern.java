package randomcode.patterns.creational;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Factory Pattern – Creational Design Pattern
 * Produces families of related UI components without specifying concrete classes.
 */
public class AbstractFactoryPattern {
    private static final Logger logger = Logger.getLogger(AbstractFactoryPattern.class.getName());

    /** Product A interface. */
    public interface Button {
        void render();
    }

    /** Product B interface. */
    public interface TextField {
        void render();
    }

    /** Abstract factory interface for UI components. */
    public interface UIFactory {
        Button createButton();
        TextField createTextField();
    }

    /**
     * Concrete factory for web-based UI.
     */
    public class WebUIFactory implements UIFactory {
        @Override
        public Button createButton() {
            return () -> {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Rendering Web Button");
                }
            };
        }
        @Override
        public TextField createTextField() {
            return () -> {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Rendering Web TextField");
                }
            };
        }
    }

    /**
     * Concrete factory for mobile-based UI.
     */
    public class MobileUIFactory implements UIFactory {
        @Override
        public Button createButton() {
            return () -> {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Rendering Mobile Button");
                }
            };
        }
        @Override
        public TextField createTextField() {
            return () -> {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Rendering Mobile TextField");
                }
            };
        }
    }

    /** Client code example. */
    public static void main(String[] args) {
        AbstractFactoryPattern pattern = new AbstractFactoryPattern();
        UIFactory factory = pattern.new MobileUIFactory();
        factory.createButton().render();
        factory.createTextField().render();
    }
}
