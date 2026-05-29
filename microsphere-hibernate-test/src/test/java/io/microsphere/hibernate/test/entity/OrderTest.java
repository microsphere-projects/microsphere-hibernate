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

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderTest {

    @Test
    void testDefaultConstructor() {
        Order order = new Order();
        assertNotNull(order);
        assertNull(order.getId());
        assertNull(order.getOrderNo());
        assertNull(order.getUser());
        assertNotNull(order.getProducts());
        assertTrue(order.getProducts().isEmpty());
    }

    @Test
    void testGetterSetter() {
        Order order = new Order();

        // Test ID
        order.setId(1L);
        assertEquals(1L, order.getId());

        // Test OrderNo
        order.setOrderNo("ORD-001");
        assertEquals("ORD-001", order.getOrderNo());

        // Test User
        User user = new User();
        order.setUser(user);
        assertEquals(user, order.getUser());

        // Test Products
        Set<Product> products = new HashSet<>();
        Product product = new Product();
        products.add(product);
        order.setProducts(products);
        assertEquals(1, order.getProducts().size());
    }

    @Test
    void testEqualsAndHashCode() {
        Order order1 = createOrder(1L, "ORD-001");
        Order order2 = createOrder(1L, "ORD-001");
        Order order3 = createOrder(2L, "ORD-002");
        Order order4 = null;
        Object differentType = "Not an Order";

        // Reflexive
        assertEquals(order1, order1);

        // Symmetric
        assertEquals(order1, order2);
        assertEquals(order2, order1);

        // Different id
        assertNotEquals(order1, order3);

        // Null check
        assertNotEquals(order1, order4);

        // Different type
        assertNotEquals(order1, differentType);

        // HashCode consistency
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        Order order1 = new Order();
        Order order2 = new Order();

        // Both null fields should be equal
        assertEquals(order1, order2);

        // One has id, other doesn't
        order1.setId(1L);
        assertNotEquals(order1, order2);

        // Reset and test orderNo
        order1 = new Order();
        order2 = new Order();
        order1.setOrderNo("TEST");
        assertNotEquals(order1, order2);
    }

    private Order createOrder(Long id, String orderNo) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNo(orderNo);
        return order;
    }
}
