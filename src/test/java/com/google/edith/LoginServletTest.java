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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.edith.services.LoginService;
import com.google.edith.servlets.LoginServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class LoginServletTest {
  private final Map<String, Object> map =
      ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "12345");

  private final LocalServiceTestHelper loggedInTestHelper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvAttributes(map)
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("gmail")
          .setEnvIsAdmin(true)
          .setEnvEmail("user@gmail.com");

  private final LocalServiceTestHelper loggedOutTestHelper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvIsLoggedIn(false);

  private LoginServlet loginServlet;
  private final UserService userService = UserServiceFactory.getUserService();

  @Mock
  HttpServletRequest request;

  @Mock
  HttpServletResponse response;
  
  @Mock
  LoginService loginService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    loginServlet = new LoginServlet(loginService);
  }

  @Test
  // Check if the servlet calls getWriter() method.
  public void doGet_whenUserLoggedIn_callsGetWriterMethod() throws IOException {
    loggedInTestHelper.setUp();
    when(loginService.checkUserLoggedIn()).thenReturn(true);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    loginServlet.doGet(request, response);
    
    verify(response, times(1)).getWriter();
    assertTrue(userService.isUserLoggedIn());
    
    loggedInTestHelper.tearDown();
  }

  @Test
  // Check if the servlet calls checkUserLoggedIn() and createJsonOfUserInfo method of LoginService.
  public void doGet_whenUserLoggedIn_callsRequiredServiceMethods() throws IOException {
    loggedInTestHelper.setUp();
    when(loginService.checkUserLoggedIn()).thenReturn(true);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
    
    loginServlet.doGet(request, response);
    
    assertTrue(userService.isUserLoggedIn());
    verify(loginService, times(1)).checkUserLoggedIn();
    verify(loginService, times(1)).createJsonOfUserInfo();
    
    loggedInTestHelper.tearDown();
  }

  @Test
  // Check if the servlet calls createLogin() method of LoginService.
  public void doGet_whenUserLoggedOut_callsCreateLoginMethod() throws IOException {
    loggedOutTestHelper.setUp();
    when(loginService.checkUserLoggedIn()).thenReturn(false);
    when(loginService.createLoginUrl("/")).thenReturn("/logIn");
    loginServlet.doGet(request, response);
    
    assertFalse(userService.isUserLoggedIn());
    verify(loginService, times(1)).createLoginUrl("/");
    verify(response, times(1)).sendRedirect("/logIn");
    
    loggedOutTestHelper.tearDown();
  }

  @Test
  // Check if storeUserInfoEntityInDatastore method is called when the user is logged in.
  public void doPost_whenUserLoggedIn_callStoreUserInfoEntityInDatastore() throws IOException {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(loginService.checkUserLoggedIn()).thenReturn(true);
    
    loginServlet.doPost(request, response);
    
    verify(loginService, times(1)).storeUserInfoEntityInDatastore(request);
    verify(response).sendRedirect(captor.capture());
    assertEquals("/index.html", captor.getValue());
  }

  @Test
  // Check if storeUserInfoEntityInDatastore method is not called when the user is logged out.
  public void doPost_whenUserLoggedOut_doesNotCallStoreUserInfoEntityInDatastore() throws IOException {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    when(loginService.checkUserLoggedIn()).thenReturn(false);
    
    loginServlet.doPost(request, response);
    
    verify(loginService, times(0)).storeUserInfoEntityInDatastore(request);
    verify(response).sendRedirect(captor.capture());
    assertEquals("/index.html", captor.getValue());
  }
}
