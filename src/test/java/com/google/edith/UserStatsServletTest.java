package com.google.edith;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.edith.servlets.UserInsightsInterface;
import com.google.edith.servlets.UserStatsServlet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public final class UserStatsServletTest {

<<<<<<< HEAD
  private DatastoreService datastore;
  private final HttpServletRequest request = 
    Mockito.mock(HttpServletRequest.class);
  private final HttpServletResponse response = 
    Mockito.mock(HttpServletResponse.class);
  private final UserInsightsInterface userInsights = 
    Mockito.mock(UserInsightsInterface.class);
  private final LocalServiceTestHelper testHelper =
=======
  private static DatastoreService DATASTORE;
  private static final LocalServiceTestHelper TEST_HELPER =
>>>>>>> fcf4ae8975a5c332bd4e87a71858910900731524
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private static final Gson GSON = new Gson();
  private static final String USER_ID = "userId";

  @Before
  public void setUp() {
    TEST_HELPER.setUp();
    DATASTORE = DatastoreServiceFactory.getDatastoreService();
  }

  @Test
  public void testServlet_doPost_runsCorrectly() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    JsonObject testJson = new JsonObject();
    testJson.addProperty("itemName", "Corn");
    testJson.addProperty("itemUserId", "userId");
    testJson.addProperty("itemCategory", "Vegetable");
    testJson.addProperty("itemPrice", "5.00");
    testJson.addProperty("itemQuantity", "4");
    testJson.addProperty("itemDate", "2020-07-14");
    testJson.addProperty("itemReceiptId", "receiptId");

    String json = GSON.toJson(testJson);
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new UserStatsServlet(datastore, userInsights).doPost(request, response);

    verify(request, Mockito.atLeast(1)).getReader();
    writer.flush();
    Assert.assertTrue(stringWriter.toString().contains("Item posted"));
  }

  @Test
<<<<<<< HEAD
  public void testServlet() throws Exception {
    
=======
  public void testServlet_doGet_runsCorrectly() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    UserInsightsInterface userInsights = Mockito.mock(UserInsightsInterface.class);

>>>>>>> fcf4ae8975a5c332bd4e87a71858910900731524
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    when(userInsights.createJson(USER_ID)).thenReturn("");

    new UserStatsServlet(DATASTORE, userInsights).doGet(request, response);
    verify(userInsights, Mockito.atLeast(1)).createJson(USER_ID);
  }
}
