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

import com.google.cloud.documentai.v1beta2.Document;
import com.google.cloud.documentai.v1beta2.DocumentUnderstandingServiceClient;
import com.google.cloud.documentai.v1beta2.GcsSource;
import com.google.cloud.documentai.v1beta2.InputConfig;
import com.google.cloud.documentai.v1beta2.ProcessDocumentRequest;
import com.google.edith.interfaces.ExtractReceiptInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses Document AI API to parse pdf receipt file stored in Google Cloud Storage Bucket named
 * edith-step.
 */
public final class ExtractReceiptService implements ExtractReceiptInterface {
  private final DocumentUnderstandingServiceClient client;

  public ExtractReceiptService(DocumentUnderstandingServiceClient client) {
    this.client = client;
  }

  @Override
  public List<Map<String, String>> extractReceipt(String blobKey) throws IOException {
    String projectId = "edith-step";
    String location = "us";
    // For local testing. As blobstore API does not store in GCS in local environment.
    String inputGcsUri =
        "gs://edith-receipts/AAANsUmjLAOYQp4Rn9XphEflkVYntq1WQX4m9oczEGqXTn4m7vce4b3d02B0Qe1jYgF2IGJRHTSN6E3u4FSREZrQgbI.SvrR-Q83SYV-TNgy";
    String parsedText = extractReceipt(projectId, location, inputGcsUri);
    return createItems(parsedText);
  }

  /**
   * This method is because of the shortfall of Document AI API, Natural Language Processing and
   * Cloud Vision API. All of these APIs were not optimal to parse the receipt
   *
   * @return List<Map<String, String>> - a list of maps of item name as key and price as value
   */
  private List<Map<String, String>> createItems(String parsedText) {
    List<Map<String, String>> items = new ArrayList<Map<String, String>>();
    // Split the string on new lines.
    for (String item : parsedText.split("\\r?\\n")) {
      // It is more specific to Kroger as the price ends with B.
      // TODO(prashantneu@) make the algorithm more general considering receipts from other stores.
      if (item.endsWith(" B")) {
        String[] itemText = item.split("\\s+");
        // Only consider the parsed text if it has item descriptions and price.
        if (itemText.length > 1
            && itemText[itemText.length - 2].matches("[-+]?[0-9]*\\.?[0-9]+")
            && !itemText[0].matches("[-+]?[0-9]*\\.?[0-9]+")) {
          items.add(processItem(itemText));
        }
      }
    }
    return items;
  }

  /**
   * Calls Document AI API and parses the pdf file as a long string.
   *
   * @return String - string representation of the content of the receipt pdf file.
   */
  private String extractReceipt(String projectId, String location, String inputGcsUri)
      throws IOException {

    String parent = String.format("projects/%s/locations/%s", projectId, location);
    GcsSource uri = GcsSource.newBuilder().setUri(inputGcsUri).build();

    InputConfig config =
        InputConfig.newBuilder().setGcsSource(uri).setMimeType("application/pdf").build();

    ProcessDocumentRequest request =
        ProcessDocumentRequest.newBuilder().setParent(parent).setInputConfig(config).build();

    Document response = client.processDocument(request);

    // Return all of the document text as one big string
    return response.getText();
  }

  /**
   * Given a item description string, creates a map where item name is key and price is value
   *
   * @return Map<String, String> - a map where item name as key and price as value
   */
  private Map<String, String> processItem(String[] itemText) {
    int itemPriceIndex = itemText.length - 2;
    int index = 0;
    String itemName = "";
    // Combines the splitted text into a single item description.
    while (index < itemPriceIndex) {
      itemName = itemName + " " + itemText[index++];
    }
    Map<String, String> itemFields = new HashMap<String, String>();
    itemFields.put("itemPrice", itemText[itemPriceIndex]);
    itemFields.put("itemName", itemName.trim());
    return itemFields;
  }
}
