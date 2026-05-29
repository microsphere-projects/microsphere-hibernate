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

package io.microsphere.hibernate.entity;

import org.hibernate.event.internal.DefaultLoadEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hibernate.event.service.spi.DuplicationStrategy.Action.KEEP_ORIGINAL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EntityCallbackListenerDuplicationStrategy} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EntityCallbackListenerDuplicationStrategy
 * @since 1.0.0
 */
class EntityCallbackListenerDuplicationStrategyTest {

    private EntityCallbackListenerDuplicationStrategy strategy;

    @BeforeEach
    void setUp() {
        this.strategy = new EntityCallbackListenerDuplicationStrategy();
    }

    @Test
    void testAreMatch() {
        EntityCallbackListener listener = new EntityCallbackListener();
        assertTrue(strategy.areMatch(listener, listener));

        assertFalse(strategy.areMatch(listener, new DefaultLoadEventListener()));
        assertFalse(strategy.areMatch(new DefaultLoadEventListener(), new DefaultLoadEventListener()));
    }

    @Test
    void testGetAction() {
        assertSame(KEEP_ORIGINAL, strategy.getAction());
    }
}
