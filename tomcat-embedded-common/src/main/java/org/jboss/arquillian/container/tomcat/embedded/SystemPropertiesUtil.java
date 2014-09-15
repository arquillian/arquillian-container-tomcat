package org.jboss.arquillian.container.tomcat.embedded;

public class SystemPropertiesUtil
{
    private static final String ENV_VAR = "${env.";

    public String substituteEvironmentVariable(final String original)
    {
        if (original.startsWith(ENV_VAR))
        {
            final String systemPropertyKey = original.substring(ENV_VAR.length(), original.length() - 1);

            return System.getProperty(systemPropertyKey);
        }
        else
        {
            return original;
        }
    }
}
