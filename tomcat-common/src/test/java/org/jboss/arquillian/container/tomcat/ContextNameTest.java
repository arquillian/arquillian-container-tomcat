/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.tomcat;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Radoslav Husar
 */
public class ContextNameTest {

    @Test
    public void shouldDeriveContextPathFromArchiveName() {

        final ContextName contextName = new ContextName("test.war");

        assertEquals("/test", contextName.getPath());
        assertEquals("", contextName.getVersion());
        assertEquals("/test", contextName.getName());
    }

    @Test
    public void shouldDeriveRootContextPathFromRootArchiveName() {

        final ContextName contextName = new ContextName("ROOT.war");

        assertEquals("", contextName.getPath());
        assertEquals("", contextName.getVersion());
        assertEquals("", contextName.getName());
    }

    @Test
    public void shouldTreatHashAsPathSeparator() {

        final ContextName contextName = new ContextName("foo#bar.war");

        assertEquals("/foo/bar", contextName.getPath());
        assertEquals("", contextName.getVersion());
        assertEquals("/foo/bar", contextName.getName());
    }

    @Test
    public void shouldTreatEachHashAsPathSeparator() {

        final ContextName contextName = new ContextName("foo#bar#baz.war");

        assertEquals("/foo/bar/baz", contextName.getPath());
    }

    @Test
    public void shouldTreatDoubleHashAsVersionMarker() {

        final ContextName contextName = new ContextName("foo##1.0.war");

        assertEquals("/foo", contextName.getPath());
        assertEquals("1.0", contextName.getVersion());
        assertEquals("/foo##1.0", contextName.getName());
    }

    @Test
    public void shouldSupportVersionedMultiSegmentPath() {

        final ContextName contextName = new ContextName("foo#bar##1.0.war");

        assertEquals("/foo/bar", contextName.getPath());
        assertEquals("1.0", contextName.getVersion());
        assertEquals("/foo/bar##1.0", contextName.getName());
    }

    @Test
    public void shouldSupportVersionedRootContext() {

        final ContextName contextName = new ContextName("ROOT##1.0.war");

        assertEquals("", contextName.getPath());
        assertEquals("1.0", contextName.getVersion());
        assertEquals("##1.0", contextName.getName());
    }

    @Test
    public void shouldTolerateMissingWarExtension() {

        final ContextName contextName = new ContextName("foo#bar");

        assertEquals("/foo/bar", contextName.getPath());
    }

    @Test
    public void shouldOnlyStripTheWarExtension() {

        final ContextName contextName = new ContextName("foo.bar.war");

        assertEquals("/foo.bar", contextName.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNullArchiveName() {

        new ContextName(null);
    }
}
