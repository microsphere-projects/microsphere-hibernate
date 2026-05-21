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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration test for {@link HibernateTest} and {@link HibernateRuntime} annotations.
 * <p>
 * Verifies that:
 * <ul>
 *   <li>Instance fields annotated with {@link HibernateRuntime} are injected for all four
 *       supported Hibernate types.</li>
 *   <li>Static fields annotated with {@link HibernateRuntime} receive {@link Configuration}
 *       and {@link SessionFactory}, but {@link Session} and {@link StatelessSession} remain
 *       {@code null} (because they are per-test resources).</li>
 *   <li>Method parameters annotated with {@link HibernateRuntime} are resolved correctly
 *       for all four supported types.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HibernateTest
 * @see HibernateRuntime
 * @see HibernateExtension
 * @since 1.0.0
 */
@HibernateTest(
        properties = {
                "hibernate.connection.driver_class=org.h2.Driver",
                "hibernate.connection.url=jdbc:h2:mem:hibernate_annotation_test_db;DB_CLOSE_DELAY=-1",
                "hibernate.connection.username=sa",
                "hibernate.connection.password=",
                "hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "hibernate.hbm2ddl.auto=update"
        },
        scanEntityPackages = {"io.microsphere.hibernate.test.entity"}
)
class HibernateAnnotationTest {

    @Nested
    class FieldTest {

        @HibernateRuntime
        private Configuration configuration;

        @HibernateRuntime
        private SessionFactory sessionFactory;

        @HibernateRuntime
        private Session session;

        @HibernateRuntime
        private StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }

    @Nested
    class StaticFieldTest {

        @HibernateRuntime
        private static Configuration configuration;

        @HibernateRuntime
        private static SessionFactory sessionFactory;

        @HibernateRuntime
        private static Session session;

        @HibernateRuntime
        private static StatelessSession statelessSession;

        @Test
        void test() {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            // Session and StatelessSession must NOT be injected into static fields
            assertNull(session);
            assertNull(statelessSession);
        }
    }

    @Nested
    class ParameterTest {

        @Test
        void test(@HibernateRuntime Configuration configuration,
                  @HibernateRuntime SessionFactory sessionFactory,
                  @HibernateRuntime Session session,
                  @HibernateRuntime StatelessSession statelessSession) {
            assertNotNull(configuration);
            assertNotNull(sessionFactory);
            assertNotNull(session);
            assertNotNull(statelessSession);
        }
    }
}
