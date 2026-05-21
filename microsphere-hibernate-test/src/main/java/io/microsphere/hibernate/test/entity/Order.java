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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * JPA entity representing a customer order, mapped to the {@code t_order} table.
 * An order belongs to a {@link User} and may contain multiple {@link Product}s via a
 * many-to-many join table ({@code t_order_product}).
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   Order order = new Order();
 *   order.setOrderNo("ORD001");
 *
 *   Product product = new Product();
 *   product.setProductName("Laptop");
 *   order.getProducts().add(product);
 *
 *   user.addOrder(order);
 *   session.persist(user); // cascades to order and products
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see User
 * @see Product
 * @since 1.0.0
 */
@Entity
@Table(name = "t_order")
public class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String orderNo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "t_order_product",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    /**
     * Returns the surrogate primary key of this order.
     *
     * @return the order id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the surrogate primary key of this order.
     *
     * @param id the order id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the business order number (e.g. {@code "ORD001"}).
     *
     * @return the order number
     */
    public String getOrderNo() {
        return orderNo;
    }

    /**
     * Sets the business order number.
     *
     * @param orderNo the order number
     */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    /**
     * Returns the {@link User} that placed this order.
     *
     * @return the owning user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the {@link User} that placed this order.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the set of {@link Product}s associated with this order.
     *
     * @return the products (never {@code null})
     */
    public Set<Product> getProducts() {
        return products;
    }

    /**
     * Replaces the set of {@link Product}s for this order.
     *
     * @param products the new set of products
     */
    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;

        return Objects.equals(id, order.id) && Objects.equals(orderNo, order.orderNo);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(orderNo);
        return result;
    }
}
