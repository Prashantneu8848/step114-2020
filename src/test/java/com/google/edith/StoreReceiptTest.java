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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.edith.interfaces.StoreReceiptInterface;
import com.google.edith.servlets.Item;
import com.google.edith.servlets.Receipt;
import com.google.edith.servlets.StoreReceipt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StoreReceiptTest {

  private final LocalServiceTestHelper testHelper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private StoreReceipt storeReceipt;

  @Mock StoreReceiptInterface storeReceiptImplementation;

  @Mock HttpServletRequest request;

  @Mock HttpServletResponse response;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testHelper.setUp();
    storeReceipt = new StoreReceipt(storeReceiptImplementation);
  }

  @After
  public void tearDown() {
    testHelper.tearDown();
  }

  @Test
  public void doPost_testServiceMethodsAreCalled() throws IOException {
    Reader inputString = new StringReader("testJson");
    BufferedReader reader = new BufferedReader(inputString);
    Item item1 =
        Item.builder()
            .setUserId("23")
            .setName("apple")
            .setPrice((float) 1.5)
            .setQuantity(2)
            .setDate("yy")
            .setCategory("fruit")
            .setExpiration("date")
            .build();
    Item[] items = {item1};
    Receipt receipt = new Receipt("23", "kro", "date", "exp", "url", 0.5f, items);
    when(request.getReader()).thenReturn(reader);
    when(storeReceiptImplementation.parseReceiptFromForm(reader)).thenReturn(receipt);

    storeReceipt.doPost(request, response);

    verify(storeReceiptImplementation, times(1)).parseReceiptFromForm(reader);
    verify(storeReceiptImplementation, times(1)).storeEntites(receipt);
    verify(response, times(1)).sendRedirect("/");
  }
}
