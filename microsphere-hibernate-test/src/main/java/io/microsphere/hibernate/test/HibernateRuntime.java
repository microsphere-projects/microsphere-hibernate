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

package io.microsphere.hibernate.test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to inject Hibernate runtime components into test fields or method parameters.
 * <p>
 * Supported types for injection:
 * <ul>
 *   <li>{@link org.hibernate.cfg.Configuration}</li>
 *   <li>{@link org.hibernate.SessionFactory}</li>
 *   <li>{@link org.hibernate.Session} (instance fields and parameters only, not static fields)</li>
 *   <li>{@link org.hibernate.StatelessSession} (instance fields and parameters only, not static fields)</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HibernateTest
 * @see HibernateExtension
 * @since 1.0.0
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface HibernateRuntime {
}
