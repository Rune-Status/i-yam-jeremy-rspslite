package net.rspslite.runelite;

import java.net.URL;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class DownloadManager {

  public static void downloadIfNecessary(String outputJarPath) throws IOException {
    if (!RuneLite.isUpToDate()) {
      String latestVersion = RuneLite.getRemoteVersion();
      String jarUrl = GitHubReleaseManager.getJarUrl(latestVersion);
      download(jarUrl, outputJarPath);
      RuneLite.setLocalVersion(latestVersion);
    }
  }

  private static void download(String jarUrl, String outputJarPath) throws IOException {
    URL url = new URL(jarUrl);
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(outputJarPath);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
  }

}
