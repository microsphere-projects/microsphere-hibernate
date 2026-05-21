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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * JPA entity representing a user's profile, mapped to the {@code t_user_profile} table.
 * Holds additional personal information (e.g. ID card number) for a {@link User}.
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
 *   // Persisting the user will cascade to the profile
 *   session.persist(user);
 *   UserProfile loaded = session.get(UserProfile.class, profile.getId());
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see User
 * @since 1.0.0
 */
@Entity
@Table(name = "t_user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String idCard;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Returns the surrogate primary key of this user profile.
     *
     * @return the id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the surrogate primary key of this user profile.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the ID card number stored in this profile.
     *
     * @return the ID card number
     */
    public String getIdCard() {
        return idCard;
    }

    /**
     * Sets the ID card number for this profile.
     *
     * @param idCard the ID card number
     */
    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    /**
     * Returns the {@link User} that owns this profile.
     *
     * @return the owning user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the {@link User} that owns this profile.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserProfile that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(idCard, that.idCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idCard);
    }
}
