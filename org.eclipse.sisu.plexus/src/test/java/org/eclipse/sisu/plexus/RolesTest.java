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

import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RolesTest {

    private static final TypeLiteral<Object> OBJECT_LITERAL = TypeLiteral.get(Object.class);

    private static final TypeLiteral<String> STRING_LITERAL = TypeLiteral.get(String.class);

    private static final TypeLiteral<Integer> INTEGER_LITERAL = TypeLiteral.get(Integer.class);

    private static final Key<Object> OBJECT_COMPONENT_KEY = Key.get(Object.class);

    private static final Key<Object> OBJECT_FOO_COMPONENT_KEY = Key.get(Object.class, Names.named("foo"));

    @Test
    public void testCanonicalRoleHint() {
        Assertions.assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), null));
        Assertions.assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), ""));
        Assertions.assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(Object.class.getName(), "default"));
        Assertions.assertEquals(OBJECT_LITERAL + ":foo", Roles.canonicalRoleHint(Object.class.getName(), "foo"));
        Assertions.assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(component("")));
        Assertions.assertEquals(OBJECT_LITERAL + "", Roles.canonicalRoleHint(component("default")));
        Assertions.assertEquals(OBJECT_LITERAL + ":foo", Roles.canonicalRoleHint(component("foo")));
    }

    @Test
    public void testDefaultComponentKeys() {
        Assertions.assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(Object.class, null));
        Assertions.assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(OBJECT_LITERAL, ""));
        Assertions.assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(Object.class, "default"));
        Assertions.assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(component("")));
        Assertions.assertEquals(OBJECT_COMPONENT_KEY, Roles.componentKey(component("default")));
    }

    @Test
    public void testComponentKeys() {
        Assertions.assertEquals(OBJECT_FOO_COMPONENT_KEY, Roles.componentKey(Object.class, "foo"));
        Assertions.assertEquals(OBJECT_FOO_COMPONENT_KEY, Roles.componentKey(component("foo")));
    }

    @Test
    public void testRoleAnalysis() {
        Assertions.assertEquals(STRING_LITERAL, Roles.roleType(requirement(String.class), OBJECT_LITERAL));
        Assertions.assertEquals(STRING_LITERAL, Roles.roleType(requirement(Object.class), STRING_LITERAL));

        Assertions.assertEquals(STRING_LITERAL,
          Roles.roleType(requirement(Object.class), TypeLiteral.get(Types.listOf(String.class))));

        Assertions.assertEquals(STRING_LITERAL,
          Roles.roleType(requirement(List.class), TypeLiteral.get(Types.listOf(String.class))));

        Assertions.assertEquals(INTEGER_LITERAL,
          Roles.roleType(requirement(Object.class),
            TypeLiteral.get(Types.mapOf(String.class, Integer.class))));

        Assertions.assertEquals(INTEGER_LITERAL,
          Roles.roleType(requirement(Map.class),
            TypeLiteral.get(Types.mapOf(String.class, Integer.class))));
    }

    private static Component component(final String hint) {
        return new ComponentImpl(Object.class, hint, Strategies.PER_LOOKUP, "");
    }

    @SuppressWarnings("deprecation")
    private static Requirement requirement(final Class<?> role) {
        return new RequirementImpl(role, false);
    }

    @Test
    public void testMissingComponentExceptions() {
        try {
            Roles.throwMissingComponentException(STRING_LITERAL, null);
            Assertions.fail("Expected ProvisionException");
        } catch (final ProvisionException e) {
        }

        try {
            Roles.throwMissingComponentException(STRING_LITERAL, "foo");
            Assertions.fail("Expected ProvisionException");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testCamelization() {
        Assertions.assertSame("thisIsATest", Roles.camelizeName("thisIsATest"));
        Assertions.assertEquals("thisIsATest", Roles.camelizeName("this-is-a-test"));
        Assertions.assertEquals("TestingA", Roles.camelizeName("-testing-a"));
        Assertions.assertEquals("testingB", Roles.camelizeName("testing-b-"));
        Assertions.assertEquals("TestingC", Roles.camelizeName("--testing--c--"));
    }
}
