package net.rspslite.runelite;

import java.net.URL;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import net.rspslite.runelite.launcher.Launcher;
import net.rspslite.runelite.launcher.GitHubReleaseManager;

public class DownloadManager {

  public static void downloadIfNecessary(String outputJarPath) throws IOException {
    if (!Launcher.isUpToDate()) {
      updateLauncher(outputJarPath);
    }
    else {
      System.out.println("RuneLite launcher up to date. Skipped download");
    }
  }

  private static void updateLauncher(String outputJarPath) throws IOException {
    String latestVersion = Launcher.getRemoteVersion();
    String jarUrl = GitHubReleaseManager.getJarUrl(latestVersion);
    download(jarUrl, outputJarPath);
    Launcher.setLocalVersion(latestVersion);
    System.out.println("Updated RuneLite launcher to " + latestVersion);
  }

  private static void download(String urlString, String outputPath) throws IOException {
    URL url = new URL(urlString);
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(outputPath);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
  }

}
