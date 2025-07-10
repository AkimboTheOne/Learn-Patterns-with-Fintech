package randomcode.patterns.creational;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton Pattern – Creational Design Pattern
 * Ensures a single instance of a shared configuration across the application.
 * 
 * Business Context: Interest rate configuration is shared across multiple modules 
 * (e.g., loans, savings, credit scoring). It must be consistent and accessed globally.
 */
public class SingletonPattern {
    private static final Logger logger = Logger.getLogger(SingletonPattern.class.getName());
    
    // Thread-safe singleton holder
    private static class SingletonHolder {
        private static final SingletonPattern INSTANCE = new SingletonPattern();
    }
    
    // Configuration data
    private double baseInterestRate;
    private final double creditRiskMultiplier;
    private boolean isRateLocked;
    
    /**
     * Private constructor prevents external instantiation.
     * Initialize with default financial configuration.
     */
    private SingletonPattern() {
        this.baseInterestRate = 0.05; // 5% base rate
        this.creditRiskMultiplier = 1.2; // 20% multiplier for high-risk accounts
        this.isRateLocked = false;
        
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Interest rate configuration initialized with default values");
        }
    }
    
    /**
     * Thread-safe singleton instance retrieval using initialization-on-demand holder idiom.
     * 
     * @return the single instance of SingletonPattern
     */
    public static SingletonPattern getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Get the current base interest rate.
     * 
     * @return current base interest rate as decimal (e.g., 0.05 for 5%)
     */
    public synchronized double getBaseInterestRate() {
        return baseInterestRate;
    }
    
    /**
     * Set the base interest rate if not locked.
     * 
     * @param rate new base interest rate as decimal
     * @throws IllegalStateException if rates are locked
     * @throws IllegalArgumentException if rate is negative
     */
    public synchronized void setBaseInterestRate(double rate) {
        if (isRateLocked) {
            throw new IllegalStateException("Interest rates are currently locked");
        }
        if (rate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        
        this.baseInterestRate = rate;
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Base interest rate updated to: %.4f%%", rate * 100));
        }
    }
    
    /**
     * Calculate effective interest rate for a customer based on credit risk.
     * 
     * @param isHighRisk true if customer is high-risk
     * @return effective interest rate
     */
    public double calculateEffectiveRate(boolean isHighRisk) {
        double effectiveRate = isHighRisk ? 
            baseInterestRate * creditRiskMultiplier : 
            baseInterestRate;
            
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Calculated effective rate: %.4f%% (High Risk: %s)", 
                effectiveRate * 100, isHighRisk));
        }
        
        return effectiveRate;
    }
    
    /**
     * Lock interest rates to prevent modifications.
     */
    public synchronized void lockRates() {
        this.isRateLocked = true;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Interest rates have been locked");
        }
    }
    
    /**
     * Unlock interest rates to allow modifications.
     */
    public synchronized void unlockRates() {
        this.isRateLocked = false;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Interest rates have been unlocked");
        }
    }
    
    /**
     * Get current rate lock status.
     * 
     * @return true if rates are locked
     */
    public synchronized boolean isRateLocked() {
        return isRateLocked;
    }
    
    /**
     * Display current configuration.
     */
    public void displayConfiguration() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format(
                "Interest Rate Configuration - Base: %.4f%%, Risk Multiplier: %.2fx, Locked: %s",
                baseInterestRate * 100, creditRiskMultiplier, isRateLocked
            ));
        }
    }
    
    /**
     * Example usage of Singleton pattern in fintech context.
     */
    public static void main(String[] args) {
        // Multiple modules accessing the same configuration instance
        SingletonPattern loanModule = SingletonPattern.getInstance();
        SingletonPattern savingsModule = SingletonPattern.getInstance();
        SingletonPattern creditModule = SingletonPattern.getInstance();
        
        // Verify it's the same instance
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Same instance? " + (loanModule == savingsModule));
        }
        
        // Display initial configuration
        loanModule.displayConfiguration();
        
        // Calculate and use rates for different risk profiles
        loanModule.calculateEffectiveRate(false); // Low risk
        loanModule.calculateEffectiveRate(true);  // High risk
        
        // Update configuration from one module (affects all)
        try {
            savingsModule.setBaseInterestRate(0.045); // 4.5%
            savingsModule.lockRates();
            
            // Attempt to modify locked rates
            creditModule.setBaseInterestRate(0.06); // This will throw an exception
        } catch (IllegalStateException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning(() -> "Cannot modify locked rates: " + e.getMessage());
            }
        }
        
        // Final configuration
        loanModule.displayConfiguration();
    }
}
