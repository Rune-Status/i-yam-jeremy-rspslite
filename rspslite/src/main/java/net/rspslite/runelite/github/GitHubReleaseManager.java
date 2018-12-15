package net.rspslite.runelite.github;

import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class GitHubReleaseManager {

  private static String RELEASES_API_URL = "https://api.github.com/repos/runelite/launcher/releases";

  public static String getLatestVersion() {
    try {
      URL url = new URL(RELEASES_API_URL);
      Scanner scanner = new Scanner(url.openStream());
      scanner.useDelimiter("\\Z");
      String jsonString = scanner.next();
      scanner.close();

      JSONArray releases = new JSONArray(jsonString);
      JSONObject latestRelease = (JSONObject) releases.get(0);
      String latestVersion = (String) latestRelease.get("tag_name");
      System.out.println("Latest RuneLite Release: " + latestVersion);
      return latestVersion;
    } catch (IOException | JSONException e) {
      e.printStackTrace();
      return null;
    }
  }

}
