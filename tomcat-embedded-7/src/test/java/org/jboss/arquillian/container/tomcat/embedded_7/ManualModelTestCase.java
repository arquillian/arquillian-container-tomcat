package org.jboss.arquillian.container.tomcat.embedded_7;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ManualModelTestCase {

    @Deployment(name = "X", managed = false)
    public static WebArchive createTestArchive()
    {
       return ShrinkWrap.create(WebArchive.class)
             .setWebXML("in-container-web.xml");
    }
    
    @ArquillianResource
    private ContainerController cc;

    @ArquillianResource
    private Deployer d;

    @Test @InSequence(0) @RunAsClient
    public void start() {
        cc.start("tomcat");
        d.deploy("X");
    }

    @Test @InSequence(2)
    public void inContainer() {
        System.out.println("weee");
    }

    @Test @InSequence(5) @RunAsClient
    public void stop() {
        d.undeploy("X");
        cc.stop("tomcat");
    }
    
    @Test @InSequence(10) @RunAsClient
    public void startAgain() throws Exception {
        cc.start("tomcat");
        d.deploy("X");
    }

    @Test @InSequence(11)
    public void inContainerAgain() {
        System.out.println("weee2");
    }

    @Test @InSequence(15) @RunAsClient
    public void stopAgain() {
        d.undeploy("X");
        cc.stop("tomcat");
    }
}
