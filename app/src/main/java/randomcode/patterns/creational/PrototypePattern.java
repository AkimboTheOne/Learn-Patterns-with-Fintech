package randomcode.patterns.creational;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prototype Pattern – Creational Design Pattern
 * Supports efficient object duplication without relying on constructors.
 * 
 * Business Context: Customer onboarding reuses validated KYC profiles with small 
 * variations (e.g., new product registration). Cloning is more efficient than recreating.
 */
public class PrototypePattern {
    private static final Logger logger = Logger.getLogger(PrototypePattern.class.getName());

    /**
     * Prototype interface defining the cloning contract.
     */
    public interface KYCPrototype extends Cloneable {
        KYCPrototype cloneProfile();
        void display();
    }

    /**
     * Concrete prototype: KYC Profile with comprehensive customer information.
     */
    public static class KYCProfile implements KYCPrototype {
        private String customerId;
        private final String fullName;
        private final LocalDate dateOfBirth;
        private final String nationality;
        private String documentType;
        private String documentNumber;
        private LocalDate documentExpiry;
        private String address;
        private String phoneNumber;
        private String email;
        private KYCStatus status;
        private LocalDate verificationDate;
        private String verifiedBy;
        private final Map<String, String> additionalData;
        
        public enum KYCStatus {
            PENDING, VERIFIED, REJECTED, EXPIRED, UNDER_REVIEW
        }
        
        /**
         * Constructor for creating new KYC profiles.
         */
        public KYCProfile(String customerId, String fullName, LocalDate dateOfBirth, String nationality) {
            this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
            this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
            this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth cannot be null");
            this.nationality = Objects.requireNonNull(nationality, "Nationality cannot be null");
            this.status = KYCStatus.PENDING;
            this.additionalData = new HashMap<>();
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Created new KYC profile for customer: " + customerId);
            }
        }
        
        /**
         * Private copy constructor for cloning.
         */
        private KYCProfile(KYCProfile original) {
            this.customerId = original.customerId;
            this.fullName = original.fullName;
            this.dateOfBirth = original.dateOfBirth;
            this.nationality = original.nationality;
            this.documentType = original.documentType;
            this.documentNumber = original.documentNumber;
            this.documentExpiry = original.documentExpiry;
            this.address = original.address;
            this.phoneNumber = original.phoneNumber;
            this.email = original.email;
            this.status = original.status;
            this.verificationDate = original.verificationDate;
            this.verifiedBy = original.verifiedBy;
            this.additionalData = new HashMap<>(original.additionalData); // Deep copy of map
        }
        
        /**
         * Implementation of the prototype cloning method.
         */
        @Override
        public KYCProfile cloneProfile() {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Cloning KYC profile for customer: " + customerId);
            }
            return new KYCProfile(this);
        }
        
        /**
         * Display KYC profile information.
         */
        @Override
        public void display() {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> String.format(
                    "KYC Profile - ID: %s, Name: %s, DOB: %s, Nationality: %s, " +
                    "Document: %s (%s), Status: %s, Verified: %s",
                    customerId, fullName, dateOfBirth, nationality,
                    documentType != null ? documentType : "N/A",
                    documentNumber != null ? documentNumber : "N/A",
                    status, verificationDate != null ? verificationDate.toString() : "Not verified"
                ));
            }
        }
        
        /**
         * Update customer ID for cloned profiles (for new product registrations).
         */
        public KYCProfile updateCustomerId(String newCustomerId) {
            Objects.requireNonNull(newCustomerId, "New customer ID cannot be null");
            this.customerId = newCustomerId;
            this.status = KYCStatus.UNDER_REVIEW; // Reset status for new product
            this.verificationDate = null;
            this.verifiedBy = null;
            return this;
        }
        
        /**
         * Add or update document information.
         */
        public KYCProfile setDocumentInfo(String type, String number, LocalDate expiry) {
            this.documentType = type;
            this.documentNumber = number;
            this.documentExpiry = expiry;
            return this;
        }
        
        /**
         * Update contact information.
         */
        public KYCProfile setContactInfo(String address, String phone, String email) {
            this.address = address;
            this.phoneNumber = phone;
            this.email = email;
            return this;
        }
        
        /**
         * Verify the KYC profile.
         */
        public KYCProfile verify(String verifierName) {
            this.status = KYCStatus.VERIFIED;
            this.verificationDate = LocalDate.now();
            this.verifiedBy = verifierName;
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> "KYC profile verified for customer: " + customerId + " by: " + verifierName);
            }
            return this;
        }
        
        /**
         * Add additional data to the profile.
         */
        public KYCProfile addAdditionalData(String key, String value) {
            this.additionalData.put(key, value);
            return this;
        }
        
        /**
         * Check if the profile is valid and not expired.
         */
        public boolean isValid() {
            boolean hasBasicInfo = customerId != null && fullName != null && 
                                   dateOfBirth != null && nationality != null;
            boolean isNotExpired = documentExpiry == null || documentExpiry.isAfter(LocalDate.now());
            boolean isVerified = status == KYCStatus.VERIFIED;
            
            return hasBasicInfo && isNotExpired && isVerified;
        }
        
        /**
         * Calculate age based on date of birth.
         */
        public int getAge() {
            return LocalDate.now().getYear() - dateOfBirth.getYear();
        }
        
        // Getters
        public String getCustomerId() { return customerId; }
        public String getFullName() { return fullName; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public String getNationality() { return nationality; }
        public String getDocumentType() { return documentType; }
        public String getDocumentNumber() { return documentNumber; }
        public LocalDate getDocumentExpiry() { return documentExpiry; }
        public String getAddress() { return address; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getEmail() { return email; }
        public KYCStatus getStatus() { return status; }
        public LocalDate getVerificationDate() { return verificationDate; }
        public String getVerifiedBy() { return verifiedBy; }
        public Map<String, String> getAdditionalData() { return new HashMap<>(additionalData); }
    }
    
    /**
     * KYC Profile Registry for managing and cloning validated profiles.
     */
    public static class KYCProfileRegistry {
        private final Map<String, KYCProfile> profileTemplates = new HashMap<>();
        
        /**
         * Register a validated profile as a template.
         */
        public void registerProfile(String templateName, KYCProfile profile) {
            Objects.requireNonNull(templateName, "Template name cannot be null");
            Objects.requireNonNull(profile, "Profile cannot be null");
            
            profileTemplates.put(templateName, profile);
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Registered KYC template: " + templateName);
            }
        }
        
        /**
         * Clone a registered profile template.
         */
        public KYCProfile cloneProfile(String templateName) {
            KYCProfile template = profileTemplates.get(templateName);
            if (template == null) {
                throw new IllegalArgumentException("Template not found: " + templateName);
            }
            
            return template.cloneProfile();
        }
        
        /**
         * Get available template names.
         */
        public String[] getAvailableTemplates() {
            return profileTemplates.keySet().toArray(String[]::new);
        }
        
        /**
         * Remove a template from registry.
         */
        public void removeTemplate(String templateName) {
            if (profileTemplates.remove(templateName) != null && logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Removed KYC template: " + templateName);
            }
        }
    }
    
    /**
     * Example usage of Prototype pattern for KYC profiles.
     */
    public static void main(String[] args) {
        // Constants for template names
        final String VERIFIED_US_TEMPLATE = "verified_us_customer";
        final String VERIFIED_EU_TEMPLATE = "verified_eu_customer";
        
        // Create a registry for profile templates
        KYCProfileRegistry registry = new KYCProfileRegistry();
        
        // Create original verified KYC profile
        KYCProfile originalProfile = new KYCProfile(
            "CUST001", 
            "Alice Johnson", 
            LocalDate.of(1985, 3, 15), 
            "US"
        );
        
        originalProfile
            .setDocumentInfo("Passport", "A12345678", LocalDate.of(2030, 3, 15))
            .setContactInfo("123 Main St, New York, NY", "+1-555-0123", "alice@email.com")
            .addAdditionalData("occupation", "Software Engineer")
            .addAdditionalData("employer", "Tech Corp")
            .verify("John Verifier");
        
        // Register as template
        registry.registerProfile(VERIFIED_US_TEMPLATE, originalProfile);
        
        originalProfile.display();
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Original profile valid: " + originalProfile.isValid());
        }
        
        // Clone for new product registration (same customer, different product)
        KYCProfile clonedForNewProduct = registry.cloneProfile(VERIFIED_US_TEMPLATE)
            .updateCustomerId("CUST001_SAVINGS")
            .addAdditionalData("product_type", "Savings Account");
        
        clonedForNewProduct.display();
        
        // Clone for similar customer profile (different customer, similar background)
        KYCProfile clonedForSimilarCustomer = registry.cloneProfile(VERIFIED_US_TEMPLATE)
            .updateCustomerId("CUST002");
        
        // Update with new customer's information
        // Note: In real scenario, you'd have methods to update name, DOB, etc.
        clonedForSimilarCustomer
            .setContactInfo("456 Oak Ave, Boston, MA", "+1-555-0456", "bob@email.com")
            .addAdditionalData("occupation", "Financial Analyst")
            .addAdditionalData("employer", "Bank Corp");
        
        clonedForSimilarCustomer.display();
        
        // Create template for international customers
        KYCProfile internationalTemplate = new KYCProfile(
            "INTL001",
            "Maria Rodriguez",
            LocalDate.of(1990, 7, 22),
            "ES"
        );
        
        internationalTemplate
            .setDocumentInfo("National ID", "12345678Z", LocalDate.of(2025, 7, 22))
            .setContactInfo("Calle Mayor 10, Madrid, Spain", "+34-123-456-789", "maria@email.com")
            .verify("International Verifier");
        
        registry.registerProfile(VERIFIED_EU_TEMPLATE, internationalTemplate);
        
        // Clone international template for new customer
        KYCProfile newEUCustomer = registry.cloneProfile(VERIFIED_EU_TEMPLATE)
            .updateCustomerId("INTL002")
            .addAdditionalData("risk_profile", "Low")
            .addAdditionalData("source_of_funds", "Employment");
        
        newEUCustomer.display();
        
        // Display registry info
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Available templates: " + String.join(", ", registry.getAvailableTemplates()));
        }
    }
}
