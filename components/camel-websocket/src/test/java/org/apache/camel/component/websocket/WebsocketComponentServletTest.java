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

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebsocketComponentServletTest {

    private static final String PROTOCOL = "ws";
    private static final String MESSAGE = "message";
    private static final String CONNECTION_KEY = "random-connection-key";

    @Mock
    private WebsocketConsumer consumer;

    @Mock
    private NodeSynchronization sync;

    @Mock
    private HttpServletRequest request;

    private WebsocketComponentServlet websocketComponentServlet;

    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        websocketComponentServlet = new WebsocketComponentServlet(sync);
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.WebsocketComponentServlet#getConsumer()} .
     */
    @Test
    public void testGetConsumer() {
        assertNull(websocketComponentServlet.getConsumer());
        websocketComponentServlet.setConsumer(consumer);
        assertEquals(consumer, websocketComponentServlet.getConsumer());
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.WebsocketComponentServlet#setConsumer(org.apache.camel.component.websocket.WebsocketConsumer)} .
     */
    @Test
    public void testSetConsumer() {
        testGetConsumer();
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.WebsocketComponentServlet#doWebSocketConnect(javax.servlet.http.HttpServletRequest, String)} .
     */
    @Test
    public void testDoWebSocketConnect() {
        websocketComponentServlet.setConsumer(consumer);
        WebSocket webSocket = websocketComponentServlet.doWebSocketConnect(request, PROTOCOL);
        assertNotNull(webSocket);
        assertEquals(DefaultWebsocket.class, webSocket.getClass());
        DefaultWebsocket defaultWebsocket = (DefaultWebsocket) webSocket;
        defaultWebsocket.setConnectionKey(CONNECTION_KEY);
        defaultWebsocket.onMessage(MESSAGE);
        InOrder inOrder = inOrder(consumer, sync, request);
        inOrder.verify(consumer, times(1)).sendExchange(CONNECTION_KEY, MESSAGE);
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Test method for {@link org.apache.camel.component.websocket.WebsocketComponentServlet#doWebSocketConnect(javax.servlet.http.HttpServletRequest, String)} .
     */
    @Test
    public void testDoWebSocketConnectConsumerIsNull() {
        WebSocket webSocket = websocketComponentServlet.doWebSocketConnect(request, PROTOCOL);
        assertNotNull(webSocket);
        assertEquals(DefaultWebsocket.class, webSocket.getClass());
        DefaultWebsocket defaultWebsocket = (DefaultWebsocket) webSocket;
        defaultWebsocket.setConnectionKey(CONNECTION_KEY);
        defaultWebsocket.onMessage(MESSAGE);
        InOrder inOrder = inOrder(consumer, sync, request);
        inOrder.verifyNoMoreInteractions();
    }
}
