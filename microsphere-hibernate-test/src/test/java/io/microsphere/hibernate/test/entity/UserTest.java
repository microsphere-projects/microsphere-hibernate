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

package io.microsphere.hibernate.test.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void testDefaultConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getProfile());
        assertNotNull(user.getOrders());
        assertTrue(user.getOrders().isEmpty());
    }

    @Test
    void testGetterSetter() {
        User user = new User();

        // Test ID
        user.setId(1L);
        assertEquals(1L, user.getId());

        // Test Name
        user.setName("John Doe");
        assertEquals("John Doe", user.getName());

        // Test Profile
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        assertEquals(profile, user.getProfile());

        // Test Orders
        Order order = new Order();
        user.addOrder(order);
        assertEquals(1, user.getOrders().size());
        assertEquals(user, order.getUser());
    }

    @Test
    void testAddOrder() {
        User user = new User();
        Order order1 = new Order();
        Order order2 = new Order();

        user.addOrder(order1);
        user.addOrder(order2);

        assertEquals(2, user.getOrders().size());
        assertEquals(user, order1.getUser());
        assertEquals(user, order2.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = createUser(1L, "John");
        User user2 = createUser(1L, "John");
        User user3 = createUser(2L, "Jane");
        User user4 = null;
        Object differentType = "Not a User";

        // Reflexive
        assertEquals(user1, user1);

        // Symmetric
        assertEquals(user1, user2);
        assertEquals(user2, user1);

        // Transitive
        User user5 = createUser(1L, "John");
        assertEquals(user1, user2);
        assertEquals(user2, user5);
        assertEquals(user1, user5);

        // Consistent
        assertEquals(user1, user2);
        assertEquals(user1, user2);

        // Null check
        assertNotEquals(user1, user4);

        // Different type
        assertNotEquals(user1, differentType);

        // Different id
        assertNotEquals(user1, user3);

        // HashCode consistency
        assertEquals(user1.hashCode(), user2.hashCode());

        // HashCode contract
        User user6 = createUser(null, "Test");
        User user7 = createUser(null, "Test");
        assertEquals(user6.hashCode(), user7.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        User user1 = new User();
        User user2 = new User();

        // Both null fields should be equal
        assertEquals(user1, user2);

        // One has id, other doesn't
        user1.setId(1L);
        assertNotEquals(user1, user2);

        // Reset and test name
        user1 = new User();
        user2 = new User();
        user1.setName("Test");
        assertNotEquals(user1, user2);
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }
}
