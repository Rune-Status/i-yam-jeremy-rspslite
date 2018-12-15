package net.rspslite.localstorage;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class LocalStorage {

  private static final String DIR_PATH = System.getProperty("user.home") + File.separator + "rspslite";
  private static final File DIR = new File(DIR_PATH);
  private static final File LOCAL_DATA_FILE = new File(getFilePath("local_data"));

  public static String getFilePath(String relativePath) {
    createDirIfNecessary();
    return DIR_PATH + File.separator + relativePath;
  }

  private static void createDirIfNecessary() {
    if (!DIR.exists()) {
      DIR.mkdir();
    }
  }

  public static String getLocalData(String key) {
    String localData = readLocalDataFile();

    for (String line : localData.split("\n")) {
      String[] split = line.split("=");
      if (split.length >= 2) {
        String currentKey = split[0];
        String currentValue = split[1];

        if (currentKey.equals(key)) {
          return currentValue;
        }
      }
    }

    return null;
  }

  public static void setLocalData(String key, String value) {
    String localData = readLocalDataFile();
    String[] lines = localData.split("\n");
    boolean keyAlreadyExists = false;

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      String[] split = line.split("=");
      if (split.length >= 2) {
        String currentKey = split[0];
        String currentValue = split[1];

        if (currentKey.equals(key)) {
          lines[i] = (key + "=" + value);
          keyAlreadyExists = true;
          break;
        }
      }
    }

    String newContents;
    if (keyAlreadyExists) {
      newContents = String.join("\n", lines);
    }
    else {
      newContents = localData + "\n" + (key + "=" + value);
    }
    writeLocalDataFile(newContents);
  }

  private static void writeLocalDataFile(String contents) {
    try {
      LOCAL_DATA_FILE.delete();
      FileWriter writer = new FileWriter(LOCAL_DATA_FILE, false);
      writer.write(contents);
      writer.close();
    } catch (IOException e) {
      System.err.println("Error: Couldn't write to local data file");
      e.printStackTrace();
    }
  }

  private static String readLocalDataFile() {
    if (!LOCAL_DATA_FILE.exists()) {
      return "";
    }

    try {
      Scanner in = new Scanner(LOCAL_DATA_FILE);
      in.useDelimiter("\\Z");
      String data = in.next();
      in.close();

      return data;
    } catch (FileNotFoundException e) {
      System.err.println("Error: Couldn't load local data file");
      e.printStackTrace();
      return "";
    }
  }

}
