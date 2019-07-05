package com.shuashuakan.android.utils;

import android.support.annotation.Nullable;

public class Strings {
  private Strings() {
    // No instances.
  }

  public static boolean isBlank(CharSequence string) {
    return (string == null || string.toString().trim().length() == 0);
  }

  public static String valueOrDefault(String string, String defaultString) {
    return isBlank(string) ? defaultString : string;
  }

  public static String truncateAt(String string, int length) {
    return string.length() > length ? string.substring(0, length) : string;
  }

  /**
   * Returns the given string if it is non-null; the empty string otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} itself if it is non-null; {@code ""} if it is null
   */
  public static String nullToEmpty(@Nullable String string) {
    return (string == null) ? "" : string;
  }

  /**
   * Returns the given string if it is nonempty; {@code null} otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} itself if it is nonempty; {@code null} if it is
   * empty or null
   */
  @Nullable public static String emptyToNull(@Nullable String string) {
    return isNullOrEmpty(string) ? null : string;
  }

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * <p>Consider normalizing your string references with {@link #nullToEmpty}.
   * If you do, you can use {@link String#isEmpty()} instead of this
   * method, and you won't need special null-safe forms of methods like {@link
   * String#toUpperCase} either. Or, if you'd like to normalize "in the other
   * direction," converting empty strings to {@code null}, you can use {@link
   * #emptyToNull}.
   *
   * @param string a string reference to check
   * @return {@code true} if the string is null or is the empty string
   */
  public static boolean isNullOrEmpty(@Nullable String string) {
    return string == null || string.length() == 0; // string.isEmpty() in Java 6
  }

  public static int getTextLength(String string) {
    int index, textLength;
    int absSumLength;
    float sumLength = 0;
    textLength = string.length();
    for (index = 0; index < textLength; index++) {
      char c = string.charAt(index);
      sumLength += getCharacterLength(c);
    }
    absSumLength = (int) sumLength;
    if (sumLength - absSumLength > 0.001) {
      return absSumLength + 1;
    } else {
      return absSumLength;
    }
  }

  private static float getCharacterLength(char c) {
    if (c >= 32 && c <= 128) {
      return 0.5f;
    } else {
      return 1;
    }
  }
}
