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
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import jakarta.inject.Inject;

import java.io.File;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.sisu.inject.DeferredClass;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.URLClassSpace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlexusRequirementTest {

    @Inject
    Component1 component;

    @Inject
    Injector injector;

    @BeforeEach
    protected void setUp() {
        Guice.createInjector(new AbstractModule() {
            @Override
            @SuppressWarnings("unchecked")
            protected void configure() {
                final ClassSpace space = new URLClassSpace(PlexusRequirementTest.class.getClassLoader());

                final DeferredClass<A> deferA = (DeferredClass<A>)space.deferLoadClass(BrokenAImpl.class.getName());

                install(new PlexusDateTypeConverter());

                bind(PlexusBeanLocator.class).to(DefaultPlexusBeanLocator.class);
                bind(PlexusBeanConverter.class).to(PlexusXmlBeanConverter.class);

                bind(A.class).annotatedWith(Names.named("AA")).to(AAImpl.class);
                bind(A.class).annotatedWith(Names.named("broken")).toProvider(deferA.asProvider());
                bind(A.class).annotatedWith(Names.named("AB")).to(ABImpl.class);
                bind(A.class).to(AImpl.class).in(Scopes.SINGLETON);
                bind(A.class).annotatedWith(Names.named("AC")).to(ACImpl.class);

                bind(B.class).annotatedWith(Names.named("B")).to(BImpl.class);

                bind(D.class).annotatedWith(Names.named("")).to(DImpl.class);

                install(new PlexusBindingModule(null, new PlexusBeanModule() {
                    public PlexusBeanSource configure(final Binder binder) {
                        binder.bind(Alpha.class).to(AlphaImpl.class).in(Scopes.SINGLETON);
                        binder.bind(Omega.class).to(OmegaImpl.class).in(Scopes.SINGLETON);

                        final DeferredClass<Gamma> gammaProvider =
                          (DeferredClass<Gamma>)space.deferLoadClass("some-broken-class").asProvider();

                        binder.bind(Gamma.class).toProvider(gammaProvider.asProvider()).in(Scopes.SINGLETON);

                        return null;
                    }
                }, new PlexusAnnotatedBeanModule(null, null)));

                requestInjection(PlexusRequirementTest.this);
            }
        });
    }

    @ImplementedBy(AImpl.class)
    interface A {

    }

    interface B {

    }

    interface C {

    }

    interface D {

    }

    static class AImpl
      implements A {

    }

    static class BImpl
      implements B {

    }

    static class DImpl
      implements D {

    }

    static class AAImpl
      extends AImpl {

    }

    static class ABImpl
      extends AImpl {

    }

    static class ACImpl
      extends AImpl {

    }

    static class BrokenAImpl
      extends AImpl {

        public BrokenAImpl(@SuppressWarnings("unused") final MissingClass missing) {
        }
    }

    @Component(role = Component1.class, instantiationStrategy = Strategies.PER_LOOKUP)
    static class Component1 {

        @Requirement
        A testField;

        A testSetter;

        @Requirement(hints = { "default" })
        void setValue(final A a) {
            testSetter = a;
        }

        @Requirement(role = A.class)
        Object testRole;

        @Requirement(hint = "AB", optional = true)
        A testHint;

        @Requirement(hint = "MISSING", optional = true)
        A testOptional = new ACImpl();

        @Requirement(role = A.class)
        Map<String, ?> testMap;

        @Requirement(hints = { "AC", "AB" })
        Map<String, A> testSubMap;

        @Requirement
        Map<String, C> testEmptyMap;

        @Requirement(role = A.class)
        List<?> testList;

        @Requirement(hints = { "AC", "AA" })
        List<? extends A> testSubList;

        @Requirement
        List<C> testEmptyList;

        @Requirement(role = A.class)
        Collection<?> testCollection;

        @Requirement(role = A.class)
        Iterable<?> testIterable;

        @Requirement(role = A.class)
        Set<?> testSet;

        @Requirement
        B testWildcard;

        @Requirement(optional = true)
        C optionalRequirement;
    }

    @Component(role = Component2.class)
    static class Component2 {

        @Requirement
        void testZeroArgSetter() {
            throw new RuntimeException();
        }
    }

    @Component(role = Component3.class)
    static class Component3 {

        @Requirement
        @SuppressWarnings("unused")
        void testMultiArgSetter(final A a1, final A a2) {
            throw new RuntimeException();
        }
    }

    @Component(role = Component4.class)
    static class Component4 {

        @Requirement
        C testMissingRequirement;
    }

    @Component(role = Component5.class)
    static class Component5 {

        @Requirement(hint = "B!")
        B testNoSuchHint;
    }

    @Component(role = Component6.class)
    static class Component6 {

        @Requirement(hints = { "AA", "AZ", "A!" })
        Map<String, B> testNoSuchHint;
    }

    @Component(role = Component7.class)
    static class Component7 {

        @Requirement(hints = { "AA", "AZ", "A!" })
        List<C> testNoSuchHint;
    }

    @Component(role = Component8.class)
    static class Component8 {

        @Requirement(hints = { "" })
        List<A> testWildcardHint;
    }

    @Component(role = Component9.class)
    static class Component9 {

        @Requirement(hint = "default")
        B testNoDefault;
    }

    @Test
    public void testRepeatInjection() {
        final Component1 duplicate = injector.getInstance(Component1.class);
        Assertions.assertSame(component.testField, duplicate.testField);
        Assertions.assertSame(component.testSetter, duplicate.testSetter);
        Assertions.assertSame(component.testRole, duplicate.testRole);
    }

    @Test
    public void testSingleRequirement() {
        Assertions.assertEquals(AImpl.class, component.testField.getClass());
        Assertions.assertEquals(AImpl.class, component.testSetter.getClass());
        Assertions.assertEquals(AImpl.class, component.testRole.getClass());
        Assertions.assertEquals(ABImpl.class, component.testHint.getClass());
        Assertions.assertEquals(ACImpl.class, component.testOptional.getClass());
        Assertions.assertEquals(BImpl.class, component.testWildcard.getClass());
    }

    @Test
    public void testRequirementMap() {
        Assertions.assertEquals(5, component.testMap.size());
        Assertions.assertEquals(0, component.testEmptyMap.size());

        // check mapping
        Assertions.assertEquals(AImpl.class, component.testMap.get("default").getClass());
        Assertions.assertEquals(AAImpl.class, component.testMap.get("AA").getClass());
        Assertions.assertEquals(ABImpl.class, component.testMap.get("AB").getClass());
        Assertions.assertEquals(ACImpl.class, component.testMap.get("AC").getClass());

        // check key ordering is same as original map-binder
        final Iterator<String> keys = component.testMap.keySet().iterator();
        Assertions.assertEquals(Hints.DEFAULT_HINT, keys.next());
        Assertions.assertEquals("AA", keys.next());
        Assertions.assertEquals("broken", keys.next());
        Assertions.assertEquals("AB", keys.next());
        Assertions.assertEquals("AC", keys.next());
        Assertions.assertFalse(keys.hasNext());

        // check value ordering is same as original map-binder
        final Iterator<?> values = component.testMap.values().iterator();
        Assertions.assertEquals(AImpl.class, values.next().getClass());
        Assertions.assertEquals(AAImpl.class, values.next().getClass());
        try {
            values.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, values.next().getClass());
        Assertions.assertEquals(ACImpl.class, values.next().getClass());
        Assertions.assertFalse(values.hasNext());
    }

    @Test
    public void testRequirementSubMap() {
        Assertions.assertEquals(2, component.testSubMap.size());

        // check mapping
        Assertions.assertEquals(ABImpl.class, component.testSubMap.get("AB").getClass());
        Assertions.assertEquals(ACImpl.class, component.testSubMap.get("AC").getClass());

        // check key ordering is same as original map-binder
        final Iterator<String> keys = component.testSubMap.keySet().iterator();
        Assertions.assertEquals("AC", keys.next());
        Assertions.assertEquals("AB", keys.next());
        Assertions.assertFalse(keys.hasNext());

        // check value ordering is same as hints
        final Iterator<A> values = component.testSubMap.values().iterator();
        Assertions.assertEquals(ACImpl.class, values.next().getClass());
        Assertions.assertEquals(ABImpl.class, values.next().getClass());
        Assertions.assertFalse(values.hasNext());
    }

    @Test
    public void testRequirementList() {
        Assertions.assertEquals(5, component.testList.size());
        Assertions.assertEquals(0, component.testEmptyList.size());

        // check ordering is same as original map-binder
        final Iterator<?> i = component.testList.iterator();
        Assertions.assertEquals(AImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        try {
            i.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, i.next().getClass());
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testRequirementSubList() {
        Assertions.assertEquals(2, component.testSubList.size());

        // check ordering is same as hints
        final Iterator<? extends A> i = component.testSubList.iterator();
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testRequirementCollection() {
        Assertions.assertEquals(5, component.testCollection.size());

        // check ordering is same as original map-binder
        final Iterator<?> i = component.testCollection.iterator();
        Assertions.assertEquals(AImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        try {
            i.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, i.next().getClass());
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testRequirementIterable() {
        // check ordering is same as original map-binder
        final Iterator<?> i = component.testIterable.iterator();
        Assertions.assertEquals(AImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        try {
            i.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, i.next().getClass());
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testRequirementSet() {
        Assertions.assertEquals(5, component.testSet.size());

        // check ordering is same as original map-binder
        final Iterator<?> i = component.testSet.iterator();
        Assertions.assertEquals(AImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        try {
            i.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, i.next().getClass());
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testZeroArgSetterError() {
        injector.getInstance(Component2.class);
    }

    @Test
    public void testMultiArgSetterError() {
        injector.getInstance(Component3.class);
    }

    @Test
    public void testMissingRequirement() {
        try {
            injector.getInstance(Component4.class);
            Assertions.fail("Expected error for missing requirement");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testNoSuchHint() {
        try {
            injector.getInstance(Component5.class);
            Assertions.fail("Expected error for no such hint");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testNoSuchMapHint() {
        try {
            injector.getInstance(Component6.class).testNoSuchHint.toString();
            Assertions.fail("Expected error for no such hint");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testNoSuchListHint() {
        try {
            injector.getInstance(Component7.class).testNoSuchHint.toString();
            Assertions.fail("Expected error for no such hint");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testWildcardHint() {
        final List<A> testList = injector.getInstance(Component8.class).testWildcardHint;

        Assertions.assertEquals(5, testList.size());

        // check ordering is same as original map-binder
        final Iterator<?> i = testList.iterator();
        Assertions.assertEquals(AImpl.class, i.next().getClass());
        Assertions.assertEquals(AAImpl.class, i.next().getClass());
        try {
            i.next();
            Assertions.fail("Expected NoClassDefFoundError");
        } catch (final NoClassDefFoundError e) {
        }
        Assertions.assertEquals(ABImpl.class, i.next().getClass());
        Assertions.assertEquals(ACImpl.class, i.next().getClass());
        Assertions.assertFalse(i.hasNext());
    }

    @Test
    public void testNoDefault() {
        try {
            injector.getInstance(Component9.class);
            Assertions.fail("Expected error for missing default requirement");
        } catch (final ProvisionException e) {
        }
    }

    interface Alpha {

    }

    interface Omega {

    }

    interface Gamma {

    }

    @Component(role = Alpha.class)
    static class AlphaImpl
      implements Alpha {

        @Requirement
        Omega omega;
    }

    @Component(role = Omega.class)
    static class OmegaImpl
      implements Omega {

        @Requirement
        Alpha alpha;
    }

    @Inject
    Alpha alpha;

    @Inject
    Omega omega;

    @Test
    public void testCircularity() {
        Assertions.assertNotNull(((OmegaImpl)omega).alpha);
        Assertions.assertNotNull(((AlphaImpl)alpha).omega);

        Assertions.assertSame(alpha, ((OmegaImpl)omega).alpha);
        Assertions.assertSame(omega, ((AlphaImpl)alpha).omega);
    }

    @Test
    public void testBadDeferredRole() {
        try {
            injector.getInstance(Gamma.class);
            Assertions.fail("Expected ProvisionException");
        } catch (final ProvisionException e) {
        }
    }

    @Test
    public void testPlexus121Compatibility()
      throws Exception {
        final List<URL> urls = new ArrayList<URL>();
        urls.add(new File("target/dependency/plexus-component-annotations-1.2.1.jar").toURI().toURL());
        Collections.addAll(urls, new URLClassSpace(getClass().getClassLoader()).getURLs());

        // check binding works with Plexus 1.2.1 annotations: @Requirement does not have optional setting
        final ClassLoader legacyLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null) {
            @Override
            protected synchronized Class<?> loadClass(final String name, final boolean resolve)
              throws ClassNotFoundException {
                if (name.contains("cobertura")) {
                    return PlexusRequirementTest.class.getClassLoader().loadClass(name);
                }
                return super.loadClass(name, resolve);
            }
        };
        legacyLoader.loadClass(SimpleRequirementExample.class.getName()).newInstance();
    }

    @SuppressWarnings("unchecked")
    static <S, T extends S> DeferredClass<T> defer(final Class<S> clazz) {
        return (DeferredClass<T>)new URLClassSpace(PlexusRequirementTest.class.getClassLoader()).deferLoadClass(clazz.getName());
    }
}
