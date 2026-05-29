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

import org.hibernate.Interceptor;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.EvictEventListener;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEventListener;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.event.spi.LoadEventListener.LoadType;
import org.hibernate.event.spi.LockEventListener;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreCollectionRecreateEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.event.spi.RefreshEventListener;
import org.hibernate.event.spi.ReplicateEventListener;
import org.hibernate.type.Type;

import java.util.ServiceLoader;

/**
 * The lifecycle callback interface for entity.
 * The implementation class will be loaded by {@link ServiceLoader Java Services Loader}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Implement a custom EntityCallback registered via
 *   // META-INF/services/io.microsphere.hibernate.entity.EntityCallback
 *   public class LoggingEntityCallback implements EntityCallback {
 *
 *       @Override
 *       public void onPersist(Object entity, String entityName) {
 *           System.out.println("Persisting: " + entityName + " -> " + entity);
 *       }
 *
 *       @Override
 *       public void onLoad(Object id, String entityClassName, Object entity,
 *                          LockMode lockMode, boolean isAssociationFetch,
 *                          Object result, Boolean readOnly, LoadType loadType) {
 *           System.out.println("Loading: " + entityClassName + " id=" + id);
 *       }
 *
 *       @Override
 *       public void onPreDelete(Object entity, Object id, Object[] state,
 *                               String[] propertyNames, Type[] types) {
 *           System.out.println("About to delete: " + entity);
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoadEventListener
 * @see PersistEventListener
 * @see MergeEventListener
 * @see DeleteEventListener
 * @see ReplicateEventListener
 * @see FlushEntityEventListener
 * @see EvictEventListener
 * @see LockEventListener
 * @see RefreshEventListener
 * @see PreLoadEventListener
 * @see PostLoadEventListener
 * @see PreDeleteEventListener
 * @see PostDeleteEventListener
 * @see PreUpdateEventListener
 * @see PostUpdateEventListener
 * @see PreInsertEventListener
 * @see PostInsertEventListener
 * @see PreCollectionRecreateEventListener
 * @see PostCollectionRecreateEventListener
 * @see PreCollectionRemoveEventListener
 * @see PostCollectionRemoveEventListener
 * @see PreCollectionUpdateEventListener
 * @see PostCollectionUpdateEventListener
 * @see EventType
 * @see Interceptor
 * @since 1.0.0
 */
public interface EntityCallback {

    /**
     * Called when an object is loaded by a stateful session.
     *
     * @param id                 The identifier value being loaded
     * @param entityClassName    The class name of the entity
     * @param entity             The entity instance being loaded
     * @param lockMode           {@link LockMode}
     * @param isAssociationFetch
     * @param result
     * @param readOnly
     * @param loadType
     * @see Session#find(Class, Object)
     * @see LoadEventListener
     */
    default void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, Boolean readOnly, LoadType loadType) {
    }

    /**
     * Called when an object is made persistent by a stateful session.
     *
     * @param entity     The entity instance whose state is being inserted
     * @param entityName The name of the entity
     * @see Session#persist(Object)
     * @see PersistEventListener
     */
    default void onPersist(Object entity, String entityName) {
    }

    /**
     * Called when an object is removed by a stateful session.
     *
     * @param entity                     The entity instance being deleted
     * @param cascadeDeleteEnabled
     * @param orphanRemovalBeforeUpdates
     * @see Session#remove(Object)
     * @see DeleteEventListener
     */
    default void onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates) {
    }

    /**
     * Called when an object is updated by a stateful session.
     *
     * @param original   The original entity
     * @param id         The identifier of the entity
     * @param entity     The entity instance being updated
     * @param entityName The name of the entity
     * @param result     The result
     * @see Session#merge(Object)
     * @see MergeEventListener
     */
    default void onMerge(Object original, Object id, Object entity, String entityName, Object result) {
    }

    /**
     * Called when an object is replicated by a stateful session.
     *
     * @param entity          The entity instance being updated
     * @param replicationMode {@link ReplicationMode}
     * @param entityName      The name of the entity
     * @see Session#replicate(Object, ReplicationMode)
     * @see MergeEventListener
     */
    default void onReplicate(Object entity, ReplicationMode replicationMode, String entityName) {
    }

    /**
     * Called when an object is detected to be dirty, during a flush.
     * <p>
     * It is not recommended that the interceptor modify the {@code currentState} and {@code previousState}.
     *
     * @param entity        The entity instance detected as being dirty and being flushed
     * @param id            The identifier of the entity
     * @param currentState  The entity's current state
     * @param previousState The entity's previous (load time) state.
     * @param propertyNames The names of the entity properties
     * @param types         The types of the entity properties
     * @see Session#flush()
     * @see FlushEventListener
     */
    default void onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
    }

    /**
     * Called when an object is evicted by a stateful session.
     *
     * @param entity The entity instance being evicted
     * @see Session#evict(Object)
     * @see EvictEventListener
     */
    default void onEvict(Object entity) {
    }

    /**
     * Called when an object is locked by a stateful session.
     *
     * @param entity     The entity instance being locked
     * @param lockMode   {@link LockMode}
     * @param entityName the name of the entity
     * @see Session#lock(Object, org.hibernate.LockMode)
     * @see LockEventListener
     */
    default void onLock(Object entity, LockMode lockMode, String entityName) {
    }

    /**
     * Called when an object is refreshed by a stateful session.
     *
     * @param entity     The entity instance being refreshed
     * @param entityName The identifier of the entity
     * @param lockMode
     * @see Session#refresh(Object)
     * @see RefreshEventListener
     */
    default void onRefresh(Object entity, String entityName, LockMode lockMode) {
    }

    /**
     * Called before injecting property values into a newly loaded entity instance.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being loaded
     * @param id            The identifier value being loaded
     * @param state         The entity state (which will be pushed into the entity instance)
     * @param propertyNames The names of the entity properties, corresponding to the {@code state}.
     * @param types         The types of the entity properties, corresponding to the {@code state}.
     * @see StatelessSession#get(String, Object)
     * @see StatelessSession#get(Class, Object)
     * @see PreLoadEventListener
     */
    default void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
    }

    /**
     * Called just after an object is initialized.
     *
     * @param entity        The entity instance being loaded
     * @param id            The identifier value being loaded
     * @param propertyNames The names of the entity properties, corresponding to the {@code state}.
     * @param types         The types of the entity properties, corresponding to the {@code state}.
     * @see PostLoadEventListener
     */
    default void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types) {
    }

    /**
     * Called before deleting an item from the datastore.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The state of the entity
     * @param propertyNames The names of the entity properties.
     * @param types         The types of the entity properties
     * @see StatelessSession#delete(Object)
     * @see PreDeleteEventListener
     */
    default void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
    }

    /**
     * Called after deleting an item from the datastore.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The state of the entity
     * @param propertyNames The names of the entity properties.
     * @param types         The types of the entity properties
     * @see StatelessSession#delete(Object)
     * @see PostDeleteEventListener
     */
    default void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
    }

    /**
     * Called before a record is inserted by a {@link StatelessSession}.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The entity state
     * @param propertyNames The names of the entity properties.
     * @param propertyTypes The types of the entity properties
     * @see StatelessSession#insert(Object)
     * @see PreInsertEventListener
     */
    default void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    }

    /**
     * Called after a record is inserted by a {@link StatelessSession}.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The entity state
     * @param propertyNames The names of the entity properties.
     * @param propertyTypes The types of the entity properties
     * @see StatelessSession#insert(Object)
     * @see PostInsertEventListener
     */
    default void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    }

    /**
     * Called before a record is updated by a {@link StatelessSession}.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The entity state
     * @param propertyNames The names of the entity properties
     * @param propertyTypes The types of the entity properties
     * @see StatelessSession#update(Object)
     * @see PreUpdateEventListener
     */
    default void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    }

    /**
     * Called after a record is updated by a {@link StatelessSession}.
     * <p>
     * It is not recommended that the interceptor modify the {@code state}.
     *
     * @param entity        The entity instance being deleted
     * @param id            The identifier of the entity
     * @param state         The entity state
     * @param propertyNames The names of the entity properties
     * @param propertyTypes The types of the entity properties
     * @see StatelessSession#update(Object)
     * @see PostUpdateEventListener
     */
    default void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
    }

    /**
     * Called before a collection is (re)created.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPreRecreateCollection(Object collection, Object key) {
    }

    /**
     * Called after a collection is (re)created.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPostRecreateCollection(Object collection, Object key) {
    }

    /**
     * Called before a collection is deleted.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPreCollectionRemove(Object collection, Object key) {
    }

    /**
     * Called after a collection is deleted.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPostRemoveCollection(Object collection, Object key) {
    }

    /**
     * Called before a collection is updated.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPreUpdateCollection(Object collection, Object key) {
    }

    /**
     * Called after a collection is (re)created.
     *
     * @param collection The collection instance.
     * @param key        The collection key value.
     */
    default void onPostUpdateCollection(Object collection, Object key) {
    }
}
