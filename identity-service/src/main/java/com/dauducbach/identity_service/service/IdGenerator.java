package com.dauducbach.identity_service.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class IdGenerator {
    private final long epoch = 1735689600000L;        // Epoch mốc 1/1/2025

    private final long datacenterIdBits = 5L;         // 5 bits
    private final long workerIdBits = 5L;
    private final long sequenceBits = 12L;

    private final long maxDatacenterId = ~(-1L << datacenterIdBits);      // 31
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxSequence = ~(-1L << sequenceBits);

    // Tinh toan cac shift
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final long datacenterId;
    private final long workerId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public IdGenerator(long datacenterId, long workerId) {
        if (datacenterId > maxDatacenterId || workerId > maxWorkerId) {
            throw new IllegalArgumentException("Invalid datacenterId or workerId");
        }

        this.datacenterId = datacenterId;
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (timestamp == lastTimestamp) {
             sequence = (sequence + 1) & maxSequence;
             if (sequence == 0) {
                 timestamp = waitNextMillis(lastTimestamp);
             }
        }else {
            sequence = 0;
        }

        lastTimestamp = timestamp;
        log.info("timestamp: {}",timestamp);
        log.info("datacenter id: {}", this.datacenterId);
        log.info("worker id: {}", workerId);

        return (timestamp - epoch) << timestampShift |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence
                ;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public void decodeSnowflake(long snowflakeId) {
        long timestamp = (snowflakeId >> 22) + epoch;       // 63 dich 22 = 41 bit
        long datacenterId = (snowflakeId >> 17) & 0x1F;     // 21 dich 17 = 5 bit
        long workerId = (snowflakeId >> 12) & 0x1F;         // 16 dich 12 = 5 bit
        long sequence = snowflakeId & 0xFFF;                // 11 dich 0  = 12 bit

        System.out.println("Decoded Snowflake ID " + snowflakeId + ":");
        System.out.println("Timestamp (UTC): " + new Date(timestamp));
        System.out.println("Datacenter ID: " + datacenterId);
        System.out.println("Worker ID: " + workerId);
        System.out.println("Sequence: " + sequence);
    }
}
