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

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClassRealmConverterTest {

    @Test
    public void testClassRealmStack()
      throws Exception {
        final ClassWorld world = new ClassWorld();

        final ClassRealm realmA = world.newRealm("A");
        final ClassRealm realmB = world.newRealm("B");
        final ClassRealm realmC = world.newRealm("C");
        final ClassRealm realmD = world.newRealm("D");

        final ClassRealmConverter converter = new ClassRealmConverter(realmA);
        Assertions.assertEquals(realmA, converter.peekContextRealm());

        ClassRealmConverter.popContextRealm();
        Assertions.assertEquals(realmA, converter.peekContextRealm());

        ClassRealmConverter.pushContextRealm(realmB);
        Assertions.assertEquals(realmB, converter.peekContextRealm());

        ClassRealmConverter.pushContextRealm(realmC);
        Assertions.assertEquals(realmC, converter.peekContextRealm());

        ClassRealmConverter.pushContextRealm(realmD);
        Assertions.assertEquals(realmD, converter.peekContextRealm());

        ClassRealmConverter.popContextRealm();
        Assertions.assertEquals(realmC, converter.peekContextRealm());

        ClassRealmConverter.popContextRealm();
        Assertions.assertEquals(realmB, converter.peekContextRealm());

        ClassRealmConverter.popContextRealm();
        Assertions.assertEquals(realmA, converter.peekContextRealm());

        ClassRealmConverter.popContextRealm();
        Assertions.assertEquals(realmA, converter.peekContextRealm());
    }
}
