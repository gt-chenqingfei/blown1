@file:JvmName("Main")

package com.shuashuakan.android.tools

import picocli.CommandLine
import picocli.CommandLine.ParameterException
import java.nio.file.Path

fun main(args: Array<String>) {
  val commandLine = CommandLine(FishCommand())
      .registerConverter(Path::class.java, PathConverter())
  try {
    val commands = commandLine.parse(*args)
    commands.forEach {
      (it.getCommand() as RunnableCommand).callCommand()
    }
  } catch (e: ParameterException) {
    println(e.message?.red())
    println("use --help to show usage")
  }
}