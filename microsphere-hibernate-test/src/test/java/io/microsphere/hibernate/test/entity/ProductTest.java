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

class ProductTest {

    @Test
    void testDefaultConstructor() {
        Product product = new Product();
        assertNotNull(product);
        assertNull(product.getId());
        assertNull(product.getProductName());
        assertNotNull(product.getOrders());
        assertTrue(product.getOrders().isEmpty());
    }

    @Test
    void testGetterSetter() {
        Product product = new Product();

        // Test ID
        product.setId(1L);
        assertEquals(1L, product.getId());

        // Test ProductName
        product.setProductName("Laptop");
        assertEquals("Laptop", product.getProductName());

        // Test Orders
        Set<Order> orders = new HashSet<>();
        Order order = new Order();
        orders.add(order);
        product.setOrders(orders);
        assertEquals(1, product.getOrders().size());
    }

    @Test
    void testEqualsAndHashCode() {
        Product product1 = createProduct(1L, "Laptop");
        Product product2 = createProduct(1L, "Laptop");
        Product product3 = createProduct(2L, "Phone");
        Product product4 = null;
        Object differentType = "Not a Product";

        // Reflexive
        assertEquals(product1, product1);

        // Symmetric
        assertEquals(product1, product2);
        assertEquals(product2, product1);

        // Different id
        assertNotEquals(product1, product3);

        // Null check
        assertNotEquals(product1, product4);

        // Different type
        assertNotEquals(product1, differentType);

        // HashCode consistency
        assertEquals(product1.hashCode(), product2.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        Product product1 = new Product();
        Product product2 = new Product();

        // Both null fields should be equal
        assertEquals(product1, product2);

        // One has id, other doesn't
        product1.setId(1L);
        assertNotEquals(product1, product2);

        // Reset and test productName
        product1 = new Product();
        product2 = new Product();
        product1.setProductName("TEST");
        assertNotEquals(product1, product2);
    }

    private Product createProduct(Long id, String productName) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(productName);
        return product;
    }
}
