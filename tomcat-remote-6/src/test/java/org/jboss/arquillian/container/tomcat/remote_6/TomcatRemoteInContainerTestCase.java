/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.remote_6;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.IOUtil;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the
 * Arquillian lifecycle
 * 
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatRemoteInContainerTestCase
{
    private static final Logger log = Logger.getLogger(TomcatRemoteInContainerTestCase.class.getName());
    
    
    //private static final String HELLO_WORLD_URL = "http://localhost:8080/test2/Test";
    private static final String PORT;
    static {
        String port = "8080";
        foo: try {
            // Ugly way to get port.
            InputStream is = TomcatRemoteClientTestCase.class.getResourceAsStream("arquillian.xml");
            if( null == is )
                break foo;
            String file = IOUtils.toString( is );
            port = StringUtils.substringBetween(file, "<port>", "</port>");
        } catch (IOException ex) {
        }
        PORT = port;
    }


    /**
     * Define the deployment
     */
    @Deployment
    public static WebArchive createTestArchive()
    {
       
        // Take the version from the package on classpath (it's MANIFEST.MF)
        //String WELD_VERSION = org.jboss.weld.servlet.WeldListener.class.getPackage().getImplementationVersion(); // 20110114-1644
        String WELD_VERSION = org.jboss.weld.servlet.WeldListener.class.getPackage().getSpecificationVersion();
        if( WELD_VERSION == null )
            WELD_VERSION = "1.1.1.Final";
        log.fine("  Using weld-servlet version: " + WELD_VERSION);
        
        final String JAVAEE_VERSION = "1.0.0.Final";
        final String CDI_API_VERSION = "1.0-SP4";

        
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test2.war")
                                .addClasses(TestServlet.class, TestBean.class)
                                   .addAsLibraries(
                                         DependencyResolvers.use(MavenDependencyResolver.class).loadReposFromPom("pom.xml") //loadDependenciesFromPom("pom.xml") //loadReposFromPom
                                                // TODO: Make the version being taken from package.
                                               .artifact("org.jboss.weld.servlet:weld-servlet:" + WELD_VERSION)
                
                                                //javax.enterprise.inject.spi.BeanManager,
                                                //org.jboss.weld.resources.ManagerObjectFactory
                                                //.artifact("org.jboss.spec:jboss-javaee-6.0") //:" + JAVAEE_VERSION)
                                                .artifact("javax.enterprise:cdi-api:" + CDI_API_VERSION)
                            
                                               .resolveAs(GenericArchive.class))

                                .addAsWebInfResource("beans.xml") // EmptyAsset.INSTANCE
                                .addAsManifestResource("in-container-context.xml", "context.xml")
                                //.addAsResource("log4j.properties")
                                .setWebXML("in-container-web.xml");
        /// DEBUG - see what's 
        //war.as(ZipExporter.class).exportTo( new File("/tmp/arq.zip"), true );
        return war;
    }

    // -------------------------------------------------------------------------------------||
    // Tests -------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Resource(name = "resourceInjectionTestName") private String resourceInjectionTestValue;

    @Inject TestBean testBean;

    /**
     * Ensures the {@link HelloWorldServlet} returns the expected response
     */
    @Test
    public void shouldBeAbleToInjectMembersIntoTestClass()
    {
      log.info("Name: " + this.resourceInjectionTestValue);
      Assert.assertEquals("Hello World from an evn-entry", this.resourceInjectionTestValue);
      Assert.assertNotNull(testBean);
      Assert.assertEquals("Hello World from an evn-entry", testBean.getName());
    }

   @Test
   public void shouldBeAbleToInvokeServletInDeployedWebApp() throws Exception
   {
        // Define the input and expected outcome
        final String expected = "hello";

        String HELLO_WORLD_URL = "http://localhost:"+PORT+"/test2/Test";
        URL url = new URL(HELLO_WORLD_URL);
        InputStream in = url.openConnection().getInputStream();

        byte[] buffer = new byte[10000];
        int len = in.read(buffer);
        String httpResponse = "";
        for (int q = 0; q < len; q++)
      {
            httpResponse += (char) buffer[q];
      }

        // Test
        Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
        log.info("Got expected result from Http Servlet: " + httpResponse);
   }
}
