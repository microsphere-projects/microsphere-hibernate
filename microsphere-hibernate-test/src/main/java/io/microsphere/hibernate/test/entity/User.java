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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * JPA entity representing a user, mapped to the {@code t_user} table.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   User user = new User();
 *   user.setName("Tom");
 *
 *   UserProfile profile = new UserProfile();
 *   profile.setIdCard("ID123456");
 *   profile.setUser(user);
 *   user.setProfile(profile);
 *
 *   Order order = new Order();
 *   order.setOrderNo("ORD001");
 *   user.addOrder(order);
 *
 *   // Persist via Hibernate Session
 *   session.persist(user);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see UserProfile
 * @see Order
 * @since 1.0.0
 */
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @OneToOne(mappedBy = "user", cascade = ALL, fetch = LAZY)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = ALL, fetch = LAZY)
    private List<Order> orders = new ArrayList<>();

    /**
     * Adds the given {@link Order} to this user's order list and sets the back-reference
     * on the order so that both sides of the bidirectional relationship are kept in sync.
     *
     * @param order the {@link Order} to associate with this user
     */
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    /**
     * Returns the surrogate primary key of this user.
     *
     * @return the user id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the surrogate primary key of this user.
     *
     * @param id the user id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the display name of this user.
     *
     * @return the user name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of this user.
     *
     * @param name the user name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the {@link UserProfile} associated with this user, or {@code null} if none.
     *
     * @return the user profile
     */
    public UserProfile getProfile() {
        return profile;
    }

    /**
     * Sets the {@link UserProfile} associated with this user.
     *
     * @param profile the user profile to associate
     */
    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    /**
     * Returns the list of {@link Order}s belonging to this user.
     *
     * @return the list of orders (never {@code null})
     */
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Replaces the entire list of {@link Order}s for this user.
     *
     * @param orders the new list of orders
     */
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user1)) return false;
        return Objects.equals(id, user1.id)
                && Objects.equals(name, user1.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
