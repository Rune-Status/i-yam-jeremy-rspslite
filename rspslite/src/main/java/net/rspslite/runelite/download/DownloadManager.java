package net.rspslite.runelite.download;

import java.net.URL;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import net.rspslite.runelite.RuneLite;
import net.rspslite.runelite.github.GitHubReleaseManager;
import net.rspslite.localstorage.LocalStorage;

public class DownloadManager {

  public static void downloadIfNecessary() throws IOException {
    if (!RuneLite.isUpToDate()) {
      String latestVersion = RuneLite.getRemoteVersion();
      String jarUrl = GitHubReleaseManager.getJarUrl(latestVersion);
      download(jarUrl);
      RuneLite.setLocalVersion(latestVersion);
    }
  }

  private static void download(String jarUrl) throws IOException {
    URL url = new URL(jarUrl);
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(LocalStorage.getFilePath("launcher.jar"));
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
  }

}
