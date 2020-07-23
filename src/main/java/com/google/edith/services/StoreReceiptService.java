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

package com.google.edith.services;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.edith.servlets.Receipt;
import com.google.edith.servlets.Item;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

public class StoreReceiptService {
    
  private final DatastoreService datastore;

  public StoreReceiptService(DatastoreService datastore) {
    this.datastore = datastore;
  }
  
  /**
  * Stores Receipt and Item entities in datastore
  * @param receipt - object which holds info of parsed file.
  */
  public void storeEntites(Receipt receipt) {
    storeReceiptEntity(receipt);
  } 

  /**
  * Receives Receipt object and creates entity
  * of type Receipt and stores it in Datastore.
  * @param receipt - object which holds info of parsed file.
  */
  private void storeReceiptEntity(Receipt receipt) {
    String userId = receipt.getUserId();
    String storeName = receipt.getStoreName();
    String date = receipt.getDate();
    String name = receipt.getName();
    String fileUrl = receipt.getFileUrl();
    float totalPrice = receipt.getTotalPrice();
    
    Optional<Entity> optEntity = getUserInfoEntity(userId);
    Entity userInfoEntity = optEntity.get();
    Entity receiptEntity = new Entity("Receipt", userInfoEntity.getKey());
    receiptEntity.setProperty("userId", userId);
    receiptEntity.setProperty("storeName", storeName);
    receiptEntity.setProperty("date", date);
    receiptEntity.setProperty("name", name);
    receiptEntity.setProperty("fileUrl", fileUrl);
    receiptEntity.setProperty("price", totalPrice);
    datastore.put(receiptEntity);
    storeReceiptItemsEntity(receipt, receiptEntity);
  }

  /**
  * Parses the form submitted by user which contains information of
  * the parsed receipt and creates a Receipt object from the JSON string.
  * @param request - request which contains the form body.
  * @return Receipt - Receipt object created from the JSON string.
  */
  public Receipt parseReceiptFromForm(HttpServletRequest request) throws IOException {
    BufferedReader bufferedReader = request.getReader();
    Gson gson = new Gson();
    JsonParser parser = new JsonParser();
    JsonObject json = (JsonObject) parser.parse(bufferedReader);
    String receiptJsonString = json.get("data").getAsString();
    System.out.println(receiptJsonString);
    return gson.fromJson(receiptJsonString, Receipt.class);
  }

  /**
  * Stores parsed item from the form with
  * receiptEntity as a parent in the datastore.
  * @param items - request which contains the form body.
  * @param receiptEntity - request which contains the form body.
  */
  private void storeReceiptItemsEntity(Receipt receipt, Entity receiptEntity) {
    Item[] items = receipt.getItems();
    for (Item item: items) {
      String userId = item.getUserId();
      String itemName = item.getName();
      float price = item.getPrice();
      int quantity = item.getQuantity();
      String category = item.getCategory();
      String expireDate = item.getExpireDate();
  
      Entity itemEntity = new Entity("Item", receiptEntity.getKey());
      itemEntity.setProperty("userId", userId);
      itemEntity.setProperty("name", itemName);
      itemEntity.setProperty("quantity", quantity);
      itemEntity.setProperty("price", price);
      itemEntity.setProperty("category", category);
      itemEntity.setProperty("date", expireDate);
      datastore.put(itemEntity);
    }
  }

  /**
   * Returns the UserInfo entity with user id.
   * Given id is not of UserInfo kind but a field of that kind.
   * @param id - id of the user who is logged in.
   */
  private Optional<Entity> getUserInfoEntity(String id) {
    
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    return Optional.ofNullable(results.asSingleEntity());
  }
}