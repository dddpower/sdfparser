package scanner 

import scala.util.parsing.combinator.token.Tokens
import scala.util.parsing.input.Positional
import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition
import java.io.PrintWriter


// Standard tokens for sdf programs. Along the lines of
// scala.util.parsing.combinator.token.Tokens.StdTokens

trait SDFTokens extends Tokens {

  abstract class SDFToken extends Token with Positional {
    pos = NoPosition
    def printToken (f : PrintWriter) = {
      val prefix = "token"
      f.write(prefix + ".kind = ")
      f.write(this.getClass.getName.split("\\$").last);
      f.write("\n");
      f.write(prefix + ".lexeme = \"" + chars + "\"");
      f.write("\n");
      f.write(prefix + ".src_pos.line = " + pos.line);
      f.write("\n");
      f.write(prefix + ".src_pos.col = " + pos.column);
      f.write("\n");
      f.write(prefix + ".len = " + chars.length);
      f.write("\n");
    }
  }

  case class SDF_Identifier(chars: String) extends SDFToken {
    override def toString = "identifier "+chars
  }

  case class SDF_Keyword(chars: String) extends SDFToken {
    override def toString = chars;
  }

  case class SDF_Delimiter(chars: String) extends SDFToken {
    override def toString = chars;
  }

  case class SDF_Operator(chars: String) extends SDFToken {
    override def toString = chars;
  }

  case class SDF_Lit(chars: String) extends SDFToken {
    override def toString = chars
  }
  case class SDF_IntLit(chars: String) extends SDFToken {
    override def toString = chars
  }

  case class SDF_ComplexLit(chars: String) extends SDFToken {
    override def toString = chars
  }

  case class SDF_FloatLit(chars: String) extends SDFToken {
    override def toString = chars
  }

  case class SDF_HexLit(chars: String) extends SDFToken {
    override def toString = chars
  }

  case class SDF_StringLit(chars: String) extends SDFToken {
    override def toString = "\""+chars+"\""
  }

  case class SDF_BooleanLit(chars: String) extends SDFToken {
    override def toString = "\""+chars+"\""
  }

  case class SDF_PiLit(chars: String) extends SDFToken {
    override def toString = "\""+chars+"\""
  }
}
