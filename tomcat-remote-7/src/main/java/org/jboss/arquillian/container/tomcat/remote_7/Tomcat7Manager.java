package org.jboss.arquillian.container.tomcat.remote_7;

import org.jboss.arquillian.container.tomcat.CommonTomcatConfiguration;
import org.jboss.arquillian.container.tomcat.CommonTomcatManager;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/10/13
 * Time: 10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Tomcat7Manager extends CommonTomcatManager {

    /**
     * Creates a Tomcat manager abstraction
     *
     * @param configuration the configuration
     */
    public Tomcat7Manager(CommonTomcatConfiguration configuration) {
        super(configuration);
    }

    protected String getDeployCommand()
    {
       return "/text/deploy?path=";
    }

    protected String getUndeployCommand()
    {
       return "/text/undeploy?path=";
    }
}
