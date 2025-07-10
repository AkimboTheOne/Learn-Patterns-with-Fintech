package randomcode.patterns.creational;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object Pool Pattern – Creational Design Pattern
 * Optimizes performance and resource reuse by maintaining a pool of initialized objects.
 * 
 * Business Context: A trading engine manages connections to external market APIs. 
 * Instead of opening/closing them constantly, a pool maintains reusable connections.
 */
public class ObjectPoolPattern {
    private static final Logger logger = Logger.getLogger(ObjectPoolPattern.class.getName());

    /**
     * Poolable interface for objects that can be managed by the pool.
     */
    public interface Poolable {
        void reset();
        boolean isValid();
        void close();
    }

    /**
     * Market connection object that represents an expensive resource.
     */
    public static class MarketConnection implements Poolable {
        private final UUID connectionId;
        private final String marketEndpoint;
        private final LocalDateTime createdAt;
        private LocalDateTime lastUsed;
        private final AtomicInteger usageCount;
        private final AtomicBoolean isConnected;
        private volatile boolean isValid;
        
        public MarketConnection(String marketEndpoint) {
            this.connectionId = UUID.randomUUID();
            this.marketEndpoint = Objects.requireNonNull(marketEndpoint, "Market endpoint cannot be null");
            this.createdAt = LocalDateTime.now();
            this.lastUsed = LocalDateTime.now();
            this.usageCount = new AtomicInteger(0);
            this.isConnected = new AtomicBoolean(false);
            this.isValid = true;
            
            // Simulate expensive connection setup
            simulateConnectionSetup();
        }
        
        /**
         * Simulate expensive connection establishment.
         */
        private void simulateConnectionSetup() {
            try {
                // Simulate network latency
                Thread.sleep(100);
                isConnected.set(true);
                
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(() -> String.format("Established connection %s to %s", 
                        connectionId.toString().substring(0, 8), marketEndpoint));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isValid = false;
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Connection setup interrupted");
                }
            }
        }
        
        /**
         * Use the connection to fetch market data.
         */
        public void fetchMarketData(String symbol) {
            if (!isValid || !isConnected.get()) {
                throw new IllegalStateException("Connection is not valid or not connected");
            }
            
            usageCount.incrementAndGet();
            lastUsed = LocalDateTime.now();
            
            // Simulate market data fetching
            try {
                Thread.sleep(10); // Simulate API call
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(() -> String.format("Fetched data for %s via connection %s (usage: %d)", 
                        symbol, connectionId.toString().substring(0, 8), usageCount.get()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Market data fetch interrupted");
                }
            }
        }
        
        /**
         * Execute a trade order through this connection.
         */
        public void executeTrade(String symbol, int quantity, double price) {
            if (!isValid || !isConnected.get()) {
                throw new IllegalStateException("Connection is not valid or not connected");
            }
            
            usageCount.incrementAndGet();
            lastUsed = LocalDateTime.now();
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> String.format("Executed trade: %d shares of %s at $%.2f via connection %s", 
                    quantity, symbol, price, connectionId.toString().substring(0, 8)));
            }
        }
        
        /**
         * Reset the connection state for reuse.
         */
        @Override
        public void reset() {
            lastUsed = LocalDateTime.now();
            // Don't reset usage count - it's cumulative for monitoring
            
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(() -> "Reset connection " + connectionId.toString().substring(0, 8));
            }
        }
        
        /**
         * Check if the connection is still valid.
         */
        @Override
        public boolean isValid() {
            // Connection expires after 30 minutes of inactivity
            boolean notExpired = lastUsed.isAfter(LocalDateTime.now().minusMinutes(30));
            // Connection becomes invalid after 1000 uses (simulate wear)
            boolean notOverused = usageCount.get() < 1000;
            
            return isValid && isConnected.get() && notExpired && notOverused;
        }
        
        /**
         * Close the connection permanently.
         */
        @Override
        public void close() {
            isValid = false;
            isConnected.set(false);
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> String.format("Closed connection %s (total usage: %d)", 
                    connectionId.toString().substring(0, 8), usageCount.get()));
            }
        }
        
        // Getters
        public UUID getConnectionId() { return connectionId; }
        public String getMarketEndpoint() { return marketEndpoint; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastUsed() { return lastUsed; }
        public int getUsageCount() { return usageCount.get(); }
        public boolean isConnected() { return isConnected.get(); }
    }
    
    /**
     * Generic object pool implementation with monitoring and lifecycle management.
     */
    public static class ConnectionPool<T extends Poolable> {
        private final BlockingQueue<T> pool;
        private final PoolableFactory<T> factory;
        private final int maxSize;
        private final AtomicInteger createdCount;
        private final AtomicLong totalAcquisitions;
        private final AtomicLong totalReleases;
        private volatile boolean isShutdown;
        
        /**
         * Factory interface for creating poolable objects.
         */
        public interface PoolableFactory<T extends Poolable> {
            T create();
        }
        
        /**
         * Pool statistics for monitoring.
         */
        public static class PoolStats {
            private final int poolSize;
            private final int maxPoolSize;
            private final int createdObjects;
            private final long totalAcquisitions;
            private final long totalReleases;
            
            public PoolStats(int poolSize, int maxPoolSize, int createdObjects, 
                           long totalAcquisitions, long totalReleases) {
                this.poolSize = poolSize;
                this.maxPoolSize = maxPoolSize;
                this.createdObjects = createdObjects;
                this.totalAcquisitions = totalAcquisitions;
                this.totalReleases = totalReleases;
            }
            
            public int getPoolSize() { return poolSize; }
            public int getMaxPoolSize() { return maxPoolSize; }
            public int getCreatedObjects() { return createdObjects; }
            public long getTotalAcquisitions() { return totalAcquisitions; }
            public long getTotalReleases() { return totalReleases; }
            public int getActiveObjects() { return (int) (totalAcquisitions - totalReleases); }
            
            @Override
            public String toString() {
                return String.format("Pool[size=%d/%d, created=%d, active=%d, acquisitions=%d, releases=%d]",
                    poolSize, maxPoolSize, createdObjects, getActiveObjects(), totalAcquisitions, totalReleases);
            }
        }
        
        /**
         * Create a connection pool with specified size and factory.
         */
        public ConnectionPool(int maxSize, PoolableFactory<T> factory) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("Pool size must be positive");
            }
            
            this.maxSize = maxSize;
            this.factory = Objects.requireNonNull(factory, "Factory cannot be null");
            this.pool = new LinkedBlockingQueue<>();
            this.createdCount = new AtomicInteger(0);
            this.totalAcquisitions = new AtomicLong(0);
            this.totalReleases = new AtomicLong(0);
            this.isShutdown = false;
            
            // Pre-populate pool with initial objects
            initializePool();
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Created connection pool with max size: " + maxSize);
            }
        }
        
        /**
         * Initialize pool with objects.
         */
        private void initializePool() {
            int initialSize = Math.min(2, maxSize); // Start with 2 connections
            for (int i = 0; i < initialSize; i++) {
                T object = factory.create();
                if (object != null && object.isValid()) {
                    boolean added = pool.offer(object);
                    if (added) {
                        createdCount.incrementAndGet();
                    } else {
                        object.close();
                    }
                }
            }
        }
        
        /**
         * Acquire an object from the pool.
         */
        public T acquire() throws InterruptedException {
            return acquire(5, TimeUnit.SECONDS);
        }
        
        /**
         * Acquire an object from the pool with timeout.
         */
        public T acquire(long timeout, TimeUnit unit) throws InterruptedException {
            if (isShutdown) {
                throw new IllegalStateException("Pool is shutdown");
            }
            
            totalAcquisitions.incrementAndGet();
            
            // Try to get from pool first
            T object = pool.poll();
            
            // If pool is empty and we haven't reached max size, create new
            if (object == null && createdCount.get() < maxSize) {
                object = factory.create();
                if (object != null && object.isValid()) {
                    createdCount.incrementAndGet();
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Created new object for pool");
                    }
                }
            }
            
            // If still no object, wait for one to be returned
            if (object == null) {
                object = pool.poll(timeout, unit);
            }
            
            // Validate object before returning
            if (object != null && !object.isValid()) {
                object.close();
                createdCount.decrementAndGet();
                // Try to get another one recursively (with reduced timeout)
                long remainingTimeout = Math.max(1, timeout / 2);
                return acquire(remainingTimeout, unit);
            }
            
            if (object != null) {
                object.reset();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Acquired object from pool");
                }
            }
            
            return object;
        }
        
        /**
         * Release an object back to the pool.
         */
        public void release(T object) {
            if (object == null || isShutdown) {
                return;
            }
            
            totalReleases.incrementAndGet();
            
            if (object.isValid()) {
                boolean offered = pool.offer(object);
                if (!offered) {
                    // Pool is full, close the object
                    object.close();
                    createdCount.decrementAndGet();
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(() -> "Released object to pool (offered: " + offered + ")");
                }
            } else {
                // Object is invalid, close it
                object.close();
                createdCount.decrementAndGet();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Closed invalid object");
                }
            }
        }
        
        /**
         * Get current pool statistics.
         */
        public PoolStats getStats() {
            return new PoolStats(
                pool.size(),
                maxSize,
                createdCount.get(),
                totalAcquisitions.get(),
                totalReleases.get()
            );
        }
        
        /**
         * Shutdown the pool and close all objects.
         */
        public void shutdown() {
            isShutdown = true;
            
            T object;
            while ((object = pool.poll()) != null) {
                object.close();
            }
            
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Connection pool shutdown completed");
            }
        }
        
        public boolean isShutdown() {
            return isShutdown;
        }
    }
    
    /**
     * Example usage of Object Pool pattern for market connections.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create a factory for market connections
        ConnectionPool.PoolableFactory<MarketConnection> factory = 
            () -> new MarketConnection("market-api.financialdata.com");
        
        // Create connection pool
        ConnectionPool<MarketConnection> pool = new ConnectionPool<>(5, factory);
        
        // Display initial stats
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Initial pool stats: " + pool.getStats());
        }
        
        // Simulate trading activity
        simulateTradingActivity(pool);
        
        // Display final stats
        if (logger.isLoggable(Level.INFO)) {
            logger.info(() -> "Final pool stats: " + pool.getStats());
        }
        
        // Shutdown pool
        pool.shutdown();
    }
    
    /**
     * Simulate multiple trading operations using the connection pool.
     */
    private static void simulateTradingActivity(ConnectionPool<MarketConnection> pool) 
            throws InterruptedException {
        
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN"};
        
        // Simulate multiple concurrent trading operations
        for (int i = 0; i < 10; i++) {
            String symbol = symbols[i % symbols.length];
            
            // Acquire connection
            MarketConnection connection = pool.acquire();
            
            if (connection != null) {
                try {
                    // Fetch market data
                    connection.fetchMarketData(symbol);
                    
                    // Execute trade
                    connection.executeTrade(symbol, 100 * (i + 1), 150.0 + (i * 5));
                    
                    // Simulate some processing time (vary the delay to avoid constant sleep in loop)
                    Thread.sleep(50L + (i % 10)); // Variable delay 50-59ms
                    
                } finally {
                    // Always release connection back to pool
                    pool.release(connection);
                }
            } else {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Could not acquire connection from pool");
                }
            }
            
            // Display stats every few operations
            if (i % 3 == 0 && logger.isLoggable(Level.INFO)) {
                logger.info(() -> "Current pool stats: " + pool.getStats());
            }
        }
        
        // Demonstrate connection reuse by acquiring multiple connections
        MarketConnection conn1 = pool.acquire();
        MarketConnection conn2 = pool.acquire();
        MarketConnection conn3 = pool.acquire();
        
        if (conn1 != null && conn2 != null && conn3 != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info(() -> String.format("Acquired 3 connections: %s, %s, %s",
                    conn1.getConnectionId().toString().substring(0, 8),
                    conn2.getConnectionId().toString().substring(0, 8),
                    conn3.getConnectionId().toString().substring(0, 8)));
            }
            
            // Release them back
            pool.release(conn1);
            pool.release(conn2);
            pool.release(conn3);
        }
    }
}
