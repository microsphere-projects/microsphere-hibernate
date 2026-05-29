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
import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

    @Test
    void testDefaultConstructor() {
        UserProfile profile = new UserProfile();
        assertNotNull(profile);
        assertNull(profile.getId());
        assertNull(profile.getIdCard());
        assertNull(profile.getUser());
    }

    @Test
    void testGetterSetter() {
        UserProfile profile = new UserProfile();

        // Test ID
        profile.setId(1L);
        assertEquals(1L, profile.getId());

        // Test IdCard
        profile.setIdCard("ID123456");
        assertEquals("ID123456", profile.getIdCard());

        // Test User
        User user = new User();
        profile.setUser(user);
        assertEquals(user, profile.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        UserProfile profile1 = createProfile(1L, "ID123");
        UserProfile profile2 = createProfile(1L, "ID123");
        UserProfile profile3 = createProfile(2L, "ID456");
        UserProfile profile4 = null;
        Object differentType = "Not a UserProfile";

        // Reflexive
        assertEquals(profile1, profile1);

        // Symmetric
        assertEquals(profile1, profile2);
        assertEquals(profile2, profile1);

        // Different id
        assertNotEquals(profile1, profile3);

        // Null check
        assertNotEquals(profile1, profile4);

        // Different type
        assertNotEquals(profile1, differentType);

        // HashCode consistency
        assertEquals(profile1.hashCode(), profile2.hashCode());
    }

    @Test
    void testEqualsWithNullFields() {
        UserProfile profile1 = new UserProfile();
        UserProfile profile2 = new UserProfile();

        // Both null fields should be equal
        assertEquals(profile1, profile2);

        // One has id, other doesn't
        profile1.setId(1L);
        assertNotEquals(profile1, profile2);

        // Reset and test idCard
        profile1 = new UserProfile();
        profile2 = new UserProfile();
        profile1.setIdCard("TEST");
        assertNotEquals(profile1, profile2);
    }

    private UserProfile createProfile(Long id, String idCard) {
        UserProfile profile = new UserProfile();
        profile.setId(id);
        profile.setIdCard(idCard);
        return profile;
    }
}
