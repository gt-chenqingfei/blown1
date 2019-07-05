package com.shuashuakan.android.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by twocity on 14-3-14.
 */
public class ParseUtils {
  private ParseUtils() {

  }

  public static String parseEnjoySMS(String msg) {
    if (Strings.isBlank(msg)) return null;
    if (!msg.toUpperCase().contains("ENJOY")) {
      return null;
    }
    if (!msg.contains("验证码：")) {
      return null;
    }
    try {
      String regx = "(?<=.?验证码.?)(\\d+)(?=.?.?非本人操作.?请忽略.?)";
      Pattern pattern = Pattern.compile(regx);
      Matcher matcher = pattern.matcher(msg);
      if (matcher.find()) {
        return matcher.group(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static long parseLongSafely(String content, long defaultValue) {
    if (content == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(content);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static int parseIntSafely(String content, int defaultValue) {
    if (content == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(content);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static float parseFloatSafely(String content, float defaultValue) {
    if (content == null) {
      return defaultValue;
    }
    try {
      return Float.parseFloat(content);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static boolean parseEmail(String str) {
    Pattern p = Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
    Matcher m = p.matcher(str);
    while (m.find()) {
      return true;
    }
    return false;
  }

  public static boolean parsePhone(String mobile) {
    if (Strings.isBlank(mobile)) return false;
    Pattern p = Pattern.compile("\\d{11}");
    Matcher m = p.matcher(mobile);
    return m.matches();
  }
}
