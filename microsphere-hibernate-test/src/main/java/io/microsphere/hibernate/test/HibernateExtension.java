/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.hibernate.test;

import io.microsphere.io.scanner.SimpleClassScanner;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import jakarta.persistence.Entity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;

/**
 * JUnit Jupiter extension that manages the Hibernate lifecycle for test classes annotated
 * with {@link HibernateTest}.
 * <p>
 * This extension:
 * <ul>
 *   <li>Builds a {@link Configuration} and {@link SessionFactory} once per test class
 *       (shared by all nested test classes)</li>
 *   <li>Opens a fresh {@link Session} and {@link StatelessSession} before each test method</li>
 *   <li>Closes the {@link Session} and {@link StatelessSession} after each test method</li>
 *   <li>Closes the {@link SessionFactory} after all tests complete</li>
 *   <li>Injects Hibernate components into fields annotated with {@link HibernateRuntime}</li>
 *   <li>Resolves method parameters annotated with {@link HibernateRuntime}</li>
 * </ul>
 * <p>
 * Static fields receive only {@link Configuration} and {@link SessionFactory}.
 * {@link Session} and {@link StatelessSession} are not injected into static fields
 * because they are per-test resources.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HibernateTest
 * @see HibernateRuntime
 * @since 1.0.0
 */
public class HibernateExtension implements BeforeAllCallback, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback, ParameterResolver {

    private static final Logger logger = LoggerFactory.getLogger(HibernateExtension.class);

    private static final Namespace NAMESPACE = Namespace.create(HibernateExtension.class);

    private static final String CONFIGURATION_KEY = "configuration";
    private static final String SESSION_FACTORY_KEY = "sessionFactory";
    private static final String SESSION_KEY = "session";
    private static final String STATELESS_SESSION_KEY = "statelessSession";

    // -------------------------------------------------------------------------
    // BeforeAllCallback
    // -------------------------------------------------------------------------

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Optional<Class<?>> testClassOpt = context.getTestClass();
        if (!testClassOpt.isPresent()) {
            return;
        }

        ExtensionContext hibernateTestContext = findHibernateTestContext(context);
        if (hibernateTestContext == null) {
            return;
        }

        Store store = hibernateTestContext.getStore(NAMESPACE);

        // Build the Configuration and SessionFactory only once (for the outermost class)
        if (store.get(SESSION_FACTORY_KEY) == null) {
            HibernateTest annotation = hibernateTestContext.getRequiredTestClass()
                    .getAnnotation(HibernateTest.class);
            Configuration configuration = buildConfiguration(annotation);
            SessionFactory sessionFactory = configuration.buildSessionFactory();
            store.put(CONFIGURATION_KEY, configuration);
            store.put(SESSION_FACTORY_KEY, sessionFactory);
            logger.debug("Created SessionFactory for test class: {}",
                    hibernateTestContext.getRequiredTestClass().getName());
        }

        // Inject static fields of the current test class (which may be a nested class)
        injectStaticFields(testClassOpt.get(), store);
    }

    // -------------------------------------------------------------------------
    // BeforeEachCallback
    // -------------------------------------------------------------------------

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        ExtensionContext hibernateTestContext = findHibernateTestContext(context);
        if (hibernateTestContext == null) {
            return;
        }

        Store parentStore = hibernateTestContext.getStore(NAMESPACE);
        Configuration configuration = parentStore.get(CONFIGURATION_KEY, Configuration.class);
        SessionFactory sessionFactory = parentStore.get(SESSION_FACTORY_KEY, SessionFactory.class);
        if (sessionFactory == null) {
            return;
        }

        Session session = sessionFactory.openSession();
        StatelessSession statelessSession = sessionFactory.openStatelessSession();

        Store methodStore = context.getStore(NAMESPACE);
        methodStore.put(SESSION_KEY, session);
        methodStore.put(STATELESS_SESSION_KEY, statelessSession);

        // Inject instance fields of the test instance (innermost test class instance)
        Object testInstance = context.getRequiredTestInstance();
        injectInstanceFields(testInstance, configuration, sessionFactory, session, statelessSession);
        logger.debug("Opened Session and StatelessSession for test: {}", context.getDisplayName());
    }

    // -------------------------------------------------------------------------
    // AfterEachCallback
    // -------------------------------------------------------------------------

    @Override
    public void afterEach(ExtensionContext context) {
        Store store = context.getStore(NAMESPACE);

        StatelessSession statelessSession = store.remove(STATELESS_SESSION_KEY, StatelessSession.class);
        Session session = store.remove(SESSION_KEY, Session.class);

        if (statelessSession != null && statelessSession.isOpen()) {
            try {
                statelessSession.close();
            } catch (Exception e) {
                logger.warn("Failed to close StatelessSession", e);
            }
        }
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Failed to close Session", e);
            }
        }
        logger.debug("Closed Session and StatelessSession after test: {}", context.getDisplayName());
    }

    // -------------------------------------------------------------------------
    // AfterAllCallback
    // -------------------------------------------------------------------------

    @Override
    public void afterAll(ExtensionContext context) {
        Optional<Class<?>> testClassOpt = context.getTestClass();
        if (!testClassOpt.isPresent()) {
            return;
        }

        // Only close the SessionFactory when the class that originally created it is done
        if (testClassOpt.get().isAnnotationPresent(HibernateTest.class)) {
            Store store = context.getStore(NAMESPACE);
            SessionFactory sessionFactory = store.remove(SESSION_FACTORY_KEY, SessionFactory.class);
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                try {
                    sessionFactory.close();
                    logger.debug("Closed SessionFactory for test class: {}",
                            testClassOpt.get().getName());
                } catch (Exception e) {
                    logger.warn("Failed to close SessionFactory", e);
                }
            }
            store.remove(CONFIGURATION_KEY);
        }
    }

    // -------------------------------------------------------------------------
    // ParameterResolver
    // -------------------------------------------------------------------------

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) {
        return parameterContext.isAnnotated(HibernateRuntime.class)
                && isSupportedType(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) {
        return resolveComponent(parameterContext.getParameter().getType(), extensionContext);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Walk up the extension context hierarchy to find the context whose test class carries
     * the {@link HibernateTest} annotation.
     */
    private ExtensionContext findHibernateTestContext(ExtensionContext context) {
        ExtensionContext current = context;
        while (current != null) {
            Optional<Class<?>> testClass = current.getTestClass();
            if (testClass.isPresent()
                    && testClass.get().isAnnotationPresent(HibernateTest.class)) {
                return current;
            }
            current = current.getParent().orElse(null);
        }
        return null;
    }

    /**
     * Build a Hibernate {@link Configuration} from the given {@link HibernateTest} annotation.
     */
    private Configuration buildConfiguration(HibernateTest annotation) {
        Configuration configuration = new Configuration();

        for (String property : annotation.properties()) {
            int separatorIndex = property.indexOf('=');
            if (separatorIndex <= 0) {
                logger.warn("Ignoring malformed Hibernate property (expected 'key=value'): '{}'",
                        property);
                continue;
            }
            String key = property.substring(0, separatorIndex).trim();
            String value = property.substring(separatorIndex + 1).trim();
            configuration.setProperty(key, value);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String packageName : annotation.scanEntityPackages()) {
            Set<Class<?>> classes = SimpleClassScanner.INSTANCE.scan(classLoader, packageName, true, true);
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Entity.class)) {
                    configuration.addAnnotatedClass(clazz);
                    logger.debug("Registered entity class: {}", clazz.getName());
                }
            }
        }

        return configuration;
    }

    /**
     * Inject static fields of {@code testClass} that are annotated with {@link HibernateRuntime}.
     * <p>
     * Only {@link Configuration} and {@link SessionFactory} are injected into static fields.
     * {@link Session} and {@link StatelessSession} are per-test resources and must not be
     * stored in static fields.
     */
    private void injectStaticFields(Class<?> testClass, Store store)
            throws IllegalAccessException {
        Configuration configuration = store.get(CONFIGURATION_KEY, Configuration.class);
        SessionFactory sessionFactory = store.get(SESSION_FACTORY_KEY, SessionFactory.class);

        for (Field field : getAllDeclaredFields(testClass)) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.isAnnotationPresent(HibernateRuntime.class)) {
                continue;
            }
            Object value = resolveValueForType(field.getType(), configuration, sessionFactory,
                    null, null);
            if (value != null) {
                field.setAccessible(true);
                field.set(null, value);
            }
        }
    }

    /**
     * Inject non-static fields of the given test instance that are annotated with
     * {@link HibernateRuntime}.
     */
    private void injectInstanceFields(Object testInstance, Configuration configuration,
                                      SessionFactory sessionFactory, Session session,
                                      StatelessSession statelessSession)
            throws IllegalAccessException {
        for (Field field : getAllDeclaredFields(testInstance.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!field.isAnnotationPresent(HibernateRuntime.class)) {
                continue;
            }
            Object value = resolveValueForType(field.getType(), configuration, sessionFactory,
                    session, statelessSession);
            if (value != null) {
                field.setAccessible(true);
                field.set(testInstance, value);
            }
        }
    }

    /**
     * Resolve the Hibernate component to inject for the given {@code type}.
     *
     * @param type             the requested type
     * @param configuration    the current Configuration (may be {@code null} for static context)
     * @param sessionFactory   the current SessionFactory (may be {@code null} for static context)
     * @param session          the current Session ({@code null} in static context)
     * @param statelessSession the current StatelessSession ({@code null} in static context)
     * @return the resolved component, or {@code null} if not applicable
     */
    private Object resolveValueForType(Class<?> type, Configuration configuration,
                                       SessionFactory sessionFactory, Session session,
                                       StatelessSession statelessSession) {
        if (type == Configuration.class) {
            return configuration;
        } else if (type == SessionFactory.class) {
            return sessionFactory;
        } else if (type == Session.class) {
            return session;
        } else if (type == StatelessSession.class) {
            return statelessSession;
        }
        return null;
    }

    /**
     * Resolve a Hibernate component for a method parameter, reading Session / StatelessSession
     * from the method-level store.
     */
    private Object resolveComponent(Class<?> type, ExtensionContext context) {
        ExtensionContext hibernateTestContext = findHibernateTestContext(context);
        if (hibernateTestContext == null) {
            return null;
        }

        Store parentStore = hibernateTestContext.getStore(NAMESPACE);
        Configuration configuration = parentStore.get(CONFIGURATION_KEY, Configuration.class);
        SessionFactory sessionFactory = parentStore.get(SESSION_FACTORY_KEY, SessionFactory.class);

        if (type == Configuration.class) {
            return configuration;
        } else if (type == SessionFactory.class) {
            return sessionFactory;
        } else {
            // Session and StatelessSession are stored in the method-level store
            Store methodStore = context.getStore(NAMESPACE);
            if (type == Session.class) {
                return methodStore.get(SESSION_KEY, Session.class);
            } else if (type == StatelessSession.class) {
                return methodStore.get(STATELESS_SESSION_KEY, StatelessSession.class);
            }
        }
        return null;
    }

    private boolean isSupportedType(Class<?> type) {
        return type == Configuration.class
                || type == SessionFactory.class
                || type == Session.class
                || type == StatelessSession.class;
    }

    /**
     * Collect all declared fields from the given class and all its superclasses up to
     * (but not including) {@link Object}.
     */
    private java.util.List<Field> getAllDeclaredFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(java.util.Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }
}
