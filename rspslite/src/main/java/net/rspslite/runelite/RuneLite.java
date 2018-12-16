package net.rspslite.runelite;

import java.io.IOException;
import org.json.JSONException;
import net.rspslite.localstorage.LocalStorage;
import net.rspslite.injector.JarInjector;

/*
 Class for managing and modifying the RuneLite client
*/
public class RuneLite {

  private static final String LAUNCHER_JAR_PATH = LocalStorage.getFilePath("launcher.jar");
  private static final String INJECTED_LAUNCHER_JAR_PATH = LocalStorage.getFilePath("injected-launcher.jar");

  public static void start() {
    try {
      DownloadManager.downloadIfNecessary(LAUNCHER_JAR_PATH);
      JarInjector.inject(LAUNCHER_JAR_PATH,
                         INJECTED_LAUNCHER_JAR_PATH,
                         RuneLiteInjector.getInjectors());
      System.out.println("Finished injecting RuneLite launcher");
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
