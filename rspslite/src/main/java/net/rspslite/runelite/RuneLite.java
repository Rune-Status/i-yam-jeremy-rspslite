package net.rspslite.runelite;

import java.io.IOException;
import org.json.JSONException;
import net.rspslite.localstorage.LocalStorage;
import net.rspslite.runelite.download.DownloadManager;
import net.rspslite.runelite.github.GitHubReleaseManager;

/*
 Class for managing and modifying the RuneLite client
*/
public class RuneLite {

  public static void start() {
    try {
      DownloadManager.downloadIfNecessary();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getLocalVersion() {
    return LocalStorage.getLocalData("launcher_version");
  }

  public static void setLocalVersion(String version) {
    LocalStorage.setLocalData("launcher_version", version);
  }

  public static String getRemoteVersion() throws IOException, JSONException {
    return GitHubReleaseManager.getLatestVersion();
  }

  public static boolean isUpToDate() throws IOException {
    return getRemoteVersion().equals(getLocalVersion());
  }

}
