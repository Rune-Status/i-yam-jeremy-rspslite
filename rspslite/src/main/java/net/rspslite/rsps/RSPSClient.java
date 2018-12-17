package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.applet.Applet;
import net.rspslite.injector.JarInjector;

public class RSPSClient {

  private static String RSPS_CLIENT_JAR_PATH = "~/alora/client.jar";
  private static String RSPS_CLIENT_MAIN_CLASS = "Alora";
  private static String[] RSPS_CLIENT_JAR_DEPENDENCIES = new String[]{};

  public static Applet getApplet() {
    injectClient();

    try {
      URL[] jarUrls = toUrls(RSPS_CLIENT_JAR_PATH, RSPS_CLIENT_JAR_DEPENDENCIES);

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

  private static void injectClient() {
    String tmpInjectedJarPath = RSPS_CLIENT_JAR_PATH + ".tmp";

    try {
      JarInjector.inject(RSPS_CLIENT_JAR_PATH,
                         tmpInjectedJarPath,
                         RSPSClientInjector.getInjectors(),
                         RSPS_CLIENT_JAR_DEPENDENCIES,
                         (name) -> false);

      File clientJar = new File(RSPS_CLIENT_JAR_PATH);
      File tmpInjectedJar = new File(tmpInjectedJarPath);
      clientJar.delete();
      boolean success = tmpInjectedJar.renameTo(clientJar);

      if (!success) {
        System.err.println("RSPS injected client JAR could not be moved to replace original");
        System.exit(1);
      }
    }
    catch (IOException e) {
      System.err.println("RSPS client could not be injected");
      System.exit(1);
    }
  }

}
