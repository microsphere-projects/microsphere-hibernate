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

import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a JUnit Jupiter test class as a Hibernate test.
 * <p>
 * Registering this annotation on a test class will automatically:
 * <ul>
 *   <li>Build a Hibernate {@link Configuration} from the given {@link #properties()} and
 *       entity packages from {@link #scanEntityPackages()}</li>
 *   <li>Create a {@code SessionFactory} before all tests in the class</li>
 *   <li>Open a {@code Session} and {@code StatelessSession} before each test method</li>
 *   <li>Close the {@code Session} and {@code StatelessSession} after each test method</li>
 *   <li>Close the {@code SessionFactory} after all tests in the class</li>
 * </ul>
 * <p>
 * Fields and method parameters annotated with {@link HibernateRuntime} will be injected
 * with the appropriate Hibernate component.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HibernateRuntime
 * @see HibernateExtension
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(HibernateExtension.class)
public @interface HibernateTest {

    /**
     * The properties of Hibernate {@link Configuration}, in {@code key=value} format.
     *
     * @return the property strings
     */
    String[] properties() default {};

    /**
     * The packages of entities to be scanned.
     *
     * @return the package names
     */
    String[] scanEntityPackages() default {};
}
