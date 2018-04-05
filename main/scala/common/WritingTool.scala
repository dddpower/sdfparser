package common

object WritingTool {
  object indent {
    var ation = ""
    def inc = {
      ation += "  "
      ation
    }
    def dec = {
      ation = ation dropRight(2)
      ation
    }
  }
  def openBlock = "{\n" + indent.inc
  def newLine = "\n" + indent.ation
  def endStmt = ";" + newLine
  def closeBlock = indent.dec + newLine  + "}"
  val semcol = ";"
  val lparen = "("
  val rparen = ")"
  val lbrace = "{"
  val rbrace = "}"
  val sdf = "sdf "
  val actor = "actor "
  val state = "state: "
  val init = "init: "
  val firing = "firing: "
  val push = "push"
  val pop = "pop()"
  val input = "input: "
  val output = "output: "
  val col = ": "
  val unimplemented = "unimplemented"
  val space = " "
  val comma = ", "
}