package randomcode.patterns.creational;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builder Pattern – Creational Design Pattern
 * Separates complex object construction from its representation.
 * 
 * Business Context: Building a full mortgage application object requires collecting 
 * personal info, credit history, employment, etc., often conditionally.
 */
public class BuilderPattern {
    private static final Logger logger = Logger.getLogger(BuilderPattern.class.getName());

    /**
     * Complex product: Mortgage Application with multiple optional components.
     */
    public static class MortgageApplication {
        // Required fields
        private final String applicantName;
        private final double requestedAmount;
        
        // Optional fields with defaults
        private final double annualIncome;
        private final boolean hasCreditHistory;
        private final int creditScore;
        private final String employmentType;
        private final int employmentYears;
        private final double downPayment;
        private final String propertyType;
        private final LocalDate applicationDate;
        private final List<String> documents;
        private final boolean isFirstTimeBuyer;
        
        /**
         * Private constructor - only accessible through Builder.
         */
        private MortgageApplication(Builder builder) {
            this.applicantName = builder.applicantName;
            this.requestedAmount = builder.requestedAmount;
            this.annualIncome = builder.annualIncome;
            this.hasCreditHistory = builder.hasCreditHistory;
            this.creditScore = builder.creditScore;
            this.employmentType = builder.employmentType;
            this.employmentYears = builder.employmentYears;
            this.downPayment = builder.downPayment;
            this.propertyType = builder.propertyType;
            this.applicationDate = builder.applicationDate;
            this.documents = new ArrayList<>(builder.documents);
            this.isFirstTimeBuyer = builder.isFirstTimeBuyer;
        }
        
        /**
         * Display mortgage application details.
         */
        public void display() {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> String.format(
                    "Mortgage Application - Applicant: %s, Amount: $%.2f, Income: $%.2f, " +
                    "Credit Score: %d, Employment: %s (%d years), Down Payment: $%.2f, " +
                    "Property: %s, First Time Buyer: %s, Documents: %d",
                    applicantName, requestedAmount, annualIncome, creditScore, 
                    employmentType, employmentYears, downPayment, propertyType, 
                    isFirstTimeBuyer, documents.size()
                ));
            }
        }
        
        /**
         * Calculate loan-to-value ratio.
         */
        public double calculateLoanToValueRatio() {
            if (downPayment <= 0) {
                return 1.0; // 100% financing
            }
            double propertyValue = requestedAmount + downPayment;
            return requestedAmount / propertyValue;
        }
        
        /**
         * Determine if application is potentially approvable based on basic criteria.
         */
        public boolean isPotentiallyApprovable() {
            double debtToIncomeRatio = (requestedAmount * 0.05) / annualIncome; // Assuming 5% interest
            boolean hasGoodCredit = creditScore >= 650;
            boolean hasStableEmployment = employmentYears >= 2;
            boolean hasReasonableLTV = calculateLoanToValueRatio() <= 0.95;
            
            return hasGoodCredit && hasStableEmployment && hasReasonableLTV && debtToIncomeRatio <= 0.28;
        }
        
        // Getters
        public String getApplicantName() { return applicantName; }
        public double getRequestedAmount() { return requestedAmount; }
        public double getAnnualIncome() { return annualIncome; }
        public boolean hasCreditHistory() { return hasCreditHistory; }
        public int getCreditScore() { return creditScore; }
        public String getEmploymentType() { return employmentType; }
        public int getEmploymentYears() { return employmentYears; }
        public double getDownPayment() { return downPayment; }
        public String getPropertyType() { return propertyType; }
        public LocalDate getApplicationDate() { return applicationDate; }
        public List<String> getDocuments() { return new ArrayList<>(documents); }
        public boolean isFirstTimeBuyer() { return isFirstTimeBuyer; }
        
        /**
         * Builder class for constructing MortgageApplication instances.
         */
        public static class Builder {
            // Required parameters
            private String applicantName;
            private double requestedAmount;
            
            // Optional parameters with default values
            private double annualIncome = 0.0;
            private boolean hasCreditHistory = false;
            private int creditScore = 300; // Minimum possible score
            private String employmentType = "Unknown";
            private int employmentYears = 0;
            private double downPayment = 0.0;
            private String propertyType = "Primary Residence";
            private LocalDate applicationDate = LocalDate.now();
            private List<String> documents = new ArrayList<>();
            private boolean isFirstTimeBuyer = false;
            
            /**
             * Constructor with required parameters.
             */
            public Builder(String applicantName, double requestedAmount) {
                this.applicantName = validateApplicantName(applicantName);
                this.requestedAmount = validateRequestedAmount(requestedAmount);
            }
            
            private String validateApplicantName(String name) {
                Objects.requireNonNull(name, "Applicant name must not be null");
                if (name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Applicant name must not be empty");
                }
                return name.trim();
            }
            
            private double validateRequestedAmount(double amount) {
                if (amount <= 0) {
                    throw new IllegalArgumentException("Requested amount must be positive");
                }
                if (amount > 10_000_000) { // $10M limit
                    throw new IllegalArgumentException("Requested amount exceeds maximum limit");
                }
                return amount;
            }
            
            /**
             * Set applicant name with validation.
             */
            public Builder setApplicantName(String name) {
                Objects.requireNonNull(name, "Applicant name must not be null");
                if (name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Applicant name must not be empty");
                }
                this.applicantName = name.trim();
                return this;
            }
            
            /**
             * Set requested loan amount with validation.
             */
            public Builder setRequestedAmount(double amount) {
                if (amount <= 0) {
                    throw new IllegalArgumentException("Requested amount must be positive");
                }
                if (amount > 10_000_000) { // $10M limit
                    throw new IllegalArgumentException("Requested amount exceeds maximum limit");
                }
                this.requestedAmount = amount;
                return this;
            }
            
            public Builder setAnnualIncome(double income) {
                if (income < 0) {
                    throw new IllegalArgumentException("Annual income cannot be negative");
                }
                this.annualIncome = income;
                return this;
            }
            
            public Builder setCreditHistory(boolean hasHistory) {
                this.hasCreditHistory = hasHistory;
                return this;
            }
            
            public Builder setCreditScore(int score) {
                if (score < 300 || score > 850) {
                    throw new IllegalArgumentException("Credit score must be between 300 and 850");
                }
                this.creditScore = score;
                return this;
            }
            
            public Builder setEmploymentType(String type) {
                this.employmentType = Objects.requireNonNullElse(type, "Unknown");
                return this;
            }
            
            public Builder setEmploymentYears(int years) {
                if (years < 0) {
                    throw new IllegalArgumentException("Employment years cannot be negative");
                }
                this.employmentYears = years;
                return this;
            }
            
            public Builder setDownPayment(double payment) {
                if (payment < 0) {
                    throw new IllegalArgumentException("Down payment cannot be negative");
                }
                this.downPayment = payment;
                return this;
            }
            
            public Builder setPropertyType(String type) {
                this.propertyType = Objects.requireNonNullElse(type, "Primary Residence");
                return this;
            }
            
            public Builder setApplicationDate(LocalDate date) {
                this.applicationDate = Objects.requireNonNullElse(date, LocalDate.now());
                return this;
            }
            
            public Builder addDocument(String document) {
                if (document != null && !document.trim().isEmpty()) {
                    this.documents.add(document.trim());
                }
                return this;
            }
            
            public Builder addDocuments(List<String> docs) {
                if (docs != null) {
                    docs.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(doc -> !doc.isEmpty())
                        .forEach(this.documents::add);
                }
                return this;
            }
            
            public Builder setFirstTimeBuyer(boolean isFirstTime) {
                this.isFirstTimeBuyer = isFirstTime;
                return this;
            }
            
            /**
             * Build the final MortgageApplication instance.
             * Performs final validation before creation.
             */
            public MortgageApplication build() {
                validateBusinessRules();
                return new MortgageApplication(this);
            }
            
            /**
             * Validate business rules before building.
             */
            private void validateBusinessRules() {
                // Basic debt-to-income check
                if (annualIncome > 0) {
                    double monthlyPayment = (requestedAmount * 0.05) / 12; // Rough estimate
                    double monthlyIncome = annualIncome / 12;
                    if (monthlyPayment / monthlyIncome > 0.50 && logger.isLoggable(Level.WARNING)) {
                        logger.warning("High debt-to-income ratio detected");
                    }
                }
                
                // Ensure minimum documentation for high-value loans
                if (requestedAmount > 500_000 && documents.size() < 3 && logger.isLoggable(Level.WARNING)) {
                    logger.warning("High-value loan may require additional documentation");
                }
            }
        }
    }
    
    /**
     * Example usage of Builder pattern for mortgage applications.
     */
    public static void main(String[] args) {
        // Example 1: Basic application
        MortgageApplication basicApp = new MortgageApplication.Builder("John Doe", 350_000)
            .setAnnualIncome(95_000)
            .setCreditScore(720)
            .setCreditHistory(true)
            .build();
        
        basicApp.display();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Potentially approvable: " + basicApp.isPotentiallyApprovable());
        }
        
        // Example 2: Comprehensive application
        MortgageApplication comprehensiveApp = new MortgageApplication.Builder("Jane Smith", 750_000)
            .setAnnualIncome(150_000)
            .setCreditScore(780)
            .setCreditHistory(true)
            .setEmploymentType("Software Engineer")
            .setEmploymentYears(5)
            .setDownPayment(150_000)
            .setPropertyType("Single Family Home")
            .setFirstTimeBuyer(false)
            .addDocument("W-2 Forms")
            .addDocument("Bank Statements")
            .addDocument("Tax Returns")
            .addDocument("Employment Verification")
            .build();
        
        comprehensiveApp.display();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> String.format("LTV Ratio: %.2f%%, Potentially approvable: %s",
                comprehensiveApp.calculateLoanToValueRatio() * 100,
                comprehensiveApp.isPotentiallyApprovable()));
        }
        
        // Example 3: First-time buyer with minimal down payment
        MortgageApplication firstTimeBuyerApp = new MortgageApplication.Builder("Mike Johnson", 400_000)
            .setAnnualIncome(85_000)
            .setCreditScore(680)
            .setCreditHistory(true)
            .setEmploymentType("Teacher")
            .setEmploymentYears(3)
            .setDownPayment(20_000) // 5% down
            .setFirstTimeBuyer(true)
            .addDocuments(List.of("Paystubs", "Bank Statements", "Pre-approval Letter"))
            .build();
        
        firstTimeBuyerApp.display();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> String.format("First-time buyer LTV: %.2f%%, Approvable: %s",
                firstTimeBuyerApp.calculateLoanToValueRatio() * 100,
                firstTimeBuyerApp.isPotentiallyApprovable()));
        }
    }
}
