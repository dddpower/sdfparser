package scanner

import java.io._
import scala.io.Source
import scala.util.parsing.combinator.lexical.Scanners

class SDFUtils {

  // Dump all tokens.
  def printoutAllTokens(sdfLexical : SDFLexical, 
                        srcPath : String, 
                        descPath : String){

    import sdfLexical.{SDFToken, ErrorToken}

    val txt = Source.fromFile(srcPath)
    val strInput:String = txt.getLines.reduceLeft[String](_ + '\n' + _)
    val fout = new PrintWriter(new File(descPath));
    var tokens = new sdfLexical.Scanner(strInput);

    fout.write("// Token sequence from SDF scanner")
    fout.write("\n")
    fout.write("// Source file name: " + srcPath)
    fout.write("\n\n")

    var exit = false
    while(!tokens.atEnd && !exit) {
      tokens.first match {
        case errtok: ErrorToken =>
          val errtok = tokens.first.asInstanceOf[ErrorToken]
          fout.write("ErrorToken: " + errtok.chars + "\n")
          exit = true
        case _ =>
          tokens.first.asInstanceOf[SDFToken].printToken(fout)
          tokens = tokens.rest
          if (!tokens.atEnd) {
            fout.write("\n")
          }
      }
    }
    fout.close();
  }
}
