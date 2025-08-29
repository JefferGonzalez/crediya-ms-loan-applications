package co.com.pragma.crediya.logger;

import co.com.pragma.crediya.model.logs.gateways.LoggerPort;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Slf4jLoggerAdapter implements LoggerPort {

    private final Logger logger;

    public Slf4jLoggerAdapter(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}