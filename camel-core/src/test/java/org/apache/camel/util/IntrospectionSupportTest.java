/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.util.jndi.ExampleBean;

/**
 * Unit test for IntrospectionSupport
 */
public class IntrospectionSupportTest extends ContextTestSupport {

    public void testOverloadSetterChooseStringSetter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", "James");
        assertEquals("James", overloadedBean.getName());
    }

    public void testOverloadSetterChooseBeanSetter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", bean);
        assertEquals("Claus", overloadedBean.getName());
    }

    public void testOverloadSetterChooseUsingTypeConverter() throws Exception {
        MyOverloadedBean overloadedBean = new MyOverloadedBean();
        Object value = "Willem".getBytes();
        // should use byte[] -> String type converter and call the setBean(String) setter method 
        IntrospectionSupport.setProperty(context.getTypeConverter(), overloadedBean, "bean", value);
        assertEquals("Willem", overloadedBean.getName());
    }

    public class MyOverloadedBean {
        private ExampleBean bean;

        public void setBean(ExampleBean bean) {
            this.bean = bean;
        }

        public void setBean(String name) {
            bean = new ExampleBean();
            bean.setName(name);
        }

        public String getName() {
            return bean.getName();
        }
    }
    
    public class MyBuilderBean {
        private String name;
        
        public String getName() {
            return name;
        }

        public MyBuilderBean setName(String name) {
            this.name = name;
            
            return this;
        }
    }
    
    public class MyOtherBuilderBean extends MyBuilderBean {    
    }
    
    public class MyOtherOtherBuilderBean extends MyOtherBuilderBean {
        
        public MyOtherOtherBuilderBean setName(String name) {
            super.setName(name);
            return this;
        }
    }
    
    public void testIsSetterBuilderPatternSupport() throws Exception {
        Method setter = MyBuilderBean.class.getMethod("setName", String.class);
        Method setter2 = MyOtherBuilderBean.class.getMethod("setName", String.class);
        Method setter3 = MyOtherOtherBuilderBean.class.getMethod("setName", String.class);
        
        assertFalse(IntrospectionSupport.isSetter(setter, false));
        assertTrue(IntrospectionSupport.isSetter(setter, true));
        
        assertFalse(IntrospectionSupport.isSetter(setter2, false));
        assertTrue(IntrospectionSupport.isSetter(setter2, true));
        
        assertFalse(IntrospectionSupport.isSetter(setter3, false));
        assertTrue(IntrospectionSupport.isSetter(setter3, true));
    }

    public void testHasProperties() throws Exception {
        Map<String, Object> empty = CastUtils.cast(Collections.emptyMap());
        assertFalse(IntrospectionSupport.hasProperties(empty, null));
        assertFalse(IntrospectionSupport.hasProperties(empty, ""));
        assertFalse(IntrospectionSupport.hasProperties(empty, "foo."));

        Map<String, Object> param = new HashMap<String, Object>();
        assertFalse(IntrospectionSupport.hasProperties(param, null));
        assertFalse(IntrospectionSupport.hasProperties(param, ""));
        assertFalse(IntrospectionSupport.hasProperties(param, "foo."));

        param.put("name", "Claus");
        assertTrue(IntrospectionSupport.hasProperties(param, null));
        assertTrue(IntrospectionSupport.hasProperties(param, ""));
        assertFalse(IntrospectionSupport.hasProperties(param, "foo."));

        param.put("foo.name", "Hadrian");
        assertTrue(IntrospectionSupport.hasProperties(param, null));
        assertTrue(IntrospectionSupport.hasProperties(param, ""));
        assertTrue(IntrospectionSupport.hasProperties(param, "foo."));
    }

    public void testGetProperties() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Map<String, Object> map = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(bean, map, null);
        assertEquals(3, map.size());

        assertEquals("Claus", map.get("name"));
        String price = map.get("price").toString();
        assertTrue(price.startsWith("10"));

        assertEquals(null, map.get("id"));
    }

    public void testAnotherGetProperties() throws Exception {
        AnotherExampleBean bean = new AnotherExampleBean();
        bean.setId("123");
        bean.setName("Claus");
        bean.setPrice(10.0);
        Date date = new Date(0);
        bean.setDate(date);
        bean.setGoldCustomer(true);
        bean.setLittle(true);
        Collection<?> children = new ArrayList<Object>();
        bean.setChildren(children);

        Map<String, Object> map = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(bean, map, null);
        assertEquals(7, map.size());

        assertEquals("Claus", map.get("name"));
        String price = map.get("price").toString();
        assertTrue(price.startsWith("10"));
        assertSame(date, map.get("date"));
        assertSame(children, map.get("children"));
        assertEquals(Boolean.TRUE, map.get("goldCustomer"));
        assertEquals(Boolean.TRUE, map.get("little"));
        assertEquals("123", map.get("id"));
    }

    public void testGetPropertiesOptionPrefix() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);
        bean.setId("123");

        Map<String, Object> map = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(bean, map, "bean.");
        assertEquals(3, map.size());

        assertEquals("Claus", map.get("bean.name"));
        String price = map.get("bean.price").toString();
        assertTrue(price.startsWith("10"));
        assertEquals("123", map.get("bean.id"));
    }

    public void testGetProperty() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setId("123");
        bean.setName("Claus");
        bean.setPrice(10.0);

        Object name = IntrospectionSupport.getProperty(bean, "name");
        assertEquals("Claus", name);
    }

    public void testSetProperty() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setId("123");
        bean.setName("Claus");
        bean.setPrice(10.0);

        IntrospectionSupport.setProperty(bean, "name", "James");
        assertEquals("James", bean.getName());
    }

    public void testAnotherGetProperty() throws Exception {
        AnotherExampleBean bean = new AnotherExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);
        Date date = new Date(0);
        bean.setDate(date);
        bean.setGoldCustomer(true);
        bean.setLittle(true);
        Collection<?> children = new ArrayList<Object>();
        bean.setChildren(children);

        Object name = IntrospectionSupport.getProperty(bean, "name");
        assertEquals("Claus", name);
        assertSame(date, IntrospectionSupport.getProperty(bean, "date"));
        assertSame(children, IntrospectionSupport.getProperty(bean, "children"));
        assertEquals(Boolean.TRUE, IntrospectionSupport.getProperty(bean, "goldCustomer"));
        assertEquals(Boolean.TRUE, IntrospectionSupport.getProperty(bean, "little"));
    }

    public void testGetPropertyLocaleIndependent() throws Exception {
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));

        try {
            ExampleBean bean = new ExampleBean();
            bean.setName("Claus");
            bean.setPrice(10.0);
            bean.setId("1");

            Object name = IntrospectionSupport.getProperty(bean, "name");
            Object id = IntrospectionSupport.getProperty(bean, "id");
            Object price = IntrospectionSupport.getProperty(bean, "price");

            assertEquals("Claus", name);
            assertEquals(10.0, price);
            assertEquals("1", id);
        } finally {
            Locale.setDefault(oldLocale);
        }
    }

    public void testGetPropertyGetter() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Method name = IntrospectionSupport.getPropertyGetter(ExampleBean.class, "name");
        assertEquals("getName", name.getName());

        try {
            IntrospectionSupport.getPropertyGetter(ExampleBean.class, "xxx");
            fail("Should have thrown exception");
        } catch (NoSuchMethodException e) {
            assertEquals("org.apache.camel.util.jndi.ExampleBean.getXxx()", e.getMessage());
        }
    }

    public void testGetPropertySetter() throws Exception {
        ExampleBean bean = new ExampleBean();
        bean.setName("Claus");
        bean.setPrice(10.0);

        Method name = IntrospectionSupport.getPropertySetter(ExampleBean.class, "name");
        assertEquals("setName", name.getName());

        try {
            IntrospectionSupport.getPropertySetter(ExampleBean.class, "xxx");
            fail("Should have thrown exception");
        } catch (NoSuchMethodException e) {
            assertEquals("org.apache.camel.util.jndi.ExampleBean.setXxx", e.getMessage());
        }
    }

    public void testIsGetter() throws Exception {
        ExampleBean bean = new ExampleBean();

        Method name = bean.getClass().getMethod("getName", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(name));
        assertEquals(false, IntrospectionSupport.isSetter(name));

        Method price = bean.getClass().getMethod("getPrice", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(price));
        assertEquals(false, IntrospectionSupport.isSetter(price));
    }

    public void testIsSetter() throws Exception {
        ExampleBean bean = new ExampleBean();

        Method name = bean.getClass().getMethod("setName", String.class);
        assertEquals(false, IntrospectionSupport.isGetter(name));
        assertEquals(true, IntrospectionSupport.isSetter(name));

        Method price = bean.getClass().getMethod("setPrice", double.class);
        assertEquals(false, IntrospectionSupport.isGetter(price));
        assertEquals(true, IntrospectionSupport.isSetter(price));
    }

    public void testOtherIsGetter() throws Exception {
        OtherExampleBean bean = new OtherExampleBean();

        Method customerId = bean.getClass().getMethod("getCustomerId", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(customerId));
        assertEquals(false, IntrospectionSupport.isSetter(customerId));

        Method goldCustomer = bean.getClass().getMethod("isGoldCustomer", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(goldCustomer));
        assertEquals(false, IntrospectionSupport.isSetter(goldCustomer));

        Method silverCustomer = bean.getClass().getMethod("isSilverCustomer", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(silverCustomer));
        assertEquals(false, IntrospectionSupport.isSetter(silverCustomer));

        Method company = bean.getClass().getMethod("getCompany", (Class<?>[]) null);
        assertEquals(true, IntrospectionSupport.isGetter(company));
        assertEquals(false, IntrospectionSupport.isSetter(company));

        Method setupSomething = bean.getClass().getMethod("setupSomething", Object.class);
        assertEquals(false, IntrospectionSupport.isGetter(setupSomething));
        assertEquals(false, IntrospectionSupport.isSetter(setupSomething));
    }

    public void testOtherIsSetter() throws Exception {
        OtherExampleBean bean = new OtherExampleBean();

        Method customerId = bean.getClass().getMethod("setCustomerId", int.class);
        assertEquals(false, IntrospectionSupport.isGetter(customerId));
        assertEquals(true, IntrospectionSupport.isSetter(customerId));

        Method goldCustomer = bean.getClass().getMethod("setGoldCustomer", boolean.class);
        assertEquals(false, IntrospectionSupport.isGetter(goldCustomer));
        assertEquals(true, IntrospectionSupport.isSetter(goldCustomer));

        Method silverCustomer = bean.getClass().getMethod("setSilverCustomer", Boolean.class);
        assertEquals(false, IntrospectionSupport.isGetter(silverCustomer));
        assertEquals(true, IntrospectionSupport.isSetter(silverCustomer));

        Method company = bean.getClass().getMethod("setCompany", String.class);
        assertEquals(false, IntrospectionSupport.isGetter(company));
        assertEquals(true, IntrospectionSupport.isSetter(company));

        Method setupSomething = bean.getClass().getMethod("setupSomething", Object.class);
        assertEquals(false, IntrospectionSupport.isGetter(setupSomething));
        assertEquals(false, IntrospectionSupport.isSetter(setupSomething));
    }
}

