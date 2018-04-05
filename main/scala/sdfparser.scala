import java.io._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.input.Positional
import scala.util.parsing.input.NoPosition
import scala.io._
import scala.language.postfixOps
import scala.util.Try

import common._

import scanner.SDFTokenParsers
import scanner.SDFUtils
import ast.MyASTTraversal._
import ast._
import ast.Opcode._
import sem._
import interpreter._
import graph.GraphSpace._


// StreamIt parser according to the Streamit language specification version 2.1
// http://groups.csail.mit.edu/cag/streamit/papers/streamit-lang-spec.pdf
// Java syntax definition to fill unspecified StreamIt parts:
// http://docs.oracle.com/javase/specs/jls/se7/html/jls-18.html
// aligned to the behavior of the strc compiler.

object sdfparser extends SDFTokenParsers {

  //typespecL
  //grammar starts here:
  def program = (struct_decl | stream_decl | builtin_stream_decl | builtin_function_decl |
                 global_decl).+ ^^ { 
    case decls=> val d = DeclList(decls)
    ProgramDecl(d)
  }

  //def program: Parser[ProgramDecl] = stream_decl.+ ^^ ProgramDecl
  def struct_decl = 
    "struct" ~> tmpID ~ ("{" ~> struct_field_decl <~ "}") ^^ {
    case id~fd =>
      val fdl = FieldDeclList(fd)
      StructDecl(id, fdl)//TBD
    }
    
  //builtin stream
  def builtin_stream_decl:Parser[BuiltInStreamDecl] = "builtInStream" ~> (tmpID ~ param_decls) <~";" ^^ {

    case id~parm =>
      val pl = ParmVarDeclList(parm)
      BuiltInStreamDecl(id, pl)//TBD
  }
  
  //builtin function
  def builtin_function_decl:Parser[BuiltInFunctionDecl] = "builtInFunction" ~> 
    data_type ~ tmpID ~ param_decls <~";" ^^ {
    case data_type ~ tmpID ~ parms =>
      val pl = ParmVarDeclList(parms)
      BuiltInFunctionDecl(tmpID, data_type, pl)
  }

//  def native_func_decl: Parser[NativeFuncDecl] = 
//    (data_type ~ tmpID ~ param_decls) <~ ";" ^^ {
//    case dt~id~pd =>
//      NativeFuncDecl(dt, id, pd)//TBD
//  }

  def helper_decl: Parser[Decl] = 
    "helper" ~> tmpID ~ ("{" ~> rep(function_decl) <~ "}") ^^ {
    case a~b =>
      NoDecl()//TBD (Cannot Find any of examples using helper declaration)
  }
  
//  def global_decl = 
//    "static" ~> ("{" ~> rep(global_stmt) ~ opt(init_decl) <~ "}") ^^ {
//    case s~od =>
//      GlobalDecl(s, od getOrElse NoDecl())//TBD
//  }
    def global_decl = {
      println("globaldecl")
      "static" ~> ("{" ~> (variable_decl_list<~";").* ~ init_decl.? <~ "}") ^^ {
      case var_decl_list_list~init =>
        println("global decl")
        var vardecllist : List[VarDecl] = Nil
        var_decl_list_list.map{ 
          f=> vardecllist = vardecllist:::f.vardecls
        }
        val vl = VarDeclList(vardecllist)
        GlobalDecl(vl, init.getOrElse(InitFunctionDecl(CompoundStmt(List(NoStmt())))))
      }
    }

  def global_stmt: Parser[Stmt] 
                  = ( expr_stmt <~ ";"
                      | var_decl_stmt  <~ ";"
                      | if_else_stmt 
                      | while_stmt 
                      | do_while_stmt <~ ";" 
                      | for_stmt 
                      )//TBD
  //The Global Decl is not clear in StreamItParserFG.g because most of grammar 
  //are in comments. I guessed above global_decl & stmt from the comment.

  def stream_decl: Parser[StreamDecl] = filter_decl | struct_stream_decl

  def filter_decl: Parser[FilterDecl] = typespec ~ opt("stateful") ~
    "filter" ~ tmpID ~ param_decls ~ filter_body ^^ {
    case t~st~kw~id~parms~b =>
      val pl = ParmVarDeclList(parms)
      val stateful = st match {case None => false; case _ => true}
      FilterDecl(t, stateful, id, pl, b)
  }

  def struct_stream_decl: Parser[StreamDecl] = typespec ~ opt("stateful") ~
    ("pipeline" | "splitjoin" | "feedbackloop") ~
    tmpID ~ param_decls ~ compound_stmt ^^ {
    case t~st~kw~id~parms~cmps =>
      val pl = ParmVarDeclList(parms)
      val stateful = st match {case None => false; case _ => true}
      kw match {
        case "pipeline" => 
          PipelineDecl(t, stateful, id, pl, cmps)
        case "splitjoin" => SplitJoinDecl(t, stateful, id, pl, cmps)
        case "feedbackloop" => FeedbackLoopDecl(t, stateful, id, pl, cmps)
      }
  }

  def param_decls: Parser[List[ParmVarDecl]]
    = opt("(" ~> ( param_decl_list |
                 ( opt("void")^^{case _ =>List[ParmVarDecl]()})) <~ ")") ^^ {
    case None => List[ParmVarDecl]()
    case Some(d) => d
  }

  def param_decl_list: Parser[List[ParmVarDecl]]
    = ( ( param_decl ~ rep("," ~> param_decl) ) ^^ {
    case d1~rst => d1 :: rst 
  } )
  
  def param_decl: Parser[ParmVarDecl] = ( data_type ~ tmpID ^^ {
    case t~v => ParmVarDecl(v,t)
  } ) 

  def filter_body : Parser[FilterBodyDecl] = "{" ~> rep( init_decl
                                | work_decl
                                | function_decl
                                | handler_decl
                                | struct_decl
                                | variable_decl_list <~ ";" ) <~ "}" ^^ {
    case dl=>
      val decllist = DeclList(dl)
      FilterBodyDecl(decllist)}

  def struct_field_decl : Parser[List[FieldDecl]] = (struct_variable_decl <~ ";").* ^^ {
    case result=> result.flatten
  }
  def struct_variable_decl : Parser[List[FieldDecl]] = data_type.into {
      resulttype=> repsep(single_field_decl(resulttype),",")
    }
  def single_field_decl(datatype:Type) : Parser[FieldDecl] = tmpID ^^ {
    case id => {
      //println("single_field_decl")
      FieldDecl(id, datatype)
    }
  }
//  def struct_variable_decl: Parser[List[FieldDecl]]
//    = data_type ~ tmpID  ~
//      rep( "," ~> tmpID ) ^^ {
//    case t~id1~mvars =>
//      val var1 = FieldDecl(id1, t)
//      val morevars = mvars.map {
//        case id => FieldDecl(id, t)
//      }
//      var1 :: morevars
//  }

  def field_decl: Parser[List[FieldDecl]]
    = data_type ~ tmpID ~
      rep( "," ~> tmpID) ^^ {
    case t~id1~mvars =>
      val var1 = FieldDecl(id1, t)
      val morevars = mvars.map {
        case id=>FieldDecl(id, t)
      }
      var1 :: morevars
  }
  
  def var_decl_stmt = variable_decl_list ^^ { 
//    case vd ~ None => DeclStmt(List(vd))
//    case vd ~ ids => {
//      val vardecllist = ids.get.map(id=> VarDecl(id, vd., NoDecl))
//      DeclStmt(vd::vardecllist)
//    }
    case vardecllist=>
      val dl = DeclList(vardecllist.vardecls)
      DeclStmt(dl)
  }
//  def variable_decl = data_type ~ ID ~ ( "=" ~ var_initializer) .? ~ 
//                       ( "," ~ ID ~ ( "=" ~ var_initializer) .? ).*;
//  def variable_decl : Parser[VarDecl] = data_type ~ tmpID ~ opt(var_initializer) ^^ {
//    case t~id ~ ini => VarDecl(id, t)
//  }
//  def variable_decl: Parser[List[VarDecl]]
//    = data_type ~ tmpID ~ rep( "," ~> tmpID ) ^^ {
//    case t~id1~mvars =>
//      val var1 = VarDecl(id1, t)
//      val morevars = mvars.map {
//        case id=>VarDecl(id, t)
//      }
//      var1 :: morevars
//  }
//  def variable_decl
//    = data_type ~ tmpID ~ opt(var_initializer) ~
//      rep( "," ~> tmpID ~ opt(var_initializer) ) ^^ {
//    case t~id~init~List()=>
//      VarDecl(id, t, init.getOrElse(NoExpr()))
//    case t~id1~init1~mvars =>
//      val var1 = VarDecl(id1, t, init1 getOrElse NoExpr())
//      val morevars = mvars.map {
//        case id~iexpr=>VarDecl(id, t, iexpr getOrElse NoExpr())
//      }
//      VarDeclList(var1 :: morevars)
//  }
//  def variable_decl = data_type ~ tmpID ~ var_initializer.? ~ ("," ~> tmpID ~ var_initializer.?).* |
//  data_type ~ tmpID ~ var_initializer.?

    def variable_decl_list : Parser[VarDeclList] = data_type.into {
      resulttype=> repsep(single_variable_decl(resulttype),",")^^{
        case vardecllist=>VarDeclList(vardecllist)
      }
    }
  
  //def variable_decl = data_type.into(fq) ~ repsep(single_variable_decl,",")
  
  def single_variable_decl(datatype:Type) : Parser[VarDecl] = tmpID ~ var_initializer.? ^^ {
    case id ~ init => VarDecl(id, datatype, init.getOrElse(NoExpr()))
  }
//  def comma_variable_decl(datatype: Type) = "," ~> tmpID ~ var_initializer.? ^^ {
//    case id~init => VarDecl(id, datatype, init.getOrElse(NoExpr()))
//  }
//  def additional_variable_decl(datatype: Type) = comma_variable_decl(datatype).*
  
  def var_initializer: Parser[Expr] = "=" ~> var_expr
  
  def var_expr: Parser[Expr] = ( ternary_expr | arr_initializer )
  
  def arr_initializer: Parser[Expr] = "{" ~> repsep(var_expr,",") <~ "}"  ^^ {
//    case None => NoExpr()
//    case Some(f~rs) =>
//      val arr = List(f) ::: rs
//      InitArrayExpr(arr)
    case list=>
      val exprl = ExprList(list)
      InitArrayExpr(exprl)
  }
  
  def init_decl: Parser[InitFunctionDecl] = "init" ~> compound_stmt ^^ {
    case c => InitFunctionDecl(c) 
  }

  def work_decl: Parser[Decl] = ("work"|"prework")~data_rates~compound_stmt ^^ {
    case tok~rates~body=>
      tok match {
        case "work"    => WorkFunctionDecl(rates, body)
        case "prework" => PreWorkFunctionDecl(rates, body)
      }
  }

  def data_rates: Parser[DataRates]
    = (push_rate | pop_rate | peek_rate).* ^^ {
    case list => 
      val decll = DeclList(list)
      DataRates(decll)
  }

  //TBD: StreamIt push/pop/peek are more complex than this:
  def push_rate = "push" ~> rate_expr ^^ {case e => PushDecl(e)}
  def pop_rate  = "pop"  ~> rate_expr ^^ {case e => PopDecl(e)}
  def peek_rate = "peek" ~> rate_expr ^^ {case e => PeekDecl(e)}
  
  def rate_expr = (ternary_expr | range_expr | star_expr)
  
  def range_expr: Parser[Expr] = "[" ~> (dynamic_expr <~ ",") ~ dynamic_expr ~
                                  opt( "," ~> dynamic_expr ) <~ "]" ^^ {
    case min~ave~max =>
      RangeExpr(min, ave, max getOrElse NoExpr())
  }

  def dynamic_expr: Parser[Expr] = star_expr | rhs_expr

  def star_expr: Parser[Expr]  = "*" ^^ {
    case e =>
      StarExpr()
  }

  def function_decl: Parser[Decl]
    = data_type ~ tmpID ~ opt(param_decls) ~ data_rates ~ compound_stmt ^^ {
    case t~id~d~r~c =>
      val pl = if(d.isDefined) ParmVarDeclList(d.get)
      else ParmVarDeclList(Nil)
      HelperFunctionDecl(t, id, pl, r, c)
  }

  def handler_decl: Parser[Decl]
    = "handler" ~> tmpID ~ opt(param_decls) ~ compound_stmt ^^ {
    case id~d~c =>
      val pl = if(d.isDefined) ParmVarDeclList(d.get)
      else ParmVarDeclList(Nil)
      HandlerFunctionDecl(id, pl, c)
  }

  def stmt: Parser[Stmt]
    = ( compound_stmt
        | add_stmt
        | body_stmt
        | loop_stmt
        | split_stmt <~ ";"
        | join_stmt <~ ";"
        | push_stmt <~ ";"
        | for_stmt
        | if_else_stmt
        | expr_stmt <~ ";"
        | return_stmt <~ ";"
        | while_stmt
        | do_while_stmt <~ ";"
        | var_decl_stmt<~ ";"
        | enqueue_stmt <~ ";"
        | break_stmt
        | continue_stmt 
        | comma_stmt 
        | msg_statement <~ ";" )
  
  //TBD: please reorder the below productions 
  //  in the order they appear in ``stmt'':

  def compound_stmt = "{" ~> rep(stmt) <~ "}" ^^ { case l => CompoundStmt(l) }

  def add_stmt = "add" ~> stream_constructor

  def body_stmt = "body" ~> stream_constructor

  def loop_stmt = "loop" ~> stream_constructor

  def stream_constructor: Parser[ConstructExpr]
    = ( anonymous_stream_constructor | named_stream_constructor)

  def anonymous_stream_constructor
    = anonymous_stream_filter_constructor | anonymous_stream_struct_constructor
  
  def anonymous_stream_filter_constructor
  = opt(typespec) ~ opt("stateful") ~ "filter" ~ 
    filter_body ~ opt(portal_spec <~ ";") ^^ {
    case t~st~f~fb~ops =>
      val stateful = st match {case None => false; case _ => true}
      ConstructAnonFilterExpr(new ID("AnonFilter_a0_"+Counter.getNumber),
          t.get,stateful,fb,ops getOrElse NoExpr())
  }
  //def tmp_id: AST
  def anonymous_stream_struct_constructor
  = opt(typespec) ~ opt("stateful") ~ 
    ("pipeline" | "splitjoin" | "feedbackloop") ~ compound_stmt ~ 
    opt(portal_spec <~";") ^^ {
    case t~st~kw~cmps~op =>
      val stateful = st match {case None => false; case _ => true}
      kw match {
        
        case "pipeline" => 
          AnonymousPipelineExpr(t getOrElse null,
                                   stateful,
                                   cmps, 
                                   op getOrElse NoExpr())
          //TBD For Setting NoIOTypeSpec
        case "splitjoin" => 
          AnonymousSplitJoinExpr(t getOrElse null,
                                    stateful,
                                    cmps, 
                                    op getOrElse NoExpr())
        case "feedbackloop" => 
          AnonymousFeedbackLoopExpr(t getOrElse null,
                                       stateful,
                                       cmps, 
                                       op getOrElse NoExpr())
      }
  }

  def named_stream_constructor: Parser[ConstructExpr]
    = tmpID ~ opt(("<" ~> data_type) <~ ">") ~
              opt(func_call_parms) ~
              opt(portal_spec) <~ ";" ^^ {
    case id~t~p~ps => 
      StreamConstructExpr(id,
                                          t getOrElse NoType(),
                                          ExprList(p getOrElse Nil),
                                          ps getOrElse NoExpr())
  }

  def portal_spec = "to" ~> tmpID ~ rep("," ~> tmpID) ^^ {
    case id~rid =>
      val exprl = ExprList(List(id):::rid)
      PortalSpecExpr(exprl)
  }

  def split_stmt = "split" ~> (rr_spec | duplicate_spec) ^^ SplitStmt

  def join_stmt = "join" ~> (rr_spec | duplicate_spec) ^^ JoinStmt

  def rr_spec = "roundrobin" ~> opt(func_call_parms) ^^ {
    case parms => RRSpec(ExprList(parms.getOrElse(Nil)))
  }

  def duplicate_spec = "duplicate" ~> opt( "(" ~> ")" ) ^^ {
    case _ => DuplicateSpec()
  }

  def push_stmt = "push" ~> "(" ~> rhs_expr <~ ")" ^^ PushStmt
  
  def for_stmt = "for" ~> for_spec 
  
  def for_spec = ("(" ~>  opt(for_init) <~ ";") ~ 
                       (opt(rhs_expr) <~ ";") ~ 
                       (opt(for_incr)  <~ ")") ~ 
                  stmt ^^ {
    case init~cond~incr~st =>
      ForStmt(init getOrElse NoStmt(), 
          cond getOrElse NoExpr(), 
          incr getOrElse NoExpr(),
          st)
  }
  
  def for_init = for_var_decl | for_var_stmt
  
  def for_var_decl = var_decl_stmt
  
  def for_var_stmt = expr_stmt
  
  def for_incr = expr_stmt
  
  def if_else_stmt = "if" ~> ( "(" ~> rhs_expr <~ ")" ) ~ stmt ~ 
                     opt(("else" | "else if") ~> stmt) ^^ {
    case cond~ifs~eif =>
      IfStmt(cond, ifs, eif getOrElse NoStmt())
  }

  def expr_stmt = ( incdec_expr
                    | assign_expr
                    | func_call_expr
                    | streamit_value_expr )

  def return_stmt = "return" ~> opt(rhs_expr) ^^ {
    case e => ReturnStmt(e getOrElse NoExpr())
  }

  def while_stmt = "while" ~> ( "(" ~> rhs_expr <~ ")" ) ~ stmt ^^ {
    case e~s =>
      WhileStmt(e,s)
  }

  def do_while_stmt = ("do" ~> stmt <~ "while") ~ ("(" ~> rhs_expr <~ ")") ^^ {
    case s~e =>
      DoWhileStmt(s, e)
  }

  def enqueue_stmt = "enqueue" ~> rhs_expr ^^ EnqueueStmt 

  def break_stmt = "break" <~ ";" ^^ {
    case b =>
      BreakStmt()
  }

  def continue_stmt = "continue" <~ ";" ^^ {
    case c =>
      ContinueStmt()
  }

  def comma_stmt = ";" ^^ { case s => CommaStmt() }

  def msg_statement: Parser[Stmt] 
    = tmpID ~ ( "." ~> tmpID ) ~ func_call_parms ~ 
      opt("[" ~> ( opt(rhs_expr) ~ 
      (":" ~> opt(rhs_expr|star_expr)) )  <~ "]") ^^ {
    case baseid~id~parms~o =>
      o match {
        case None =>
          MsgStmt(baseid, id, ExprList(parms), NoExpr(), NoExpr())
        case Some(min~max) =>
          MsgStmt(baseid, id, ExprList(parms), 
                   min getOrElse NoExpr(), 
                   max getOrElse NoExpr())
      }
  }

  def rhs_expr = ( ternary_expr )

  def ternary_expr : Parser[Expr]
    = ( logic_or_expr ~ opt("?" ~> (ternary_expr <~ ":") ~ ternary_expr ) ) ^^ {
    case l~o =>
      o match {
        case None => l 
        case Some(~(t, f)) => TernaryExpr(TO_Cond, l, t, f) 
      }
  }
  def logic_or_expr : Parser[Expr]
    = ( logic_and_expr ~ rep("||" ~ logic_and_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def logic_and_expr : Parser[Expr]
    = ( bitwise_or_expr ~ rep("&&" ~ bitwise_or_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def bitwise_or_expr : Parser[Expr]
    = ( bitwise_xor_expr ~ rep("|" ~ bitwise_xor_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }
      
  def bitwise_xor_expr : Parser[Expr]
    = ( bitwise_and_expr ~ rep("^" ~ bitwise_and_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def bitwise_and_expr : Parser[Expr]
    = ( eq_expr ~ rep("&" ~ eq_expr) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def eq_expr : Parser[Expr]
    = ( rel_expr ~ rep(("=="|"!=") ~ rel_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def rel_expr : Parser[Expr]
    = ( shift_expr ~ rep(("<"|"<="|">"|">=") ~ shift_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def shift_expr : Parser[Expr]
    = ( add_expr ~ rep(("<<"|">>") ~ add_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def add_expr : Parser[Expr]
    = ( mult_expr ~ rep(("+"|"-") ~ mult_expr ) ) ^^ {
    case l~rest => leftAssociativeInfix(l, rest)
  }

  def mult_expr : Parser[Expr]
    = ( cast_expr ~ rep(("*"|"/"|"%") ~ cast_expr) ) ^^ {
    case l~rest => leftAssociativeInfix(l,rest)
  }

  def cast_expr: Parser[Expr]
    = ("("~>primitive_type<~")").* ~ prefix_op_expr ^^ {
    
    case casts~e =>
      val list=casts.asInstanceOf[List[ast.Type]]
      if (list.isEmpty) e
      else
        CastExpr(TypeList(list), e) 
  }

  def prefix_op_expr: Parser[Expr]
    = opt("+"|"-"|"!"|"~") ~ (incdec_expr | rvalue) ^^ {
    case op~e =>
      val expr=e.asInstanceOf[Expr]
      op match {
        case None => expr
        case Some(s) => s match {
          case "+" => UnaryExpr(UO_Plus, expr)
          case "-" => UnaryExpr(UO_Minus, expr)
          case "!" => UnaryExpr(UO_LogicNot, expr)
          case "~" => UnaryExpr(UO_BitNot, expr)
        }
      }
  }

  def incdec_expr: Parser[Expr]
    = ( lvalue<~"++" ^^ {case lv: Expr => UnaryExpr(UO_PostInc, lv) }
        | lvalue<~"--" ^^ {case lv: Expr => UnaryExpr(UO_PostDec, lv) }
        | "++"~>lvalue ^^ {case lv: Expr => UnaryExpr(UO_PreInc, lv) }
        | "--"~>lvalue ^^ {case lv: Expr => UnaryExpr(UO_PreDec, lv) } )

  def lvalue: Parser[Expr] 
    = tmpID ~ rep("."~tmpID | "["~ternary_expr<~"]") ^^ {
      case v~q=> val tmp = q.asInstanceOf[List[String~Expr]]
        tmp.foldLeft(v:Expr) {
          case (lhs, "."~f) => FieldExpr(lhs, f) 
          case (lhs, "["~e) => ArraySubscriptExpr(lhs, e)
        }
  }

  def rvalue: Parser[Expr]
    = ( literal_expr
        | paren_expr
        | func_call_expr
        | lvalue
        | streamit_value_expr )

  def paren_expr: Parser[Expr] = ( "(" ~> rhs_expr <~ ")" ) ^^ {
    case expr => ParenExpr(expr.asInstanceOf[Expr])
  }

  def literal_expr: Parser[Expr]
  = tmpintLit|tmpfloatLit|tmphexLit|tmpstringLit|
    tmpbooleanLit|tmpcomplexLit|tmppiLit

//  def func_call_expr = tmpID ~ rep("."~>tmpID) ~ func_call_parms ^^ {
//    case id~rid~parms =>
//      CallExpr(id::rid, parms)
//  }
  def func_call_expr = tmpID ~ func_call_parms ^^ {
    case id~parms =>
      CallExpr(id, ExprList(parms))
  }

  def func_call_parms: Parser[List[Expr]]
    =  "(" ~> opt(ternary_expr ~ rep("," ~> ternary_expr)) <~ ")" ^^ {
    case Some(p1~parms) => p1::parms
    case None => List[Expr]()
  }

  def assign_expr: Parser[Expr]
    = lvalue ~ assign_op ~ rhs_expr ^^ {
    case l~op~r => BinaryExpr(Opcode.withName(op), l, r)
  }

  def assign_op = ( "=" | "+=" | "-=" | "*=" | "/=" | "%=" | "&=" | "|="
                    | "^=" | "<<=" | ">>=" ) 
  //TBD: Exprs not provided by StreamIt: %=, &=, |=, ^= 

  def streamit_value_expr: Parser[Expr]
    = ( "pop" ~> "(" ~> ")" ^^ {case _ => PopExpr()}
        | "peek" ~> "(" ~> rhs_expr <~ ")" ^^ {case e => PeekExpr(e)}
        | "iter" ~> "(" ~> ")" ^^ {case s => NoExpr()/*TBD*/} ) 

  def typespec: Parser[IOTypeSpec] = ( data_type ~ "->" ~ data_type ^^ {
      case l~"->"~r => IOTypeSpec(l, r)
  } )

  def data_type: Parser[Type]
    = ( compound_type | primitive_type | void_type | portal_type )

  def primitive_type: Parser[Type]
    = TK_Int | TK_Float | TK_Boolean | TK_Complex | TK_Bit

  def compound_type: Parser[Type] 
    = ( (primitive_type | type_name) ~ rep("["~>ternary_expr<~"]") ^^ {
    case tname~subscr => subscr.foldRight(tname: Type) {
          case (idx, ar) => {
            println("parse constantArrayType : idx : " + idx + " ar: " + ar)
            ConstantArrayType(ar, idx)
          }
      }
  } )
  
  def portal_type: Parser[QualType] = "portal" ~> "<" ~> tmpID <~ ">"^^ {
    case id =>
      QualType(PortalType(id))
  }

  def void_type: Parser[Type] = TK_Void

  def type_name: Parser[Type]
  = tmpID ^^ { case i=>UnresolvedType(i) }

  // Parsing helper functions:
  def pruneTilde(xs: List[String~Expr]) = (xs.map{case ~(a, b) => (a,b)})

  def leftAssociativeInfix(first: Expr, rest: List[String~Expr]): Expr = {
    pruneTilde(rest).foldLeft(first) {
      case (l, r) => BinaryExpr(Opcode.withName(r._1), l, r._2)
    }
  }
  // Built - in function ( funcstions of math , print, println )
  // Those are built-in math functions & print
  def builtinFunctions: String = {
    var str: String = "builtInFunction void abs();"
    str += "builtInFunction void arg();"
    str += "builtInFunction void exp();"
    str += "builtInFunction void log();"
    str += "builtInFunction void sin();"
    str += "builtInFunction void cos();"
    str += "builtInFunction void sqrt();"
    str += "builtInFunction void csqrt();"
    str += "builtInFunction void floor();"
    str += "builtInFunction void ceil();"
    str += "builtInFunction void print();"
    str += "builtInFunction void println();"
    str
  }

  def builtinStreams: String = {
    var str: String = "builtInStream Identity();"
    str += "builtInStream FileReader();"
    str += "builtInStream FileWriter();"
    str
  }
  
  //def GenAnonymousID = ast.AnonymousID(Counter.getNum)
  def main(args: Array[String]) {
    
    var AST : AST = null
    // Root node of the AST representing the source program. 

    SDFCmdLineParsers.processCmdLine(args)
    ErrorReporter.SrcFile = SDFCmdLineParsers.getSrcFileName
  
    if (SDFCmdLineParsers.isDumpTokens) {
      SDFCmdLineParsers.getTokenFile(lexical)
      //System.exit(0)
    }
  
    val txt = Source.fromFile(SDFCmdLineParsers.getSrcOfPath)
    val input = txt.getLines.reduceLeft[String](_ + '\n' + _)
    //input += builtinFunctions + builtinStreams
    val tokens = new lexical.Scanner(input)
    println("Syntax Analysis ..."); //pass 1
    val result = phrase(program)(tokens)
    
    result match {
      case Success(tree, _) =>
         AST = tree
         //print AST
         println(tree)
      case e: NoSuccess => {
        
        //Console.err.println(e)
        System.exit(1)
      }
    }
    if (SDFCmdLineParsers.isDumpAst) {
      SDFCmdLineParsers.getASTFile(AST)
    }
    
//    println("find filter")
//    //println(findASTNode[FilterDecl](AST))
//    findASTNode[FilterDecl](AST) match {
//      case k:List[Any]=>println(LaminarWriter.genActorString(k(0).asInstanceOf[FilterDecl]))
//      case _=> println("else case")
//    }
    
//    println("sementic analysis")
//    MySemanticAnalysis.makeScopeTree(AST)
//    SymbolTable.printOut
//    MySemanticAnalysis.decorateAST(AST)
//    MySemanticAnalysis.typePromotion(AST)
    if (SDFCmdLineParsers.isDumpGrammarErrors) {
      //After Checking Context Free Grammar and Dumping Errors
      // , The main program is finished.")
      System.exit(0)//Normal Termination
    }
//    println("Semantic Analysis ...") //pass 2
//    SemanticAnalysis.check(AST)
//
//    if (SDFCmdLineParsers.isDumpAst) {
//      SDFCmdLineParsers.getASTFile(AST)
//    }
    println("Running Evaluation  ...") //pass 3
    val scalacode = ScalaWriter.importstr + ScalaWriter.genScala(AST) + "\n" +
      ScalaWriter.mainPipeName /*+ ScalaWriter.genToDotCommand("result.gv")*/
    println("writing result \n" + scalacode)
//    val file = new File("target.scala")
//    val bw = new BufferedWriter(new java.io.FileWriter(file))
//    bw write(scalacode)
//    bw close()
//    println("file generation finished")
//    Eval.fromFileName("target.scala")
    val ggraph = Eval[PipeLine](scalacode)
    ggraph.toDotFile("result.dot")
    LaminarWriter.graph = ggraph
    LaminarWriter.prog = AST.asInstanceOf[ProgramDecl]
    LaminarWriter.mainPipeName = ScalaWriter.mainPipeName
    println("Evaluation finished")
    LaminarWriter.triggerMapping
//    println("genGraph Result")
//    println(LaminarWriter.genGraph)
    //println("genSDF Result")
    val output = LaminarWriter.genSDF
    def filewrite(target: String, filename: String, extension: String) = {
      val file = new File(filename + "." + extension)
      val bw = new BufferedWriter(new java.io.FileWriter(file))
      bw.write(target)
      bw.close()  
    }
    filewrite(output,LaminarWriter.mainPipeName,"sdf")
    
//    
//    if (SDFCmdLineParsers.isDumpAst) {
//      SDFCmdLineParsers.getASTFile(AST)
//    }
//
//
//    if (SDFCmdLineParsers.isDumpSDFGraph) {
//      println("Printing Dot file for Stream Graph  ...") //pass 4
//      SDFCmdLineParsers.getSDFGraph
//    }
//    
//    println("Done  ...")
//    
    //ScopeTreeNode.printOutScopeTreea
    println("End of Main")
  }
}
