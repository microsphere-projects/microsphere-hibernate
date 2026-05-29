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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Abstract Hibernate Test
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Extend AbstractHibernateTest to write integration tests with a Hibernate SessionFactory
 *   class MyHibernateTest extends AbstractHibernateTest {
 *
 *       @Override
 *       protected Configuration buildConfiguration() {
 *           return new Configuration()
 *               .addAnnotatedClass(User.class)
 *               .setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb");
 *       }
 *
 *       @Test
 *       void testPersist() {
 *           User user = new User();
 *           user.setName("Alice");
 *           Transaction tx = session.beginTransaction();
 *           session.persist(user);
 *           tx.commit();
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
abstract class AbstractHibernateTest {

    protected Configuration configuration;

    protected SessionFactory sessionFactory;

    protected Session session;

    protected StatelessSession statelessSession;

    @BeforeEach
    final void setUp() {
        this.configuration = buildConfiguration();
        customizeConfiguration(this.configuration);

        this.sessionFactory = this.configuration.buildSessionFactory();
        this.session = this.sessionFactory.openSession();
        this.statelessSession = this.sessionFactory.openStatelessSession();
        init();
    }

    @AfterEach
    final void tearDown() {
        destroy();
        this.session.close();
        this.statelessSession.close();
        this.sessionFactory.close();
    }

    /**
     * Builds the Hibernate {@link Configuration} used to create the {@link SessionFactory}.
     * Subclasses must implement this method to supply the appropriate configuration.
     *
     * @return the {@link Configuration} for the test
     */
    protected abstract Configuration buildConfiguration();

    /**
     * Hook for subclasses to further customize the {@link Configuration} after it is built.
     * The default implementation does nothing.
     *
     * @param configuration the {@link Configuration} to customize
     */
    protected void customizeConfiguration(Configuration configuration) {
    }

    /**
     * Hook called after the {@link SessionFactory}, {@link Session}, and {@link StatelessSession}
     * are created. Subclasses may override to perform additional setup before each test.
     */
    protected void init() {
    }

    /**
     * Hook called before sessions are closed at the end of each test.
     * Subclasses may override to perform additional teardown.
     */
    protected void destroy() {
    }
}
