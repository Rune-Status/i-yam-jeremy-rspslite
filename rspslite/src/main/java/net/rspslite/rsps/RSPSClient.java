package net.rspslite.rsps;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.applet.Applet;
import net.rspslite.injector.JarInjector;

public class RSPSClient {

  private static String RSPS_CLIENT_JAR_PATH = "~/alora/client.jar";
  private static String RSPS_CLIENT_MAIN_CLASS = "Alora";
  private static String[] RSPS_CLIENT_JAR_DEPENDENCIES = new String[]{};

  public static Applet getApplet() {
    injectClient();

    try {
      Map<String, byte[]> zipData = new HashMap<>();
      JarInjector.mapEntries(RSPS_CLIENT_JAR_PATH, (name, data) -> {
        zipData.put(name, data);
      });

      ClassLoader clientLoader = new ClassLoader(RSPSClient.class.getClassLoader()) {

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException { // mimics code from net.runelite.client.rs.ClientLoader
          String file = name.replace(".", "/") + ".class";
          byte[] data = zipData.get(file);
          if (data == null) {
            throw new ClassNotFoundException(name);
          }

          return defineClass(name, data, 0, data.length);
        }

      };

      Class<?> clientClass = clientLoader.loadClass(RSPS_CLIENT_MAIN_CLASS);
      Applet applet = (Applet) clientClass.newInstance();
      return applet;
    } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      System.err.println("RSPS Applet class could not be instantiated.");
      e.printStackTrace();
      return null;
    }
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
