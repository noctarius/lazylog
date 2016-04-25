/*
 * Copyright (c) 2015-2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.lazylog;

import com.noctarius.tengi.core.exception.LoggerException;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The <tt>LoggerManager</tt> is a utility class to create and cache
 * {@link com.noctarius.tengi.spi.logging.Logger} instances. It will
 * automatically scan the classpath for implementations of
 * {@link com.noctarius.tengi.spi.logging.LoggerFactory} classes using
 * the Java ServiceLoader API and registers each of them, however it
 * is recommended to have only one logger adapter avaialable at a
 * given time.
 */
public final class LoggerManager {

    private static final LoggerManager INSTANCE = new LoggerManager();

    private static final String BINDING_PREFIX_NAME = "::name::";
    private static final String BINDING_PREFIX_CLASS = "::class::";

    private static final Logger NOOP_LOGGER = new NoOpLogger();

    private static final Map<Class<?>, LoggerFactory> LOGGER_FACTORIES;
    private static final LoggerFactory SINGLE_AVAILABLE_FACTORY;

    static {
        ServiceLoader<LoggerFactory> serviceLoader = ServiceLoader.load(LoggerFactory.class);

        Map<Class<?>, LoggerFactory> loggerFactories = new HashMap<>();
        serviceLoader.forEach((loggerFactory) -> loggerFactories.put(loggerFactory.loggerClass(), loggerFactory));
        LOGGER_FACTORIES = loggerFactories;

        LoggerFactory singleAvailableFactory = null;
        if (loggerFactories.size() == 1) {
            singleAvailableFactory = loggerFactories.values().iterator().next();
        }
        SINGLE_AVAILABLE_FACTORY = singleAvailableFactory;
    }

    private final ConcurrentMap<String, Logger> loggerCache = new ConcurrentHashMap<>();

    private LoggerManager() {
    }

    private Logger getLogger0(Class<?> binding, LoggerFactory loggerFactory) {
        String loggerName = buildLoggerName(binding, loggerFactory);
        return loggerCache.computeIfAbsent(loggerName, (key) -> {
            if (loggerFactory != null) {
                return loggerFactory.create(binding);
            }
            return NOOP_LOGGER;
        });
    }

    private Logger getLogger0(String binding, LoggerFactory loggerFactory) {
        String loggerName = buildLoggerName(binding, loggerFactory);
        return loggerCache.computeIfAbsent(loggerName, (key) -> {
            if (loggerFactory != null) {
                return loggerFactory.create(binding);
            }
            return NOOP_LOGGER;
        });
    }

    /**
     * Retrieves a logger bound to the given <tt>binding</tt> type. If a matching binding
     * for another <tt>Logger</tt> instance already exists the cached instance is returned.
     *
     * @param binding the <tt>java.lang.Class</tt> to bind to
     * @return the new <tt>Logger</tt> instance or a cached one if the binding is already created
     */
    public static Logger getLogger(Class<?> binding) {
        if (LOGGER_FACTORIES.size() > 1) {
            throw new LoggerException("Multiple Logger frameworks registered, please choose Logger type explicitly");
        }
        return getLogger(binding, SINGLE_AVAILABLE_FACTORY);
    }

    /**
     * Retrieves a logger bound to the given <tt>binding</tt> string. If a matching binding
     * for another <tt>Logger</tt> instance already exists the cached instance is returned.
     *
     * @param binding the <tt>java.lang.String</tt> to bind to
     * @return the new <tt>Logger</tt> instance or a cached one if the binding is already created
     */
    public static Logger getLogger(String binding) {
        if (LOGGER_FACTORIES.size() > 1) {
            throw new LoggerException("Multiple Logger frameworks registered, please choose Logger type explicitly");
        }
        return getLogger(binding, SINGLE_AVAILABLE_FACTORY);
    }

    /**
     * Retrieves a logger bound to the given <tt>binding</tt> type. If a matching binding
     * for another <tt>Logger</tt> instance already exists the cached instance is returned.
     *
     * @param binding    the <tt>java.lang.Class</tt> to bind to
     * @param loggerType the {@link com.noctarius.tengi.spi.logging.Logger} type if multiple adapters are registered
     * @return the new <tt>Logger</tt> instance or a cached one if the binding is already created
     */
    public static Logger getLogger(Class<?> binding, Class<?> loggerType) {
        LoggerFactory loggerFactory = LOGGER_FACTORIES.get(loggerType);
        if (loggerFactory == null) {
            throw new LoggerException("Requested Logger frameworks is not registered");
        }
        return getLogger(binding, loggerFactory);
    }

    /**
     * Retrieves a logger bound to the given <tt>binding</tt> string. If a matching binding
     * for another <tt>Logger</tt> instance already exists the cached instance is returned.
     *
     * @param binding    the <tt>java.lang.String</tt> to bind to
     * @param loggerType the {@link com.noctarius.tengi.spi.logging.Logger} type if multiple adapters are registered
     * @return the new <tt>Logger</tt> instance or a cached one if the binding is already created
     */
    public static Logger getLogger(String binding, Class<?> loggerType) {
        LoggerFactory loggerFactory = LOGGER_FACTORIES.get(loggerType);
        if (loggerFactory == null) {
            throw new LoggerException("Requested Logger frameworks is not registered");
        }
        return getLogger(binding, loggerFactory);
    }

    private static Logger getLogger(Class<?> binding, LoggerFactory loggerFactory) {
        return INSTANCE.getLogger0(binding, loggerFactory);
    }

    private static Logger getLogger(String binding, LoggerFactory loggerFactory) {
        return INSTANCE.getLogger0(binding, loggerFactory);
    }

    private static String buildLoggerName(Class<?> binding, LoggerFactory loggerFactory) {
        return buildLoggerName(binding.getName(), BINDING_PREFIX_CLASS, loggerFactory);
    }

    private static String buildLoggerName(String binding, LoggerFactory loggerFactory) {
        return buildLoggerName(binding, BINDING_PREFIX_NAME, loggerFactory);
    }

    private static String buildLoggerName(String binding, String prefix, LoggerFactory loggerFactory) {
        String loggerClassName = loggerFactory == null ? "noop" : loggerFactory.loggerClass().getSimpleName();
        return prefix + loggerClassName + "::" + binding;
    }

    private static final class NoOpLogger
            implements Logger {

        @Override
        public void log(Level level, Throwable throwable, String message) {
        }

        @Override
        public void log(Level level, Throwable throwable, String message, Object arg) {
        }

        @Override
        public void log(Level level, Throwable throwable, String message, Object arg1, Object arg2) {
        }

        @Override
        public void log(Level level, Throwable throwable, String message, Object arg1, Object arg2, Object arg3) {
        }

        @Override
        public void log(Level level, Throwable throwable, String message, Object arg1, Object arg2, Object arg3, Object... args) {
        }
    }

}
