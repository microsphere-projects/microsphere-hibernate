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

import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.type.Type;

import java.util.List;

import static io.microsphere.collection.ListUtils.forEach;

/**
 * Composite {@link EntityCallback}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   EntityCallback loggingCallback = new LoggingEntityCallback();
 *   EntityCallback auditCallback = new AuditEntityCallback();
 *   List<EntityCallback> callbacks = Arrays.asList(loggingCallback, auditCallback);
 *   CompositeEntityCallback composite = new CompositeEntityCallback(callbacks);
 *
 *   // The composite will invoke both callbacks in order
 *   composite.onPersist(myEntity, "MyEntity");
 *   composite.onLoad(entityId, "MyEntity", myEntity, LockMode.NONE,
 *                    false, null, null, LoadType.GET);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EntityCallback
 * @since 1.0.0
 */
public class CompositeEntityCallback implements EntityCallback {

    private final List<EntityCallback> callbacks;

    /**
     * Creates a new {@link CompositeEntityCallback} backed by the given list of callbacks.
     * Each callback will be invoked in the order it appears in the list.
     *
     * @param callbacks the ordered list of {@link EntityCallback} instances to delegate to
     */
    public CompositeEntityCallback(List<EntityCallback> callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch, Object result, Boolean readOnly, LoadEventListener.LoadType loadType) {
        forEach(this.callbacks, callback -> callback.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, readOnly, loadType));
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        forEach(this.callbacks, callback -> callback.onPersist(entity, entityName));
    }

    @Override
    public void onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates) {
        forEach(this.callbacks, callback -> callback.onDelete(entity, cascadeDeleteEnabled, orphanRemovalBeforeUpdates));
    }

    @Override
    public void onMerge(Object original, Object id, Object entity, String entityName, Object result) {
        forEach(this.callbacks, callback -> callback.onMerge(original, id, entity, entityName, result));
    }

    @Override
    public void onReplicate(Object entity, ReplicationMode replicationMode, String entityName) {
        forEach(this.callbacks, callback -> callback.onReplicate(entity, replicationMode, entityName));
    }

    @Override
    public void onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        forEach(this.callbacks, callback -> callback.onFlushDirty(entity, id, currentState, previousState, propertyNames, types));
    }

    @Override
    public void onEvict(Object entity) {
        forEach(this.callbacks, callback -> callback.onEvict(entity));
    }

    @Override
    public void onLock(Object entity, LockMode lockMode, String entityName) {
        forEach(this.callbacks, callback -> callback.onLock(entity, lockMode, entityName));
    }

    @Override
    public void onRefresh(Object entity, String entityName, LockMode lockMode) {
        forEach(this.callbacks, callback -> callback.onRefresh(entity, entityName, lockMode));
    }

    @Override
    public void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        forEach(this.callbacks, callback -> callback.onPreLoad(entity, id, state, propertyNames, types));
    }

    @Override
    public void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types) {
        forEach(this.callbacks, callback -> callback.onPostLoad(entity, id, propertyNames, types));
    }

    @Override
    public void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        forEach(this.callbacks, callback -> callback.onPreDelete(entity, id, state, propertyNames, types));
    }

    @Override
    public void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        forEach(this.callbacks, callback -> callback.onPostDelete(entity, id, state, propertyNames, types));
    }

    @Override
    public void onPreRecreateCollection(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPreRecreateCollection(collection, key));
    }

    @Override
    public void onPostRecreateCollection(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPostRecreateCollection(collection, key));
    }

    @Override
    public void onPreCollectionRemove(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPreCollectionRemove(collection, key));
    }

    @Override
    public void onPostRemoveCollection(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPostRemoveCollection(collection, key));
    }

    @Override
    public void onPreUpdateCollection(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPreUpdateCollection(collection, key));
    }

    @Override
    public void onPostUpdateCollection(Object collection, Object key) {
        forEach(this.callbacks, callback -> callback.onPostUpdateCollection(collection, key));
    }

    @Override
    public void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        forEach(this.callbacks, callback -> callback.onPreInsert(entity, id, state, propertyNames, propertyTypes));
    }

    @Override
    public void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        forEach(this.callbacks, callback -> callback.onPostInsert(entity, id, state, propertyNames, propertyTypes));
    }

    @Override
    public void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        forEach(this.callbacks, callback -> callback.onPreUpdate(entity, id, state, propertyNames, propertyTypes));
    }

    @Override
    public void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        forEach(this.callbacks, callback -> callback.onPostUpdate(entity, id, state, propertyNames, propertyTypes));
    }
}
