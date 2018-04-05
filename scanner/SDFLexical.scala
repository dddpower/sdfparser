package scanner 

import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.token._
import scala.util.parsing.input.Positional
import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition
import scala.util.parsing.input.CharArrayReader.EofCh
import scala.util.matching.Regex
import scala.collection.mutable
import scala.language.postfixOps

class SDFLexical extends StdLexical with SDFTokens {

  // The set of operators (ordering does not matter).
  val operators = new mutable.HashSet[String]

  def regex(r: Regex): Parser[String] = new Parser[String] {
    def apply(in: Input) = r.findPrefixMatchOf(
      in.source.subSequence(in.offset, in.source.length)) match {
      case Some(matched) =>
        Success(in.source.subSequence(in.offset,
        in.offset + matched.end).toString, in.drop(matched.end))
      case None =>
        Failure("string matching regex '" + r +
        "' expected but " + in.first + " found", in)
    }
  } // (from ``Scala for the impatient, p. 285'')

  // see `token` in `Scanners`
  override def token: Parser[Token] =
  (
    positioned(regex("0x[0-9|a-f|A-F]+".r) ^^ {SDF_HexLit(_)}) |
    positioned(regex("""(\d+(\.\d*)|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?+[i]""".r) ^^ {SDF_ComplexLit(_)}) |
    positioned(regex("""(\d+(\.\d*)|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?""".r) ^^
    {SDF_FloatLit(_)}) |
    positioned(regex("""\d+([eE][+-]?\d+)[fFdD]?""".r) ^^ {SDF_FloatLit(_)}) |
    positioned(regex("""\d+""".r) ^^ {SDF_IntLit(_)}) |
    positioned(regex("""true|false""".r) ^^ {SDF_BooleanLit(_)}) |
    positioned(regex("""\bpi\b""".r) ^^ {SDF_PiLit(_)}) |
    //regex("""[a-z|A-Z][a-z|A-Z|0-9]*""".r) ^^ {SDF_Identifier(_)} |
    /*
    identChar ~ rep( identChar | digit )              ^^
    { case first ~ rest => processIdent(first :: rest mkString "") } |
    */
    positioned (identChar ~ rep( identChar | digit )              ^^
    { case first ~ rest => processSDFIdent(first :: rest mkString "") }) |
    positioned('\'' ~ rep( chrExcept('\'', '\n', EofCh) ) ~ '\'' ^^
    { case '\'' ~ chars ~ '\'' => SDF_StringLit(chars mkString "") }) |
    positioned('\"' ~ rep( chrExcept('\"', '\n', EofCh) ) ~ '\"' ^^
    { case '\"' ~ chars ~ '\"' => SDF_StringLit(chars mkString "") }) |
    EofCh                                             ^^^ EOF  |
    '\'' ~> failure("unterminated string literal") |
    '\"' ~> failure("unterminated string literal") |
    positioned(delim) |
    positioned(operator) |
    failure("illegal character")
  )

  //Along the lines of processIdent from StdLexical:
  def processSDFIdent(name: String): SDFToken = {
    if (reserved contains name) {
      SDF_Keyword(name)
    } else {
      SDF_Identifier(name)
    }
  }

  private lazy val _delim: Parser[SDFToken] = {
    // construct parser for delimiters by |'ing together the parsers
    // for the individual delimiters,
    // starting with the longest one -- otherwise a delimiter D will never
    // be matched if there is
    // another delimiter that is a prefix of D
    def parseDelim(s: String): Parser[SDFToken] = accept(s.toList) ^^
    { x => SDF_Delimiter(s) }

    val d = new Array[String](delimiters.size)
    delimiters.copyToArray(d, 0)
    scala.util.Sorting.quickSort(d)
    (d.toList map parseDelim).foldRight(failure("no matching delimiter"):
    Parser[SDFToken])((x, y) => y | x)
  }
  override def delim: Parser[SDFToken] = _delim

  private lazy val _operator: Parser[SDFToken] = {
    // construct parser for operators, same as for delimiters:
    def parseOp(s: String): Parser[SDFToken] = accept(s.toList) ^^
    { x => SDF_Operator(s) }

    val o = new Array[String](operators.size)
    operators.copyToArray(o, 0)
    scala.util.Sorting.quickSort(o)
    (o.toList map parseOp).foldRight(failure("no matching operator"):
    Parser[SDFToken])((x, y) => y | x)
  }
  def operator: Parser[SDFToken] = _operator
}
