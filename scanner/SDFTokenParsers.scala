package scanner

import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.input.NoPosition
import scala.collection.mutable
import scala.language.implicitConversions
import java.lang._

import ast._
import sem._
import common._

trait SDFTokenParsers extends TokenParsers {
  type Tokens = SDFTokens

  val lexical = new SDFLexical

  import lexical.{ SDF_Identifier, SDF_IntLit, SDF_FloatLit, SDF_HexLit, 
                   SDF_StringLit, SDF_Keyword, SDF_Delimiter, SDF_Operator,
                   SDF_BooleanLit, SDF_ComplexLit, SDF_PiLit}

  protected val keywordCache = mutable.HashMap[String, Parser[String]]()

  // Reserved StreamIt keywords:
  lexical.reserved += ("filter", "pipeline", "splitjoin", "feedbackloop")
  lexical.reserved += ("stateful", "split", "duplicate", "roundrobin", "add")
  lexical.reserved += ("body", "loop", "iter", "enqueue")
  lexical.reserved += ("void", "int", "float", "complex", "double", "bit")
  lexical.reserved += ("boolean", "portal")

  lexical.reserved += ("init", "prework", "work", "push", "pop", "peek", "join")
  lexical.reserved += ("for", "if", "else", "else if", "while", "do", "return")
  lexical.reserved += ("handler", "continue", "break", "to")
  lexical.reserved += ("struct", "native", "helper", "static")
    
  // StreamIt delimiters:
  lexical.delimiters += ("(", ")", "[", "]", "{", "}", ";", ",", ":")
  lexical.delimiters += ("->", ".") 

  // StreamIt operators:
  lexical.operators += ("=", "?", "&&", "||", "==", "!=", "!")
  lexical.operators += ("<=", "<", ">=", ">")
  lexical.operators += ("&", "|", "^", "<<", ">>", "~")
  lexical.operators += ("+", "-", "*", "/", "%")
  lexical.operators += ("++", "--", "&=", "|=", "^=")
  lexical.operators += ("*=", "/=", "%=", "+=", "-=", "<<=", ">>=")

  /**
   * A parser which matches a single keyword token.
   *
   * @param chars    The character string making up the matched keyword.
   * @return a `Parser` that matches the given string
   */
  // Required to turn strings into parsers automatically
  
  implicit def keyword(chars: String): Parser[String] = {
    //println("Called Keyword conversion " + chars);
    if (lexical.reserved.contains(chars)) {
      keywordCache.getOrElseUpdate(chars, accept(SDF_Keyword(chars)) ^^
        (_.chars))
    } else if (lexical.delimiters.contains(chars)) {
      keywordCache.getOrElseUpdate(chars, accept(SDF_Delimiter(chars)) ^^
        (_.chars))
    } else if (lexical.operators.contains(chars)) {
        keywordCache.getOrElseUpdate(chars, accept(SDF_Operator(chars)) ^^
        (_.chars))
    } else
      failure("A \""+chars+"\""+" is neither a delimiter nor a reserved"
              + " keyword.")
  }

  def TK_Int: Parser[ast.QualType] = accept(SDF_Keyword("int")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Int, pos=kw.pos)
  }
  def TK_Float: Parser[ast.QualType] = accept(SDF_Keyword("float")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Float, pos=kw.pos)
  }
  def TK_Boolean: Parser[ast.QualType] = accept(SDF_Keyword("boolean")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Boolean, pos=kw.pos)
  }
  def TK_Complex: Parser[ast.QualType] = accept(SDF_Keyword("complex")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Complex, pos=kw.pos)
  }
  def TK_Bit: Parser[ast.QualType] = accept(SDF_Keyword("bit")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Bit, pos=kw.pos)
  }
  def TK_Void: Parser[ast.QualType] = accept(SDF_Keyword("void")) ^^ {
    case kw: SDF_Keyword => QualType(GlobalDeclContext.T_Void, pos=kw.pos)
  }
  def tmpID: Parser[ast.ID] = {
    elem("SDF identifier", _.isInstanceOf[SDF_Identifier]) ^^ {
      x => val id=x.asInstanceOf[SDF_Identifier]
        ast.ID(id.pos, id.chars)
    }
  }
  def ID: Parser[String] = {
    elem("ID", _.isInstanceOf[SDF_Identifier]) ^^ (_.chars) 
  }
  def tmpintLit: Parser[ast.IntLiteral] = {
    elem("int literal", _.isInstanceOf[SDF_IntLit]) ^^ {
      x => val i=x.asInstanceOf[SDF_IntLit]
        var iv : Int = 0
        try {
          iv = Integer.parseInt(i.chars)
        } catch {
          case _: Throwable =>
            ErrorReporter.reportFatalError("str->Int conversion failed for %",
              "SDF_IntLit", i.pos) 
        }
        ast.IntLiteral(i.pos, i.chars, iv)
    }
  }
  def intLit: Parser[String] = {
    elem("int literal", _.isInstanceOf[SDF_IntLit]) ^^ (_.chars)
  }
  def tmppiLit: Parser[ast.PiLiteral] = {
    elem("pi literal", _.isInstanceOf[SDF_PiLit]) ^^ {
      num => val i=num.asInstanceOf[SDF_PiLit]
      ast.PiLiteral(i.pos, i.chars, Math.PI)
    }
  }
  def piLit: Parser[String] = {
    elem("pi literal", _.isInstanceOf[SDF_PiLit]) ^^ (_.chars)
  }
  def tmpcomplexLit: Parser[ast.ComplexLiteral] = {
    elem("Complex literal", _.isInstanceOf[SDF_ComplexLit]) ^^ {
      num => val i=num.asInstanceOf[SDF_ComplexLit]
        println(i.chars)
        ast.ComplexLiteral(i.pos, i.chars, num.chars)
    }
  }
  def complexLit: Parser[String] = {
    elem("complex literal", _.isInstanceOf[SDF_ComplexLit]) ^^ (_.chars)
  }
  def tmpfloatLit: Parser[ast.FloatLiteral] = {
    elem("float literal", _.isInstanceOf[SDF_FloatLit]) ^^ {
      x => val f=x.asInstanceOf[SDF_FloatLit]
        var fv : Float = 0.0f
        try {
          fv = Float.parseFloat(f.chars)
        } catch {
          case _: Throwable =>
            ErrorReporter.reportFatalError("str->Float conversion failed for %",
              "SDF_FloatLit", f.pos) 
        }
        ast.FloatLiteral(f.pos, f.chars, fv)
    }
  }
  def floatLit: Parser[String] = {
    elem("float literal", _.isInstanceOf[SDF_FloatLit]) ^^ (_.chars)
  }
  def tmphexLit: Parser[ast.IntLiteral] = {
    elem("hex literal", _.isInstanceOf[SDF_HexLit]) ^^ {
      x => val hx=x.asInstanceOf[SDF_HexLit]
        var iv: Int = 0 
        try {
          val lit = hx.chars.drop(2) // strip "0x" from the literal
          var lv: Long = java.lang.Long.parseLong(lit, 16)
          // unsigned to signed Int conversion:
          var div: Long = Int.MaxValue
          div = div - Int.MinValue + 1
          lv = ((lv - Int.MinValue) % div) + Int.MinValue
          iv = lv.toInt
        } catch {
          case _: Throwable =>
            ErrorReporter.reportFatalError("hex->int conversion failed for %",
              "SDF_HexLit", hx.pos) 
        }
        ast.IntLiteral(hx.pos, hx.chars, iv)
    }
  }
  def hexLit: Parser[String] = {
    elem("hex literal", _.isInstanceOf[SDF_HexLit]) ^^ (_.chars)
  }
  def tmpstringLit: Parser[ast.StringLiteral] = {
    elem("string literal", _.isInstanceOf[SDF_StringLit]) ^^ {
      s => val sl=s.asInstanceOf[SDF_StringLit]
      ast.StringLiteral(sl.pos, sl.chars)
    }
  }
  def stringLit: Parser[String] = {
    elem("string literal", _.isInstanceOf[SDF_StringLit]) ^^ (_.chars)
  }
  def tmpbooleanLit: Parser[ast.BooleanLiteral] = {
    elem("boolean literal", _.isInstanceOf[SDF_BooleanLit]) ^^ {
      b => val tok=b.asInstanceOf[SDF_BooleanLit]
        val bv = tok.chars match {
          case "true" => true
          case "false" => false
        }
        ast.BooleanLiteral(tok.pos, tok.chars, bv)
    }
  }
  def booleanLit: Parser[String] = {
    elem("boolean literal", _.isInstanceOf[SDF_BooleanLit]) ^^ (_.chars)
  }
}
