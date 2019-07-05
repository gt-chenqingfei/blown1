package com.shuashuakan.android.tools

import java.io.StringReader
import javax.xml.stream.XMLInputFactory

class ManifestParser {
  val names = mutableListOf<String>()
  val values = mutableListOf<String>()

  val metaData by lazy {
    val map = mutableMapOf<String, String>()
    names.forEachIndexed { index, s ->
      map[s] = values[index]
    }
    map.toMap()
  }

  fun parse(xmlContent: String) {
    val factory = XMLInputFactory.newFactory()
    val xmlReader = factory.createXMLStreamReader(StringReader(xmlContent))
    while (xmlReader.hasNext()) {
      if (xmlReader.isStartElement && xmlReader.name.localPart == "meta-data") {
        for (i in 0..xmlReader.attributeCount) {
          if ("http://schemas.android.com/apk/res/android" == xmlReader.getAttributeNamespace(i)) {
            val name = xmlReader.getAttributeLocalName(i)
            val value = xmlReader.getAttributeValue(i)
            when (name) {
              "name" -> names.add(value)
              "value" -> values.add(value)
              "resource" -> values.add(value)
              else -> throw IllegalStateException("unknown: $name")
            }
          }
        }
      }
      xmlReader.next()
    }
  }
}