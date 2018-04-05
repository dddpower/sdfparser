package common

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition
import scala.collection.mutable

object ErrorReporter {

  var numErrors: Int = 0
  var numWarnings: Int = 0

  var SrcFile: String = "<unknown sourcefile>"
  
  def hasErrors: Boolean = {
    if(numErrors == 0) false else true
  }
  
  def report(kind: String,
             message: String,
             tokenName: String = "",
             pos: Position = NoPosition) {
    var msg = SrcFile + ":"
    if(pos != NoPosition) {
      msg += pos.line + ":" + pos.column + ": "
    }
    msg += kind
    message.foreach {
      case '%' => 
        msg += tokenName
      case c   => 
        msg += c
    }
    msg += "."
    if(kind.contains("error"))
      numErrors+=1
    else
      numWarnings+=1
    System.err.println(msg)
  }

  def reportError(message: String,
                  tokenName: String = "",
                  pos: Position = NoPosition) {
    report("error: ", message, tokenName, pos)
  }

  def reportFatalError(message: String, tokenName: String, pos: Position) {
    report("error: ", message, tokenName, pos)
    System.exit(1)
  }

  def reportWarning(message: String,
                    tokenName: String = "",
                    pos: Position = NoPosition) {
    report("warning: ", message, tokenName, pos)
  }
}
