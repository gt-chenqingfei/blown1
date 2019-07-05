package com.shuashuakan.android.spider;

import java.io.IOException;
import java.util.Map;

/**
 * Created by twocity on 1/12/17.
 */

public interface DigestCreator {
  String createDigest(Map<String, Object> data) throws IOException;
}
