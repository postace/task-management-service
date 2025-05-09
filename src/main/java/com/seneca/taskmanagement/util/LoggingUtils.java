package com.seneca.taskmanagement.util;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Utility class for structured logging throughout the application.
 */
public class LoggingUtils {

    /**
     * Adds task-specific context to the MDC for logging.
     *
     * @param taskId The ID of the task being processed
     */
    public static void setTaskContext(String taskId) {
        if (taskId != null) {
            MDC.put("taskId", taskId);
        }
    }

    /**
     * Adds user-specific context to the MDC for logging.
     *
     * @param userId The ID of the user performing the action
     */
    public static void setUserContext(String userId) {
        if (userId != null) {
            MDC.put("userId", userId);
        }
    }

    /**
     * Clears the task context from MDC.
     */
    public static void clearTaskContext() {
        MDC.remove("taskId");
    }

    /**
     * Clears the user context from MDC.
     */
    public static void clearUserContext() {
        MDC.remove("userId");
    }

    /**
     * Logs an operation with structured metadata.
     *
     * @param logger   The SLF4J logger instance
     * @param message  The log message
     * @param metadata Additional metadata to include in the log
     */
    public static void logOperation(Logger logger, String message, Map<String, Object> metadata) {
        // Add all metadata to MDC temporarily
        if (metadata != null) {
            metadata.forEach((key, value) -> {
                if (value != null) {
                    MDC.put(key, value.toString());
                }
            });
        }

        try {
            // Log the message
            logger.info(message);
        } finally {
            // Clean up temporary MDC entries
            if (metadata != null) {
                metadata.keySet().forEach(MDC::remove);
            }
        }
    }

    /**
     * Executes a block of code with task context, ensuring context is cleaned up.
     *
     * @param taskId The task ID to set in the context
     * @param runnable The code to execute with the task context
     */
    public static void withTaskContext(String taskId, Runnable runnable) {
        try {
            setTaskContext(taskId);
            runnable.run();
        } finally {
            clearTaskContext();
        }
    }

    /**
     * Executes a block of code with user context, ensuring context is cleaned up.
     *
     * @param userId The user ID to set in the context
     * @param runnable The code to execute with the user context
     */
    public static void withUserContext(String userId, Runnable runnable) {
        try {
            setUserContext(userId);
            runnable.run();
        } finally {
            clearUserContext();
        }
    }
}
