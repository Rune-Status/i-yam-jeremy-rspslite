package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.net.URL;
import java.net.URLClassLoader;
import java.applet.Applet;
import javassist.CtClass;
import net.rspslite.injector.JarInjector;
import net.rspslite.localstorage.LocalStorage;
import net.rspslite.rsps.hooks.HookReader;
import net.rspslite.rsps.hooks.Hook;

public class RSPSClient {

  private static final String RSPS_CLIENT_JAR_PATH = System.getProperty("user.home") + File.separator + "alora/client.jar";
  private static final String RSPS_INJECTED_CLIENT_JAR_PATH = System.getProperty("user.home") + File.separator + "alora/injected-client.jar";
  private static final String RSPS_CLIENT_MAIN_CLASS = "Alora";
  private static final String[] RSPS_CLIENT_JAR_DEPENDENCIES_FOR_INJECTION = new String[]{
                                                                            System.getProperty("user.home") + File.separator + "alora/clientlibs.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/discord-rpc-release-v3.3.0.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/Theme.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/jna-4.5.2.jar",

                                                                            // RuneLite Dependencies
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/runescape-api-1.5.4.jar",
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/runelite-api-1.5.4.jar",
                                                                            "/Users/i-yam-jeremy/.runelite/repository2/slf4j-api-1.7.25.jar"
                                                                          };

  private static final String[] RSPS_CLIENT_JAR_DEPENDENCIES_FOR_CLASSLOADER = new String[]{
                                                                            System.getProperty("user.home") + File.separator + "alora/clientlibs.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/discord-rpc-release-v3.3.0.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/Theme.jar",
                                                                            System.getProperty("user.home") + File.separator + "alora/jna-4.5.2.jar",
                                                                          };

  private static final String OSRS_INJECTED_CLIENT_PATH = LocalStorage.getFilePath("osrs-injected-client.jar");
  private static final String[] OSRS_INJECTED_CLIENT_JAR_DEPENDENCIES = new String[]{
    "/Users/i-yam-jeremy/.runelite/repository2/runescape-api-1.5.4.jar",
    "/Users/i-yam-jeremy/.runelite/repository2/runelite-api-1.5.4.jar",
    "/Users/i-yam-jeremy/.runelite/repository2/guava-23.2-jre.jar",
    "/Users/i-yam-jeremy/.runelite/repository2/slf4j-api-1.7.25.jar",
  };

  public static Applet getApplet(ClassLoader classLoader) {
    injectClient(OSRS_INJECTED_CLIENT_PATH, OSRS_INJECTED_CLIENT_JAR_DEPENDENCIES, RSPS_CLIENT_JAR_PATH, RSPS_CLIENT_JAR_DEPENDENCIES_FOR_INJECTION, RSPS_INJECTED_CLIENT_JAR_PATH);

    System.err.println("Quitting");
    System.exit(1);

    try {
      URL[] jarUrls = toUrls(RSPS_INJECTED_CLIENT_JAR_PATH, RSPS_CLIENT_JAR_DEPENDENCIES_FOR_CLASSLOADER);

      ClassLoader clientLoader = new URLClassLoader(jarUrls, classLoader);

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

     Hook[] hooks = HookReader.readHooks();
     Map<String, CtClass> classMap = new HashMap<>();
     List<Map<String, Consumer<CtClass>>> injectors = new ArrayList<>();

     injectors.add(RSPSClientInjector.getInjectors(osrsInjectedClientJarPath, osrsJars, rspsClientJarPath, rspsJars));

     for (int i = 0; i < hooks.length; i++) {
       injectors.add(RSPSClientInjector.getHookInjector(hooks[i], classMap));
     }

     JarInjector.inject(rspsClientJarPath,
                        rspsInjectedClientJarPath,
                        injectors,
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
