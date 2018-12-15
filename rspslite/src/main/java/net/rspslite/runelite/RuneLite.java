package net.rspslite.runelite;

import net.rspslite.runelite.download.DownloadManager;
import net.rspslite.runelite.github.GitHubReleaseManager;

/*
 Class for managing and modifying the RuneLite client
*/
public class RuneLite {

  public static void start() {
    DownloadManager.downloadIfNecessary();
  }

  private static String getLocalVersion() {
    return "TODO";
  }

  private static String getRemoteVersion() {
    return GitHubReleaseManager.getLatestVersion();
  }

  public static boolean isUpToDate() {
    return getRemoteVersion().equals(getLocalVersion());
  }

}
