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

import io.microsphere.logging.Logger;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.type.Type;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link EntityCallback} for Testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EntityCallback
 * @since 1.0.0
 */
public class TestEntityCallback implements EntityCallback {

    private static final Logger logger = getLogger(TestEntityCallback.class);

    @Override
    public void onLoad(Object id, String entityClassName, Object entity, LockMode lockMode, boolean isAssociationFetch,
                       Object result, Boolean readOnly, LoadEventListener.LoadType loadType) {
        logger.trace("onLoad - id : {}, entityClassName : {}, entity : {}, lockMode : {}, isAssociationFetch : {}, result : {}, readOnly : {}, loadType : {}",
                id, entityClassName, entity, lockMode, isAssociationFetch, result, readOnly, loadType);
        EntityCallback.super.onLoad(id, entityClassName, entity, lockMode, isAssociationFetch, result, readOnly, loadType);
    }

    @Override
    public void onPersist(Object entity, String entityName) {
        logger.trace("onPersist - entity : {}, entityName : {}", entity, entityName);
        EntityCallback.super.onPersist(entity, entityName);
    }

    @Override
    public void onDelete(Object entity, boolean cascadeDeleteEnabled, boolean orphanRemovalBeforeUpdates) {
        logger.trace("onDelete - entity : {}, cascadeDeleteEnabled : {}, orphanRemovalBeforeUpdates : {}",
                entity, cascadeDeleteEnabled, orphanRemovalBeforeUpdates);
        EntityCallback.super.onDelete(entity, cascadeDeleteEnabled, orphanRemovalBeforeUpdates);
    }

    @Override
    public void onMerge(Object original, Object id, Object entity, String entityName, Object result) {
        logger.trace("onMerge - original : {}, id : {}, entity : {}, entityName : {}, result : {}",
                original, id, entity, entityName, result);
        EntityCallback.super.onMerge(original, id, entity, entityName, result);
    }

    @Override
    public void onReplicate(Object entity, ReplicationMode replicationMode, String entityName) {
        logger.trace("onReplicate - entity : {}, replicationMode : {}, entityName : {}", entity, replicationMode, entityName);
        EntityCallback.super.onReplicate(entity, replicationMode, entityName);
    }

    @Override
    public void onFlushDirty(Object entity, Object id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        logger.trace("onFlushDirty - entity : {}, id : {}, currentState : {}, previousState : {}, propertyNames : {}, types : {}",
                entity, id, currentState, previousState, propertyNames, types);
        EntityCallback.super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }

    @Override
    public void onEvict(Object entity) {
        logger.trace("onEvict - entity : {}", entity);
        EntityCallback.super.onEvict(entity);
    }

    @Override
    public void onLock(Object entity, LockMode lockMode, String entityName) {
        logger.trace("onLock - entity : {}, lockMode : {}, entityName : {}", entity, lockMode, entityName);
        EntityCallback.super.onLock(entity, lockMode, entityName);
    }

    @Override
    public void onRefresh(Object entity, String entityName, LockMode lockMode) {
        logger.trace("onRefresh - entity : {}, entityName : {}, lockMode : {}", entity, entityName, lockMode);
        EntityCallback.super.onRefresh(entity, entityName, lockMode);
    }

    @Override
    public void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        logger.trace("onPreLoad - entity : {}, id : {}, state : {}, propertyNames : {}, types : {}",
                entity, id, state, propertyNames, types);
        EntityCallback.super.onPreLoad(entity, id, state, propertyNames, types);
    }

    @Override
    public void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types) {
        logger.trace("onPostLoad - entity : {}, id : {}, propertyNames : {}, types : {}", entity, id, propertyNames, types);
        EntityCallback.super.onPostLoad(entity, id, propertyNames, types);
    }

    @Override
    public void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        logger.trace("onPreDelete - entity : {}, id : {}, state : {}, propertyNames : {}, types : {}",
                entity, id, state, propertyNames, types);
        EntityCallback.super.onPreDelete(entity, id, state, propertyNames, types);
    }

    @Override
    public void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
        logger.trace("onPostDelete - entity : {}, id : {}, state : {}, propertyNames : {}, types : {}",
                entity, id, state, propertyNames, types);
        EntityCallback.super.onPostDelete(entity, id, state, propertyNames, types);
    }

    @Override
    public void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        logger.trace("onPreInsert - entity : {}, id : {}, state : {}, propertyNames : {}, propertyTypes : {}",
                entity, id, state, propertyNames, propertyTypes);
        EntityCallback.super.onPreInsert(entity, id, state, propertyNames, propertyTypes);
    }

    @Override
    public void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        logger.trace("onPostInsert - entity : {}, id : {}, state : {}, propertyNames : {}, propertyTypes : {}",
                entity, id, state, propertyNames, propertyTypes);
        EntityCallback.super.onPostInsert(entity, id, state, propertyNames, propertyTypes);
    }

    @Override
    public void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        logger.trace("onPreUpdate - entity : {}, id : {}, state : {}, propertyNames : {}, propertyTypes : {}",
                entity, id, state, propertyNames, propertyTypes);
        EntityCallback.super.onPreUpdate(entity, id, state, propertyNames, propertyTypes);
    }

    @Override
    public void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes) {
        logger.trace("onPostUpdate - entity : {}, id : {}, state : {}, propertyNames : {}, propertyTypes : {}",
                entity, id, state, propertyNames, propertyTypes);
        EntityCallback.super.onPostUpdate(entity, id, state, propertyNames, propertyTypes);
    }

    @Override
    public void onPreRecreateCollection(Object collection, Object key) {
        logger.trace("onPreRecreateCollection - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPreRecreateCollection(collection, key);
    }

    @Override
    public void onPostRecreateCollection(Object collection, Object key) {
        logger.trace("onPostRecreateCollection - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPostRecreateCollection(collection, key);
    }

    @Override
    public void onPreCollectionRemove(Object collection, Object key) {
        logger.trace("onPreCollectionRemove - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPreCollectionRemove(collection, key);
    }

    @Override
    public void onPostRemoveCollection(Object collection, Object key) {
        logger.trace("onPostRemoveCollection - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPostRemoveCollection(collection, key);
    }

    @Override
    public void onPreUpdateCollection(Object collection, Object key) {
        logger.trace("onPreUpdateCollection - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPreUpdateCollection(collection, key);
    }

    @Override
    public void onPostUpdateCollection(Object collection, Object key) {
        logger.trace("onPostUpdateCollection - collection : {}, key : {}", collection, key);
        EntityCallback.super.onPostUpdateCollection(collection, key);
    }
}
