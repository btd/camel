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
package org.apache.camel.component.websocket;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MemoryWebsocketStoreTest {

    private static final String KEY_1 = "one";
    private static final String KEY_2 = "two";

    @Mock
    private WebsocketConsumer consumer;

    @Mock
    private NodeSynchronization sync;

    @Mock
    private DefaultWebsocket websocket1 = new DefaultWebsocket(sync, consumer);;

    @Mock
    private DefaultWebsocket websocket2 = new DefaultWebsocket(sync, consumer);;

    private MemoryWebsocketStore store;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        store = new MemoryWebsocketStore();
        when(websocket1.getConnectionKey()).thenReturn(KEY_1);
        when(websocket2.getConnectionKey()).thenReturn(KEY_2);
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#add(String, org.apache.camel.component.websocket.DefaultWebsocket)} .
     */
    @Test
    public void testAdd() {
        assertNotNull(websocket1.getConnectionKey());

        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));

        store.add(websocket2);
        assertEquals(websocket2, store.get(KEY_2));
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#add(org.apache.camel.component.websocket.DefaultWebsocket)} .
     */
    @Test(expected = NullPointerException.class)
    public void testAddNullValue() {
        store.add(null);
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#remove(org.apache.camel.component.websocket.DefaultWebsocket)} .
     */
    @Test
    public void testRemoveDefaultWebsocket() {
        // first call of websocket1.getConnectionKey()
        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));
        // second call of websocket1.getConnectionKey()
        store.remove(websocket1);
        assertNull(store.get(KEY_1));

        InOrder inOrder = inOrder(websocket1, websocket2);
        inOrder.verify(websocket1, times(2)).getConnectionKey();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#remove(org.apache.camel.component.websocket.DefaultWebsocket)} .
     */
    @Test
    public void testRemoveDefaultWebsocketKeyNotSet() {
        // first call of websocket1.getConnectionKey()
        store.add(websocket1);

        // overload getConnectionKey() after store.add() - otherwise npe
        when(websocket1.getConnectionKey()).thenReturn(null);

        assertEquals(websocket1, store.get(KEY_1));

        try {
            store.remove(websocket1);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals(NullPointerException.class, e.getClass());
        }

        InOrder inOrder = inOrder(websocket1, websocket2);
        inOrder.verify(websocket1, times(2)).getConnectionKey();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#remove(org.apache.camel.component.websocket.DefaultWebsocket)} .
     */
    @Test
    public void testRemoveNotExisting() {
        websocket1.setConnectionKey(KEY_1);
        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));
        assertNull(store.get(KEY_2));
        store.remove(websocket2);
        assertEquals(websocket1, store.get(KEY_1));
        assertNull(store.get(KEY_2));

        InOrder inOrder = inOrder(websocket1, websocket2);
        inOrder.verify(websocket2, times(1)).getConnectionKey();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#remove(String)} .
     */
    @Test
    public void testRemoveString() {
        websocket1.setConnectionKey(KEY_1);
        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));
        store.remove(KEY_1);
        assertNull(store.get(KEY_1));
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#remove(String)} .
     */
    @Test
    public void testRemoveStringNotExisting() {
        websocket1.setConnectionKey(KEY_1);
        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));
        assertNull(store.get(KEY_2));
        store.remove(KEY_2);
        assertEquals(websocket1, store.get(KEY_1));
        assertNull(store.get(KEY_2));
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#get(String)} .
     */
    @Test
    public void testGetString() {
        websocket1.setConnectionKey(KEY_1);
        store.add(websocket1);
        assertEquals(websocket1, store.get(KEY_1));
        assertNull(store.get(KEY_2));
        websocket2.setConnectionKey(KEY_2);
        store.add(websocket2);
        assertEquals(websocket1, store.get(KEY_1));
        assertEquals(websocket2, store.get(KEY_2));
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.MemoryWebsocketStore#getAll()} .
     */
    @Test
    public void testGetAll() {
        Collection<DefaultWebsocket> sockets = store.getAll();
        assertNotNull(sockets);
        assertEquals(0, sockets.size());

        websocket1.setConnectionKey(KEY_1);
        store.add(websocket1);
        sockets = store.getAll();
        assertNotNull(sockets);
        assertEquals(1, sockets.size());
        assertTrue(sockets.contains(websocket1));

        websocket2.setConnectionKey(KEY_2);
        store.add(websocket2);
        sockets = store.getAll();
        assertNotNull(sockets);
        assertEquals(2, sockets.size());
        assertTrue(sockets.contains(websocket1));
        assertTrue(sockets.contains(websocket2));
    }
}
