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

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import static org.hibernate.event.spi.EventType.DELETE;
import static org.hibernate.event.spi.EventType.EVICT;
import static org.hibernate.event.spi.EventType.FLUSH_ENTITY;
import static org.hibernate.event.spi.EventType.LOAD;
import static org.hibernate.event.spi.EventType.LOCK;
import static org.hibernate.event.spi.EventType.MERGE;
import static org.hibernate.event.spi.EventType.PERSIST;
import static org.hibernate.event.spi.EventType.POST_COLLECTION_RECREATE;
import static org.hibernate.event.spi.EventType.POST_COLLECTION_REMOVE;
import static org.hibernate.event.spi.EventType.POST_COLLECTION_UPDATE;
import static org.hibernate.event.spi.EventType.POST_DELETE;
import static org.hibernate.event.spi.EventType.POST_INSERT;
import static org.hibernate.event.spi.EventType.POST_LOAD;
import static org.hibernate.event.spi.EventType.POST_UPDATE;
import static org.hibernate.event.spi.EventType.POST_UPSERT;
import static org.hibernate.event.spi.EventType.PRE_COLLECTION_RECREATE;
import static org.hibernate.event.spi.EventType.PRE_COLLECTION_REMOVE;
import static org.hibernate.event.spi.EventType.PRE_COLLECTION_UPDATE;
import static org.hibernate.event.spi.EventType.PRE_DELETE;
import static org.hibernate.event.spi.EventType.PRE_INSERT;
import static org.hibernate.event.spi.EventType.PRE_LOAD;
import static org.hibernate.event.spi.EventType.PRE_UPDATE;
import static org.hibernate.event.spi.EventType.PRE_UPSERT;
import static org.hibernate.event.spi.EventType.REFRESH;
import static org.hibernate.event.spi.EventType.REPLICATE;

/**
 * Hibernate {@link Integrator} for {@link EntityCallback}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Register via META-INF/services/org.hibernate.integrator.spi.Integrator
 *   // (file content: io.microsphere.hibernate.entity.EntittyCallbackIntegrator)
 *
 *   // Or register programmatically when building the SessionFactory:
 *   Configuration configuration = new Configuration()
 *       .addAnnotatedClass(User.class);
 *   SessionFactory sessionFactory = configuration
 *       .buildSessionFactory(
 *           new StandardServiceRegistryBuilder()
 *               .applySettings(configuration.getProperties())
 *               .build()
 *       );
 *
 *   // Once active, all EntityCallback implementations loaded via ServiceLoader
 *   // are notified for Hibernate events (load, persist, delete, merge, etc.)
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Integrator
 * @see DuplicationStrategy
 * @see EntityCallbackListener
 * @since 1.0.0
 */
public class EntittyCallbackIntegrator implements Integrator {

    /**
     * Registers the {@link EntityCallbackListener} for all supported Hibernate event types
     * on the given {@link SessionFactoryImplementor}.
     *
     * @param metadata           the Hibernate metadata
     * @param sessionFactory     the session factory being built
     * @param serviceRegistry    the session factory service registry
     */
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        EventListenerRegistry eventListenerRegistry = sessionFactory.getEventListenerRegistry();

        // Add DuplicationStrategy
        eventListenerRegistry.addDuplicationStrategy(new EntityCallbackListenerDuplicationStrategy());

        // Append EntityCallbackListener
        EntityCallbackListener listener = new EntityCallbackListener();

        eventListenerRegistry.appendListeners(LOAD, listener);

        eventListenerRegistry.appendListeners(PERSIST, listener);

        eventListenerRegistry.appendListeners(MERGE, listener);

        eventListenerRegistry.appendListeners(DELETE, listener);

        eventListenerRegistry.appendListeners(REPLICATE, listener);

        eventListenerRegistry.appendListeners(FLUSH_ENTITY, listener);

        eventListenerRegistry.appendListeners(EVICT, listener);

        eventListenerRegistry.appendListeners(LOCK, listener);

        eventListenerRegistry.appendListeners(REFRESH, listener);

        eventListenerRegistry.appendListeners(PRE_LOAD, listener);
        eventListenerRegistry.appendListeners(POST_LOAD, listener);

        eventListenerRegistry.appendListeners(POST_DELETE, listener);
        eventListenerRegistry.appendListeners(PRE_DELETE, listener);

        eventListenerRegistry.appendListeners(PRE_UPDATE, listener);
        eventListenerRegistry.appendListeners(POST_UPDATE, listener);

        eventListenerRegistry.appendListeners(PRE_INSERT, listener);
        eventListenerRegistry.appendListeners(POST_INSERT, listener);

        eventListenerRegistry.appendListeners(PRE_UPSERT, listener);
        eventListenerRegistry.appendListeners(POST_UPSERT, listener);

        eventListenerRegistry.appendListeners(PRE_COLLECTION_RECREATE, listener);
        eventListenerRegistry.appendListeners(POST_COLLECTION_RECREATE, listener);

        eventListenerRegistry.appendListeners(PRE_COLLECTION_REMOVE, listener);
        eventListenerRegistry.appendListeners(POST_COLLECTION_REMOVE, listener);

        eventListenerRegistry.appendListeners(PRE_COLLECTION_UPDATE, listener);
        eventListenerRegistry.appendListeners(POST_COLLECTION_UPDATE, listener);

    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        integrate(metadata, sessionFactory, (SessionFactoryServiceRegistry) sessionFactory.getServiceRegistry());
    }

    /**
     * Removes previously registered {@link EntityCallbackListener} listeners from the session factory.
     * This implementation is a no-op; all cleanup is handled by the session factory shutdown.
     *
     * @param sessionFactory  the session factory being closed
     * @param serviceRegistry the session factory service registry
     */
    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }
}