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

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * JPA entity representing a product, mapped to the {@code t_product} table.
 * Products participate in a many-to-many relationship with {@link Order}s via the
 * {@code t_order_product} join table.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   Product product = new Product();
 *   product.setProductName("Laptop");
 *
 *   Order order = new Order();
 *   order.setOrderNo("ORD001");
 *   order.getProducts().add(product);
 *
 *   // Insert via StatelessSession
 *   statelessSession.insert(product);
 *   Product loaded = statelessSession.get(Product.class, product.getId());
 *   assertEquals("Laptop", loaded.getProductName());
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Order
 * @since 1.0.0
 */
@Entity
@Table(name = "t_product")
public class Product {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String productName;

    @ManyToMany(mappedBy = "products", fetch = LAZY)
    private Set<Order> orders = new HashSet<>();

    /**
     * Returns the surrogate primary key of this product.
     *
     * @return the product id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the surrogate primary key of this product.
     *
     * @param id the product id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the human-readable name of this product (e.g. {@code "Laptop"}).
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the human-readable name of this product.
     *
     * @param productName the product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Returns the set of {@link Order}s that include this product.
     *
     * @return the orders (never {@code null})
     */
    public Set<Order> getOrders() {
        return orders;
    }

    /**
     * Replaces the set of {@link Order}s that include this product.
     *
     * @param orders the new set of orders
     */
    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Product product)) return false;

        return Objects.equals(id, product.id)
                && Objects.equals(productName, product.productName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(productName);
        return result;
    }
}
