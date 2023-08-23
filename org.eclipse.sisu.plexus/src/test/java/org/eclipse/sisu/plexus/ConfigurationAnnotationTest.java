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

import java.util.Arrays;
import java.util.HashSet;

import org.codehaus.plexus.component.annotations.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigurationAnnotationTest {

    @Configuration("Default")
    String defaultConfig;

    @Configuration(name = "Name", value = "Default")
    String namedConfig;

    @Configuration("${property}")
    String propertyConfig;

    @Test
    public void testConfigurationImpl()
      throws NoSuchFieldException {
        checkBehaviour("defaultConfig");
        checkBehaviour("namedConfig");
        checkBehaviour("propertyConfig");

        Assertions.assertFalse(replicate(getConfiguration("defaultConfig")).equals(getConfiguration("namedConfig")));
        Assertions.assertFalse(replicate(getConfiguration("defaultConfig")).equals(getConfiguration("propertyConfig")));
    }

    private static void checkBehaviour(final String name)
      throws NoSuchFieldException {
        final Configuration orig = getConfiguration(name);
        final Configuration clone = replicate(orig);

        Assertions.assertTrue(orig.equals(clone));
        Assertions.assertTrue(clone.equals(orig));
        Assertions.assertTrue(clone.equals(clone));
        Assertions.assertFalse(clone.equals(""));

        Assertions.assertEquals(orig.hashCode(), clone.hashCode());

        String origToString = orig.toString().replace("\"", "").replace(".class", "");
        String cloneToString = clone.toString().replace('[', '{').replace(']', '}');
        cloneToString = cloneToString.replace("class ", "").replace("interface ", "");

        Assertions.assertEquals(new HashSet<String>(Arrays.asList(origToString.split("[(, )]"))),
          new HashSet<String>(Arrays.asList(cloneToString.split("[(, )]"))));

        Assertions.assertEquals(orig.annotationType(), clone.annotationType());
    }

    private static Configuration getConfiguration(final String name)
      throws NoSuchFieldException {
        return ConfigurationAnnotationTest.class.getDeclaredField(name).getAnnotation(Configuration.class);
    }

    private static Configuration replicate(final Configuration orig) {
        return new ConfigurationImpl(orig.name(), orig.value());
    }

    @Test
    public void testNullChecks() {
        checkNullNotAllowed(null, "");
        checkNullNotAllowed("", null);
    }

    private static void checkNullNotAllowed(final String name, final String value) {
        try {
            new ConfigurationImpl(name, value);
            Assertions.fail("Expected IllegalArgumentException");
        } catch (final IllegalArgumentException e) {
        }
    }
}
