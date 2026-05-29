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

import io.microsphere.hibernate.test.entity.Order;
import io.microsphere.hibernate.test.entity.Product;
import io.microsphere.hibernate.test.entity.User;
import io.microsphere.hibernate.test.entity.UserProfile;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.hibernate.LockMode.READ;
import static org.hibernate.ReplicationMode.LATEST_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Abstract Hibernate Test for H2 Database
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Extend to write Hibernate integration tests backed by an in-memory H2 database
 *   class MyEntityTest extends AbstractHibernateH2Test {
 *
 *       @Test
 *       void testPersistAndGet() {
 *           doInTransaction(() -> {
 *               session.persist(user);
 *               User found = session.get(User.class, user.getId());
 *               assertEquals(user, found);
 *           });
 *       }
 *
 *       // Override createUser() to provide a custom User graph for tests
 *       @Override
 *       protected User createUser() {
 *           User user = new User();
 *           user.setName("Alice");
 *           return user;
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractHibernateTest
 * @since 1.0.0
 */
public abstract class AbstractHibernateH2Test extends AbstractHibernateTest {

    protected User user;

    @Override
    protected void init() {
        super.init();
        user = createUser();
    }

    /**
     * Creates a default {@link User} entity graph (with {@link UserProfile}, {@link Order}, and
     * {@link Product}) used as test fixture data. Override to supply a custom graph.
     *
     * @return a new {@link User} instance with associated entities
     */
    protected User createUser() {
        User user = new User();
        user.setName("Tom");

        UserProfile profile = new UserProfile();
        profile.setIdCard("ID123456");
        profile.setUser(user);
        user.setProfile(profile);

        Order order = new Order();
        order.setOrderNo("ORD001");

        Product product = new Product();
        product.setProductName("Laptop");

        order.getProducts().add(product);
        user.addOrder(order);
        return user;
    }

    @Override
    protected Configuration buildConfiguration() {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(UserProfile.class)
                .addAnnotatedClass(Order.class)
                .addAnnotatedClass(Product.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect")
                .setProperty("hibernate.format_sql", "true")
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "update");
        return configuration;
    }

    /**
     * Executes the given action, asserting that it completes without throwing any exception.
     *
     * @param action the action to run
     */
    protected void doInAction(Runnable action) {
        assertDoesNotThrow(action::run);
    }

    @Nested
    @DisplayName("Session Test")
    class SessionTest {

        @Test
        @DisplayName("Test : persist & get")
        void testPersistAndGet() {
            doInTransaction(() -> {
                session.persist(user);
                User user1 = session.get(User.class, user.getId());
                assertEquals(user, user1);

                List<Order> orders = user1.getOrders();
                assertFalse(orders.isEmpty());
                orders.forEach(order -> {
                    Set<Product> products = order.getProducts();
                    assertFalse(products.isEmpty());
                });

                UserProfile profile = user1.getProfile();
                assertEquals(profile, user.getProfile());

            });
        }

        @Test
        @DisplayName("Test : persist & merge")
        void testPersistAndMerge() {
            doInTransaction(() -> {
                session.persist(user);
                user.setName("Merged User");

                User user1 = session.get(User.class, user.getId());
                List<Order> orders = user1.getOrders();
                orders.forEach(order -> {
                    order.setOrderNo("TEST ORDER NO");
                });
                User mergedUser = session.merge(user);
                session.flush();

                orders.forEach(order -> {
                    order.setUser(null);
                    session.merge(order);
                });

                user.getOrders().clear();
                mergedUser = session.merge(user);

                assertEquals(mergedUser, user);
            });
        }

        @Test
        @DisplayName("Test : persist & remove")
        void testPersistAndRemove() {
            doInTransaction(() -> {
                session.persist(user);

                UserProfile profile = user.getProfile();
                user.setProfile(null);
                session.remove(profile);
                session.flush();

                List<Order> orders = user.getOrders();
                orders.forEach(order -> {
                    session.remove(order);
                });
                orders.clear();
                session.flush();

                session.remove(user);
            });
        }

        @Test
        @DisplayName("Test : persist & replicate")
        void testPersistAndReplicate() {
            doInTransaction(() -> {
                session.persist(user);
                session.replicate(user, LATEST_VERSION);
            });
        }

        @Test
        @DisplayName("Test : persist & flush")
        void testPersistAndFlush() {
            doInTransaction(() -> {
                session.persist(user);
                session.flush();
            });
        }

        @Test
        @DisplayName("Test : persist & detach")
        void testPersistAndDetach() {
            doInAction(() -> {
                session.persist(user);
                session.detach(user);
            });
        }

        @Test
        @DisplayName("Test : persist & evict")
        void testPersistAndEvict() {
            doInAction(() -> {
                session.persist(user);
                session.evict(user);
            });
        }

        @Test
        @DisplayName("Test : persist & lock")
        void testPersistAndLock() {
            doInAction(() -> {
                session.persist(user);
                session.lock(user, READ);
            });
        }

        @Test
        @DisplayName("Test : persist & refresh")
        void testPersistAndRefresh() {
            doInTransaction(() -> {
                session.persist(user);
                // refresh
                session.refresh(user);
            });
        }

        @Test
        @DisplayName("Test : persist & load")
        void testPersistAndLoad() {
            doInTransaction(() -> {
                session.persist(user);
                User user1 = session.find(User.class, user.getId());
                assertEquals(user, user1);
            });
        }

        @Test
        @DisplayName("Test : persist & FROM User")
        void testPersistAndGetManagedEntities() {
            doInTransaction(() -> {
                session.persist(user);
                List<User> users = session.createQuery("FROM User", User.class).getResultList();
                assertFalse(users.isEmpty());
            });
        }

        void doInTransaction(Runnable action) {
            assertDoesNotThrow(() -> {
                Transaction transaction = session.beginTransaction();
                action.run();
                transaction.commit();
            });
        }
    }

    @Nested
    @DisplayName("Stateless Session Test")
    class StatelessSessionTest {

        @Test
        @DisplayName("Test : insert & get")
        void testInsertAndGet() {
            doInTransaction(() -> {
                Long userId = (Long) statelessSession.insert(user);
                User user1 = statelessSession.get(User.class, userId);
                assertEquals(user, user1);
            });
        }

        @Test
        @DisplayName("Test : insert & delete")
        void testInsertAndDelete() {
            doInTransaction(() -> {
                Long userId = (Long) statelessSession.insert(user);
                statelessSession.delete(user);
                User user1 = statelessSession.get(User.class, userId);
                assertNull(user1);
            });
        }

        @Test
        @DisplayName("Test : insert & update")
        void testInsertAndUpdate() {
            Long userId = (Long) statelessSession.insert(user);
            user.setName("Updated User");
            statelessSession.update(user);
            User user1 = statelessSession.get(User.class, userId);
            assertEquals(user, user1);
        }

        @Test
        @DisplayName("Test : insert & refresh")
        void testInsertAndRefresh() {
            Long userId = (Long) statelessSession.insert(user);
            user.setName("Refreshed User");
            statelessSession.refresh(user);
            User user1 = statelessSession.get(User.class, userId);
            assertEquals(user, user1);
        }

        @Test
        @DisplayName("Test : insert & fetch")
        void testInsertAndFetch() {
            Long userId = (Long) statelessSession.insert(user);
            statelessSession.fetch(user);
            User user1 = statelessSession.get(User.class, userId);
            assertEquals(user, user1);
        }

        void doInTransaction(Runnable action) {
            assertDoesNotThrow(() -> {
                Transaction transaction = statelessSession.beginTransaction();
                action.run();
                transaction.commit();
            });
        }
    }

}
