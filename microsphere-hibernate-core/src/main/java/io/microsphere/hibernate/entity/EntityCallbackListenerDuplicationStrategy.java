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

import org.hibernate.event.service.spi.DuplicationStrategy;

import java.util.Objects;

import static org.hibernate.event.service.spi.DuplicationStrategy.Action.KEEP_ORIGINAL;

/**
 * {@link DuplicationStrategy} class for {@link EntityCallbackListener}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Used internally by EntittyCallbackIntegrator to prevent duplicate listener registration
 *   EntityCallbackListenerDuplicationStrategy strategy = new EntityCallbackListenerDuplicationStrategy();
 *
 *   EntityCallbackListener listener1 = new EntityCallbackListener();
 *   EntityCallbackListener listener2 = new EntityCallbackListener();
 *   boolean match = strategy.areMatch(listener1, listener2); // true — same class
 *
 *   DuplicationStrategy.Action action = strategy.getAction(); // KEEP_ORIGINAL
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EntityCallbackListener
 * @since 1.0.0
 */
class EntityCallbackListenerDuplicationStrategy implements DuplicationStrategy {

    /**
     * Returns {@code true} if both {@code listener} and {@code original} are instances of
     * {@link EntityCallbackListener}, preventing duplicate registrations.
     *
     * @param listener the candidate listener
     * @param original the already-registered listener
     * @return {@code true} if both are instances of {@link EntityCallbackListener}
     */
    @Override
    public boolean areMatch(Object listener, Object original) {
        Class<?> listenerClass = listener.getClass();
        return Objects.equals(listenerClass, original.getClass())
                && EntityCallbackListener.class.equals(listenerClass);
    }

    /**
     * Returns {@link org.hibernate.event.service.spi.DuplicationStrategy.Action#KEEP_ORIGINAL},
     * meaning the first registered {@link EntityCallbackListener} is retained when a duplicate is detected.
     *
     * @return {@link org.hibernate.event.service.spi.DuplicationStrategy.Action#KEEP_ORIGINAL}
     */
    @Override
    public Action getAction() {
        return KEEP_ORIGINAL;
    }
}
