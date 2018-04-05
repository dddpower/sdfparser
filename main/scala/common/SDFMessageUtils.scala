package common

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

object SDFMessageUtils {
  def printUsageInfo(args: Array[String]){
    println("Usage: sdfparser <src file> [options]")
    println("Option summary:")
    println("-dumpTokens <filename>")
    println("-dumpAST <filename>")
    println("-help")
  }
  def printInvalidCmd(strOption : String) {
    println("Please check a option[" + strOption + "] that is not allow to us")
  }
  def printDupOptions(strOption : String) {
    println("Please check a option[" + strOption + 
            "] that is apready duplicated")
  }
  def printNotHasbeenCalledCmdParse() {
    println("Before calling me, please call the processCmdLine of function")
  }
  def printNotReadyOption(strOption : String) {
    println("Before calling me, please put a option[" + strOption + "]")
  }
  def printCheckFile {
    println("Please check a src file whether it exists or not")
  }
  def printNoSrcFile {
    println("Please check a src file is not given")
  }
  def printInvalidOptionValue(strOption : String) {
    println("Please check following a option[" + strOption + "] value")
  }
  def pos2str(pos: Position): String = pos match {
    case NoPosition => "<unknown src position>"
    case p => p.line + ":" + p.column
  }
}
