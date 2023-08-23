/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.sisu.plexus;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.name.Names;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapConstantTest {

    @BeforeEach
    protected void setUp()
      throws Exception {
        Guice.createInjector(new AbstractModule() {
            private void bind(final String name, final String value) {
                bindConstant().annotatedWith(Names.named(name)).to(value);
            }

            @Override
            protected void configure() {
                bind("Empty", "<items/>");

                bind("Custom",
                  "<items implementation='java.util.LinkedHashMap'>"
                    + "<builder implementation='java.lang.StringBuilder'>TEST</builder>"
                    + "<file implementation='java.io.File'>TEST</file>" + "</items>");

                bind("Map", "<items><key1>value1</key1><key2>value2</key2></items>");

                bind("Properties", "<items><property><name>key1</name><value>value1</value></property>"
                  + "<property><value>value2</value><name>key2</name></property></items>");

                bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);
                install(new ConfigurationConverter());
            }
        }).injectMembers(this);
    }

    @Inject
    @Named("Empty")
    Map<?, ?> empty;

    @Inject
    @Named("Custom")
    Map<?, ?> custom;

    @Inject
    @Named("Map")
    Map<?, ?> map;

    @Inject
    @Named("Properties")
    Properties properties;

    @Test
    public void testEmptyMap() {
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    public void testCustomMap() {
        Assertions.assertEquals(LinkedHashMap.class, custom.getClass());
        Assertions.assertEquals("TEST", custom.get("builder").toString());
        Assertions.assertEquals(StringBuilder.class, custom.get("builder").getClass());
        Assertions.assertEquals(new File("TEST"), custom.get("file"));
    }

    @Test
    public void testMapAndProperties() {
        final HashMap<String, String> testMap = new HashMap<String, String>();
        testMap.put("key1", "value1");
        testMap.put("key2", "value2");

        Assertions.assertEquals(testMap, map);
        Assertions.assertEquals(testMap, properties);
    }
}
