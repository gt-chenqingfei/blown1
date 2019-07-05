package com.shuashuakan.android.tools

import com.shuashuakan.android.tools.RunnableCommand.InvalidCommandException
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Path
import java.nio.file.Paths

class PathConverter : ITypeConverter<Path> {
  override fun convert(value: String?): Path {
    return Paths.get(value).absPath()
  }
}

interface RunnableCommand {
  @Throws(InvalidCommandException::class)
  fun validate(): Boolean

  fun run()

  fun callCommand() {
    try {
      if (validate()) run()
    } catch (e: InvalidCommandException) {
      println("Error: ${e.message}".red())
    }
  }

  class InvalidCommandException(msg: String) : Exception(msg)
}

fun validateArgs(condition: Boolean, msg: () -> String) {
  if (!condition) {
    throw InvalidCommandException(msg())
  }
}

@Suppress("MemberVisibilityCanPrivate")
@Command(name = "fish",
    header = [FISH_HEADER, "A simple tool for FISH project, use COMMAND --help for command helps", "\n"],
    subcommands = [ApkUploadCommand::class, ApkPatchCommand::class, ApkDumpCommand::class, ApkChannelCommand::class])
class FishCommand : RunnableCommand {

  override fun run() {
    if (versionInfoRequested || usageHelpRequested)
      CommandLine.usage(this, System.out)
  }

  override fun validate(): Boolean = true

  @Option(names = ["-V", "--version"], versionHelp = true,
      description = ["display version info"])
  var versionInfoRequested: Boolean = false

  @Option(names = ["-h", "--help"], usageHelp = true,
      description = ["display this help message"])
  var usageHelpRequested: Boolean = false
}

private const val FISH_HEADER = """
________  __       _______. __    __
|   ____||  |     /       ||  |  |  |
|  |__   |  |    |   (----`|  |__|  |
|   __|  |  |     \   \    |   __   |
|  |     |  | .----)   |   |  |  |  |
|__|     |__| |_______/    |__|  |__|

  """
