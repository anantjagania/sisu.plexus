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
import java.util.Collections;

import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HintsTest {

    @Test
    public void testCanonicalHint() {
        Assertions.assertEquals("default", Hints.canonicalHint(null));
        Assertions.assertEquals("default", Hints.canonicalHint(""));
        Assertions.assertEquals("default", Hints.canonicalHint(new String("default")));
        Assertions.assertEquals("foo", Hints.canonicalHint("foo"));
    }

    @Test
    public void testCanonicalHints() {
        assertArrayEquals(new String[0], Hints.canonicalHints());
        assertArrayEquals(new String[0], Hints.canonicalHints(requirement()));
        assertArrayEquals(new String[0], Hints.canonicalHints(requirement("")));
        assertArrayEquals(new String[] { "default" }, Hints.canonicalHints(requirement("default")));
        assertArrayEquals(new String[] { "foo" }, Hints.canonicalHints(requirement("foo")));
        assertArrayEquals(new String[] { "default", "foo" }, Hints.canonicalHints(requirement("", "foo")));
        assertArrayEquals(new String[] { "foo", "default" }, Hints.canonicalHints(requirement("foo", "")));
    }
    @Test

    public void testCanonicalHintList() {
        Assertions.assertEquals(Collections.emptyList(), Hints.canonicalHints(Arrays.<String>asList()));
        Assertions.assertEquals(Collections.emptyList(), Hints.canonicalHints(Arrays.asList("")));
        Assertions.assertEquals(Arrays.asList(Hints.DEFAULT_HINT), Hints.canonicalHints(Arrays.asList("default")));
        Assertions.assertEquals(Arrays.asList("foo"), Hints.canonicalHints(Arrays.asList("foo")));
        Assertions.assertEquals(Arrays.asList("default", "foo"), Hints.canonicalHints(Arrays.asList("", "foo")));
        Assertions.assertEquals(Arrays.asList("foo", "default"), Hints.canonicalHints(Arrays.asList("foo", "")));
    }

    @Test
    public void testHintsAreInterned() {
        Assertions.assertSame("hint", Hints.canonicalHint(new String("hint")));
        Assertions.assertSame("hint", Hints.canonicalHints(requirement(new String("hint")))[0]);
        final Requirement requirement = requirement(new String("foo"), new String("bar"));
        Assertions.assertSame("foo", Hints.canonicalHints(requirement)[0]);
        Assertions.assertSame("bar", Hints.canonicalHints(requirement)[1]);
        Assertions.assertNotSame(new String("hint"), Hints.canonicalHint("hint"));
        Assertions.assertEquals(new String("hint"), Hints.canonicalHint("hint"));
    }

    @Test
    public void testIsDefaultHint() {
        Assertions.assertTrue(Hints.isDefaultHint(null));
        Assertions.assertTrue(Hints.isDefaultHint(""));
        Assertions.assertTrue(Hints.isDefaultHint(new String("default")));
        Assertions.assertFalse(Hints.isDefaultHint("foo"));
    }

    private static <T> void assertArrayEquals(final T[] a, final T[] b) {
        Assertions.assertTrue(Arrays.equals(a, b), ("Expected: " + Arrays.toString(a) + "but was: " + Arrays.toString(b)));
    }

    @SuppressWarnings("deprecation")
    private static Requirement requirement(final String... hints) {
        return new RequirementImpl(Object.class, true, hints);
    }
}
