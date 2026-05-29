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

import org.junit.jupiter.api.Test;

import static io.microsphere.hibernate.constants.PropertyConstants.DEFAULT_MICROSPHERE_HIBERNATE_ENABLED;
import static io.microsphere.hibernate.constants.PropertyConstants.DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE;
import static io.microsphere.hibernate.constants.PropertyConstants.MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME;
import static io.microsphere.hibernate.constants.PropertyConstants.MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropertyConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyConstants
 * @since 1.0.0
 */
class PropertyConstantsTest {

    @Test
    void test() {
        assertEquals("microsphere.hibernate.", MICROSPHERE_HIBERNATE_PROPERTY_NAME_PREFIX);
        assertEquals("true", DEFAULT_MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_VALUE);
        assertTrue(DEFAULT_MICROSPHERE_HIBERNATE_ENABLED);
        assertEquals("microsphere.hibernate.enabled", MICROSPHERE_HIBERNATE_ENABLED_PROPERTY_NAME);
    }

}
