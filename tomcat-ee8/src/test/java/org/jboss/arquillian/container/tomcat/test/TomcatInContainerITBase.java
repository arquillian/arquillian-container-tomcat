/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.test;

import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.*;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Assert;
import org.junit.Test;

public abstract class TomcatInContainerITBase {

    protected static final TestDeploymentFactory TEST_DEPLOYMENT_FACTORY = new TestDeploymentFactory();

    @Resource(name = "resourceInjectionTestName")
    protected String resourceInjectionTestValue;

    @Test
    @OperateOnDeployment(ROOT_CONTEXT)
    public void shouldBeAbleToInjectMembersIntoTestClassOfRootWebApp(final TestBean testBean) {

        assertInjection(testBean);
    }

    @Test
    @OperateOnDeployment(TEST_CONTEXT)
    public void shouldBeAbleToInjectMembersIntoTestClassOfTestWebApp(final TestBean testBean) {

        assertInjection(testBean);
    }

    protected void assertInjection(final TestBean testBean) {

        Assert.assertEquals("Hello World from an evn-entry", this.resourceInjectionTestValue);
        Assert.assertNotNull(testBean);
        Assert.assertEquals("Hello World from an evn-entry", testBean.getName());
    }
}
