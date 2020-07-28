// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.edith;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.edith.services.SearchService;
import com.google.edith.servlets.SearchServlet;
import com.google.edith.servlets.Receipt;
import com.google.edith.servlets.Item;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.mockito.ArgumentCaptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class SearchServletTest {
  
  private SearchServlet searchServlet;
  private final UserService userService = UserServiceFactory.getUserService();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private Map<String, Object> map = ImmutableMap
            .of("com.google.appengine.api.users.UserService.user_id_key", "12345");
   private LocalServiceTestHelper testHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(),
        new LocalUserServiceTestConfig())
        .setEnvAttributes(map)
        .setEnvIsLoggedIn(true)
        .setEnvAuthDomain("gmail")
        .setEnvIsAdmin(true)
        .setEnvEmail("user@gmail.com");

  @Mock
  SearchService searchService;

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testHelper.setUp();
    searchServlet = new SearchServlet(searchService);
  }

  @After
  public void tearDown() {
    testHelper.tearDown();
  }

  // When entity kind is Receipt then createReceiptObjects method should be called.
  @Test
  public void create_ReceiptObjects_whenEntityKindIsReceipt() throws IOException {
    when(request.getParameter("kind")).thenReturn("Receipt");
    when(request.getParameter("name")).thenReturn("weekend");
    List<Entity> entities = Collections.emptyList();
    Receipt[] receipts = new Receipt[1];
    when(searchService.findEntityFromDatastore("weekend", "", "Receipt", "", ""))
          .thenReturn(entities);
    when(searchService.createReceiptObjects(entities)).thenReturn(receipts);
    searchServlet.doPost(request, response);
    verify(searchService, times(1)).createReceiptObjects(entities);
    verify(searchService, times(0)).createItemObjects(entities);
  }

  // When entity kind is Receipt then createReceiptObjects method should be called.
  @Test
  public void create_ItemObjects_whenEntityKindIsItem() throws IOException {
    when(request.getParameter("kind")).thenReturn("Item");
    when(request.getParameter("name")).thenReturn("apple");
    List<Entity> entities = Collections.emptyList();
    Item[] items = new Item[1];
    when(searchService.findEntityFromDatastore("apple", "", "Item", "", ""))
          .thenReturn(entities);
    when(searchService.createItemObjects(entities)).thenReturn(items);
    searchServlet.doPost(request, response);
    verify(searchService, times(1)).createItemObjects(entities);
    verify(searchService, times(0)).createReceiptObjects(entities);
  }

  // Redirect to search-results section after form is submitted.
  @Test
  public void check_redirectAfterFormSubmission_redirects() throws IOException {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    searchServlet.doPost(request, response);
    verify(response).sendRedirect(captor.capture());
    assertEquals("/#search-results", captor.getValue());
  }

  // Check if getWriter method is called for Receipt kind.
  @Test
  public void check_getWriterIsCalledForReceipt_verifiesMethodCall() throws IOException {
    setUpPostForReceipt();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    searchServlet.doGet(request, response);
    verify(response, times(1)).getWriter();
  }

  // Check if receipt response has all fields.
  @Test
  public void check_responseFields_containsAllReceiptFields() throws IOException {
    setUpPostForReceipt();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    searchServlet.doGet(request, response);
    String servletResponse = stringWriter.toString();
    assertTrue(servletResponse.contains("userId"));
    assertTrue(servletResponse.contains("storeName"));
    assertTrue(servletResponse.contains("date"));
    assertTrue(servletResponse.contains("name"));
    assertTrue(servletResponse.contains("fileUrl"));
    assertTrue(servletResponse.contains("totalPrice"));
    assertTrue(servletResponse.contains("items"));
  }

  // Check if getWriter method is called for Item kind.
  @Test
  public void check_getWriterIsCalledForItem_verifiesMethodCall() throws IOException {
    setUpPostForItem();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    searchServlet.doGet(request, response);
    verify(response, times(1)).getWriter();
  }

  // Check if item response has all fields.
  @Test
  public void check_responseFields_containsAllItemFields() throws IOException {
    setUpPostForItem();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    searchServlet.doGet(request, response);
    String servletResponse = stringWriter.toString();
    System.out.println(servletResponse);

    assertTrue(servletResponse.contains("userId"));
    assertTrue(servletResponse.contains("name"));
    assertTrue(servletResponse.contains("price"));
    assertTrue(servletResponse.contains("quantity"));
    assertTrue(servletResponse.contains("category"));
    assertTrue(servletResponse.contains("expireDate"));
  }

  // Sets up conditions by populating kind and receipt field of SearchServlet class.
  private void setUpPostForReceipt() throws IOException {
    when(request.getParameter("kind")).thenReturn("Receipt");
    when(request.getParameter("name")).thenReturn("weekend");
    List<Entity> entities = Collections.emptyList();
    Item item1 = new Item("12345", "apple", 1.5f, 2, "fruit", "date");
    Item[] items = {item1};
    Receipt receipt1 = new Receipt("12345","kro", "unknown", "weekend", "url1", 2.5f, items);
    Receipt[] receipts = {receipt1};

    when(searchService.findEntityFromDatastore("weekend", "", "Receipt", "", ""))
          .thenReturn(entities);
    when(searchService.createReceiptObjects(entities)).thenReturn(receipts);
    searchServlet.doPost(request, response);
  }

  // Sets up conditions by populating kind and item field of SearchServlet class.
  private void setUpPostForItem() throws IOException {
    when(request.getParameter("kind")).thenReturn("Item");
    when(request.getParameter("name")).thenReturn("weekend");
    List<Entity> entities = Collections.emptyList();
    Item item1 = new Item("12345", "apple", 1.5f, 2, "fruit", "date");
    Item[] items = {item1};
    when(searchService.findEntityFromDatastore("apple", "", "Item", "", ""))
          .thenReturn(entities);
    when(searchService.createItemObjects(entities)).thenReturn(items);
    searchServlet.doPost(request, response);
  }
}
