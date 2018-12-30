package net.rspslite.rsps.hooks;

import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HookReader {

  private static String HOOK_FILE_RESOURCE_URL = "http://localhost:8080/rsps_client_hooks.json";

  public static Hook[] readHooks() {
    try {
      InputStream input = new URL(HOOK_FILE_RESOURCE_URL).openStream();
      Scanner scanner = new Scanner(input);
      scanner.useDelimiter("\\A");
      String json = scanner.next();
      scanner.close();

      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();
      return gson.fromJson(json, Hook[].class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

}
