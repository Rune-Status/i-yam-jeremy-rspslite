package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.applet.Applet;
import net.rspslite.injector.JarInjector;
import net.rspslite.localstorage.LocalStorage;

public class RSPSClient {

  private static final String RSPS_CLIENT_JAR_PATH = System.getProperty("user.home") + File.separator + "alora/client.jar";
    private static final String RSPS_INJECTED_CLIENT_JAR_PATH = System.getProperty("user.home") + File.separator + "alora/injected-client.jar";
  private static final String RSPS_CLIENT_MAIN_CLASS = "Alora";
  private static final String[] RSPS_CLIENT_JAR_DEPENDENCIES = new String[]{
                                                                            System.getProperty("user.home") + File.separator + "alora/clientlibs.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/discord-rpc-release-v3.3.0.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/Theme.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/jna-4.5.2.jar",

                                                                            // RuneLite Dependencies
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/runescape-api-1.5.4.jar",
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/runelite-api-1.5.4.jar",
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/slf4j-api-1.7.25.jar"
                                                                          };

  private static final String OSRS_INJECTED_CLIENT_PATH = LocalStorage.getFilePath("osrs-injected-client.jar");
  private static final String[] OSRS_INJECTED_CLIENT_JAR_DEPENDENCIES = new String[]{"/Users/i-yam-jeremy/.runelite/repository2/runescape-api-1.5.4.jar", "/Users/i-yam-jeremy/.runelite/repository2/runelite-api-1.5.4.jar"};

  public static Applet getApplet() {
    injectClient(OSRS_INJECTED_CLIENT_PATH, OSRS_INJECTED_CLIENT_JAR_DEPENDENCIES, RSPS_CLIENT_JAR_PATH, RSPS_CLIENT_JAR_DEPENDENCIES, RSPS_INJECTED_CLIENT_JAR_PATH);

    try {
      URL[] jarUrls = toUrls(RSPS_INJECTED_CLIENT_JAR_PATH, RSPS_CLIENT_JAR_DEPENDENCIES);

      ClassLoader clientLoader = new URLClassLoader(jarUrls, RSPSClient.class.getClassLoader());

      Class<?> clientClass = clientLoader.loadClass(RSPS_CLIENT_MAIN_CLASS);
      Applet applet = (Applet) clientClass.newInstance();
      return applet;
    } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      System.err.println("RSPS Applet class could not be instantiated.");
      e.printStackTrace();
      return null;
    }
  }

  private static URL[] toUrls(String mainJarPath, String[] jarDependencies) throws IOException {
    URL[] jarUrls = new URL[1 + jarDependencies.length];
    jarUrls[0] = urlFromFilePath(mainJarPath);
    for (int i = 0; i < jarDependencies.length; i++) {
      jarUrls[1+i] = urlFromFilePath(jarDependencies[i]);
    }
    return jarUrls;
  }

  private static URL urlFromFilePath(String path) throws IOException {
    return new File(path).toURI().toURL();
  }

  private static void injectClient(String osrsInjectedClientJarPath, String[] osrsJarDependencies, String rspsClientJarPath, String[] rspsJarDependencies, String rspsInjectedClientJarPath) {

    try {
      URL[] osrsJars = toUrls(osrsInjectedClientJarPath, osrsJarDependencies);
      URL[] rspsJars = toUrls(rspsClientJarPath, rspsJarDependencies);

      JarInjector.inject(rspsClientJarPath,
                         rspsInjectedClientJarPath,
                         RSPSClientInjector.getInjectors(osrsInjectedClientJarPath, osrsJars, rspsClientJarPath, rspsJars),
                         rspsJarDependencies,
                         (name) -> false);
    }
    catch (IOException e) {
      System.err.println("RSPS client could not be injected");
      e.printStackTrace();
      System.exit(1);
    }
  }

}
