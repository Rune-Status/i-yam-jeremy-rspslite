package net.rspslite.runelite.launcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONException;
import net.rspslite.localstorage.LocalStorage;
import net.rspslite.injector.JarInjector;
import net.rspslite.runelite.DownloadManager;

/*
 Class for managing and modifying the RuneLite launcher
*/
public class Launcher {

  private static final String LAUNCHER_JAR_PATH = LocalStorage.getFilePath("launcher.jar");
  private static final String INJECTED_LAUNCHER_JAR_PATH = LocalStorage.getFilePath("injected-launcher.jar");

  public static void start() {
    try {
      DownloadManager.downloadIfNecessary(LAUNCHER_JAR_PATH);
      JarInjector.inject(LAUNCHER_JAR_PATH,
                         INJECTED_LAUNCHER_JAR_PATH,
                         LauncherInjector.getInjectors());
      runLauncher(INJECTED_LAUNCHER_JAR_PATH);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void runLauncher(String launcherPath) throws IOException {
    try {
      URL launcherUrl = new File(INJECTED_LAUNCHER_JAR_PATH).toURI().toURL();
      URLClassLoader launcherLoader = new URLClassLoader(new URL[]{launcherUrl}, Launcher.class.getClassLoader());
      Class<?> mainClass = Class.forName("net.runelite.launcher.Launcher", true, launcherLoader);
      Method main = mainClass.getDeclaredMethod("main", new Class<?>[]{String[].class});
      main.invoke(null, new Object[]{new String[]{"--debug", "--nojvm"}});
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      System.err.println("Launcher could not be started");
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

  public static void launchRuneLiteClient(List<?> jarFiles, String clientArgs, String clientMainClass) throws MalformedURLException { // mimics code from net.runelite.launcher.ReflectionLauncher
    URL[] jarUrls = new URL[jarFiles.size()];
		int i = 0;
		for (Object file : jarFiles) {
			jarUrls[i++] = ((File)file).toURI().toURL();
		}

		URLClassLoader loader = new URLClassLoader(jarUrls, Launcher.class.getClassLoader()); // the changed line (includes this classloader so the RuneLite client has access to all necessary classes)

		javax.swing.UIManager.put("ClassLoader", loader); // hack for Substance
		Thread thread = new Thread() {
			public void run() {
				try {
					Class<?> mainClass = loader.loadClass(clientMainClass);

					Method main = mainClass.getMethod("main", String[].class);

					String[] args = clientArgs != null ? clientArgs.split(" ") : new String[0];
					main.invoke(null, (Object) args);
				}
				catch (Exception e) {
					System.err.println("Unable to launch RuneLite client");
          e.printStackTrace();
				}
			}
		};
		thread.setName("RuneLite");
    thread.start();
  }

}
