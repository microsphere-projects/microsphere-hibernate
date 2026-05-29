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

package io.microsphere.hibernate.constants;

import io.microsphere.annotation.ConfigurationProperty;

import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.DOT;
import static java.lang.Boolean.parseBoolean;

/**
 * The interface to declare the property constants of Microsphere Hibernate
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Enable Microsphere Hibernate via system property
 *   System.setProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME, "true");
 *
 *   // Read the enabled flag
 *   boolean enabled = Boolean.parseBoolean(
 *       System.getProperty(MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME,
 *                          DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE));
 *
 *   // Use the property name prefix to build custom property names
 *   String customProperty = MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX + "myFeature.enabled";
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Hibernate : "microsphere.hibernate."
     */
    String MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX = MICROSPHERE_PROPERTY_NAME_PREFIX + "hibernate" + DOT;

    /**
     * The default value of enabled property of Microsphere Hibernate : "true"
     */
    String DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE = "true";

    /**
     * The default value of enabled property of Microsphere Hibernate : true
     */
    boolean DEFAULT_MICROSPHERE_HIBERNATE_ENABLED = parseBoolean(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);

    /**
     * The enabled property name of Microsphere Hibernate
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE,
            source = SYSTEM_PROPERTIES_SOURCE
    )
    String MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME = MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;

}
