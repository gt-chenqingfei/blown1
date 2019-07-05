package com.shuashuakan.android.spider;

import okio.ByteString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by twocity on 1/12/17.
 */

class HMACDigestCreator implements DigestCreator {
  private static final String SECRET_KEY = "5fa87362043c1e75b2c2fa0e339bcf94";
  private final SaltProvider saltProvider;

  HMACDigestCreator(SaltProvider saltProvider) {
    this.saltProvider = saltProvider;
  }

  @Override public String createDigest(Map<String, Object> data) throws IOException {
    String keyAndValue = concatKeyAndValues(data);
    String md5OfKeyValues = Utils.md5hash(keyAndValue);
    String message = md5OfKeyValues + "&" + saltProvider.get();
    byte[] bytes = Utils.sha256HMAC(message, SECRET_KEY);
    return ByteString.of(bytes).base64();
  }

  interface SaltProvider {
    String get();
  }

  private static String concatKeyAndValues(Map<String, Object> objectMap)
      throws UnsupportedEncodingException {
    TreeMap<String, Object> newSortedMap = new TreeMap<>();
    newSortedMap.putAll(objectMap);
    StringBuilder stringBuilder = new StringBuilder();

    for (Map.Entry<String, Object> entry : newSortedMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (!Utils.isBlank(String.valueOf(value))) {
        stringBuilder.append(key);
        stringBuilder.append('=');
        stringBuilder.append(encodeUrl(String.valueOf(value)));
        stringBuilder.append('&');
      }
    }

    if (stringBuilder.length() > 0) {
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }

    return stringBuilder.toString();
  }

  private static String encodeUrl(String value) throws UnsupportedEncodingException {
    String encoded = URLEncoder.encode(String.valueOf(value), "UTF-8");
    if (value.indexOf(' ') != -1) {
      return encoded.replace("+", "%20");
    }
    return encoded;
  }
}
