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

package com.google.edith.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Optional;
import java.util.NoSuchElementException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that checks if user is logged in.
 * if logged in then provides with user information along with logout url.
 * if not logged in then redirectes to a url to log in.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (userService.isUserLoggedIn()) {
      Gson gson = new Gson();
      String json = gson.toJson(createUserInfo(userService));
      response.setContentType("application/json");
      response.getWriter().println(json);
    } else {
      String loginUrl = userService.createLoginURL("/");
      response.sendRedirect(loginUrl);
    }
  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String firstName = getParameter(request, "first-name").orElse("");
    String lastName = getParameter(request, "last-name").orElse("");
    String userName = getParameter(request, "username").orElse("");
    String favoriteStore = getParameter(request, "favorite-store").orElse("");

    UserService userService = UserServiceFactory.getUserService();
    String id = userService.getCurrentUser().getUserId();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Do not create another entity to set nickname if it already exists.
    Entity userInfoEntity = getUserInfoEntity(id).orElseGet(() -> {
      Entity info = new Entity("UserInfo");
      info.setProperty("id", id);
      return info;
    });

    userInfoEntity.setProperty("firstName", firstName);
    userInfoEntity.setProperty("lastName", lastName);
    userInfoEntity.setProperty("userName", userName);
    userInfoEntity.setProperty("favoriteStore", favoriteStore);

    datastore.put(userInfoEntity);

    response.sendRedirect("/");
  }

  private Optional<String> getParameter(HttpServletRequest request, String name) {
    return Optional.ofNullable(request.getParameter(name));
  }

  /**
   * Returns the UserInfo entity with user id.
   * Given id is not of UserInfo kind but a field of that kind.
   */
  private Optional<Entity> getUserInfoEntity(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    return Optional.ofNullable(results.asSingleEntity());
  }
  
  /**
   * Creates UserInfo object encapsulating user data.
   * @param userService - provides information about ther logged in user.
   * @return UserInfo - wrapper object for user information and logout url.
   */
  private UserInfo createUserInfo(UserService userService) {
    User user = userService.getCurrentUser();
    String logoutUrl = userService.createLogoutURL("/");
    String firstName = "";
    String lastName = "";
    String userName = "";
    String favoriteStore = "";
    
    Optional<Entity> optEntity = getUserInfoEntity(user.getUserId());
    
    if (optEntity.isPresent()) {
      Entity userInfoEntity = optEntity.get();
      firstName = (String) userInfoEntity.getProperty("firstName");
      lastName = (String) userInfoEntity.getProperty("lastName");
      userName = (String) userInfoEntity.getProperty("userName");
      favoriteStore = (String) userInfoEntity.getProperty("favoriteStore");
    }

    UserInfo userInfo = UserInfo.builder()
        .setFirstName(firstName)
        .setLastName(lastName)
        .setUserName(userName)
        .setFavoriteStore(favoriteStore)
        .setEmail(user.getEmail())
        .setUserId(user.getUserId())
        .setLogOutUrl(logoutUrl)
        .build();

    return userInfo;
  }
}