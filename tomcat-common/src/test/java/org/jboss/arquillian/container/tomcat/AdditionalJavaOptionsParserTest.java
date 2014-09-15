/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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

import static junit.framework.Assert.*;

import java.util.List;

import org.junit.Test;

/**
 *
 * @author <a href="mailto:trepel@redhat.com">Tomas Repel</a>
 *
 */
public class AdditionalJavaOptionsParserTest
{

    @Test
    public void parseNull()
    {
        final String additionalProperties = null;
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(0, props.size());
    }

    @Test
    public void parseEmpty()
    {
        final String additionalProperties = "";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(0, props.size());
    }

    @Test
    public void parseOnlyWhiteSpaces()
    {
        final String additionalProperties = "  \t\n\t   ";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(0, props.size());
    }

    @Test
    public void parseDelimitedByWhitespaces()
    {
        final String additionalProperties = "p0 p1\tp2\np3  p2\t\tp5\n\np6";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(7, props.size());
    }

    @Test
    public void parseDelimitedByQuotes()
    {
        final String additionalProperties = "p0 \"p1 with space\"\t\"p2\"\"p3\"\n\"p4\" p5";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(6, props.size());
    }

    @Test
    public void parseWindowsPaths()
    {
        final String additionalProperties = "p1=C:\\MyApps\\myApp.exe \"p2=C:\\Program Files\\MyApp\\myApp.exe\"";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(2, props.size());
    }

    @Test
    public void parseIfQuoteIsInside()
    {
        final String additionalProperties = "p0 p1Quote\"Is\"Inside \"p2\"";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(3, props.size());
    }

    @Test
    public void parseQuotesOnly()
    {
        final String additionalProperties = "\"p0\"\"p1\"\"p3\"\"p4\"";
        final List<String> props = AdditionalJavaOptionsParser.parse(additionalProperties);

        assertNotNull(props);
        assertEquals(4, props.size());
    }
}
