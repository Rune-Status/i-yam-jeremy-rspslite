package net.rspslite.runelite.launcher;

import java.net.URL;
import java.util.Scanner;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class GitHubReleaseManager {

  private static final String RELEASES_API_URL = "https://api.github.com/repos/runelite/launcher/releases";

  private static String latestVersion = null;

  public static String getLatestVersion() throws IOException, JSONException {
    if (GitHubReleaseManager.latestVersion != null) {
      return GitHubReleaseManager.latestVersion;
    }
    else {
      URL url = new URL(RELEASES_API_URL);
      Scanner scanner = new Scanner(url.openStream());
      scanner.useDelimiter("\\Z");
      String jsonString = scanner.next();
      scanner.close();

      JSONArray releases = new JSONArray(jsonString);
      JSONObject latestRelease = (JSONObject) releases.get(0);
      GitHubReleaseManager.latestVersion = (String) latestRelease.get("tag_name");
      System.out.println("Found latest RuneLite launcher version: " + latestVersion);
      return GitHubReleaseManager.latestVersion;
    }
  }

  public static String getJarUrl(String version) {
    return "https://github.com/runelite/launcher/releases/download/" + version + "/RuneLite.jar";
  }

}
