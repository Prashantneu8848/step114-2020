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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.edith.servlets.ExtractReceipt;
import com.google.edith.servlets.ReceiptData;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class ReceiptDataTest {
  private Map<String, Object> map =
      ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "12345");

  private final LocalServiceTestHelper testHelper =
      new LocalServiceTestHelper(new LocalUserServiceTestConfig())
          .setEnvAttributes(map)
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("gmail")
          .setEnvIsAdmin(true)
          .setEnvEmail("user@gmail.com");

  private ReceiptData receiptData;
  private final UserService userService = UserServiceFactory.getUserService();

  @Mock ExtractReceipt extractReceipts;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testHelper.setUp();
    receiptData = new ReceiptData();
  }

  @After
  public void tearDown() {
    testHelper.tearDown();
  }

  @Test
  public void testExtractReceiptData() throws IOException {
    // try {
    //   Receipt returnedReceipt = receiptData.extractReceiptData();
    //   assertNotNull(returnedReceipt);
    //   assertTrue(returnedReceipt instanceof Receipt);
    // } catch (Exception e) {
    //   fail("must return Receipt Object");
    // }
  }
}
