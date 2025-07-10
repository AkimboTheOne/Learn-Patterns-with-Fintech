package randomcode.patterns.creational;

import java.util.logging.Logger;

/**
 * Factory Method Pattern - Creational Design Pattern
 * Creates objects without specifying their exact class
 */
public class FactoryMethodPattern {

    private static final Logger logger = Logger.getLogger(FactoryMethodPattern.class.getName());

    // Common interface for all products (processors)
    public interface TransactionProcessor {
        void processTransaction(double amount);
    }

    // Concrete product 1 - implements the common interface
    public class CreditCardProcessor implements TransactionProcessor {
        @Override
        public void processTransaction(double amount) {
            if (logger.isLoggable(java.util.logging.Level.INFO)) {
                logger.info(String.format("Processing credit card: $%.2f", amount));
            }
        }
    }

    // Concrete product 2 - implements the common interface
    public class WireTransferProcessor implements TransactionProcessor {
        @Override
        public void processTransaction(double amount) {
            if (logger.isLoggable(java.util.logging.Level.INFO)) {
                logger.info(String.format("Processing wire transfer: $%.2f", amount));
            }
        }
    }

    // Factory class - responsible for creating the correct objects
    public class ProcessorFactory {
        // Constants for processor types
        private static final String CREDIT_TYPE = "credit";
        private static final String WIRE_TYPE = "wire";

        public ProcessorFactory() {
            // Public constructor to allow instantiation
        }

        // Factory method - decides which object to create based on parameter
        public TransactionProcessor getProcessor(String type) {
            // Input validation
            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException("Processor type cannot be null or empty");
            }

            // Creation logic - returns the appropriate object
            return switch (type.trim().toLowerCase()) {
                case CREDIT_TYPE -> new CreditCardProcessor();
                case WIRE_TYPE -> new WireTransferProcessor();
                default -> throw new IllegalArgumentException("Unknown processor type: " + type);
            };
        }
    }

    // Example usage of Factory Method pattern
    public static void main(String[] args) {
        // 1. Create pattern instance
        FactoryMethodPattern pattern = new FactoryMethodPattern();

        // 2. Create factory
        ProcessorFactory factory = pattern.new ProcessorFactory();

        // 3. Use factory to create object without knowing its specific class
        TransactionProcessor processor = factory.getProcessor("credit");

        // 4. Use the created object
        processor.processTransaction(100.00);
    }
}
