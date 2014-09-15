/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author <a href="mailto:trepel@redhat.com">Tomas Repel</a>
 *
 */
public class AdditionalJavaOptionsParser
{

    private static final String OPTION = "\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"|\\S+";

    private static final String QUOTED_CONTENT = "^\"(.*)\"$";

    /**
     * Parse additional java options. Options are separated by whitespace. In case some option value contains whitespace, the
     * whole key-value pair has to be quoted. For instance string 'opt0 opt1=val1 "opt2=val2 with space"' results in three
     * key-value pairs (opt0 option has an empty value).
     *
     * @param additionaOptions - options to parse
     * @return List of parsed options, returns empty list rather that null value
     */
    public static List<String> parse(final String additionalOptions)
    {
        final List<String> options = new ArrayList<String>();
        if (additionalOptions != null)
        {
            final Pattern p = Pattern.compile(OPTION, Pattern.DOTALL);
            final Matcher m = p.matcher(additionalOptions);
            while (m.find())
            {
                if (!m.group().trim().equals(""))
                {
                    options.add(Pattern.compile(QUOTED_CONTENT, Pattern.DOTALL).matcher(m.group().trim()).replaceAll("$1"));
                }
            }
        }
        return options;
    }

}
