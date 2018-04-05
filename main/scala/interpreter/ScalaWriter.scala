package interpreter
import ast._
import common.Counter 
import LaminarWriter._
import common.WritingTool._

object ScalaWriter {
  Counter.reSet
  var mainPipeName:String = _
  indent.ation = ""
  var filterpointer : FilterDecl = _
  val importstr = "import graph.GraphSpace._\nimport graph._\n"
  
  def genToDotCommand(outputfilename: String) : String = {
    newLine + mainPipeName + ".toDotFile(\"" + 
    outputfilename + "\")" + newLine
  }
  def genScala(node: WhileStmt) : String =
    "while(" + genScala(node.Condition) + ")" + genScala(node.Body)
  
  def genScala(node : ForStmt) : String = {
    println("forstmt enter check")
    genScala(node.Init) + newLine +
    "while(" + genScala(node.Condition) + ")" + openBlock +
    genScala(node.Body) + newLine + genScala(node.Incr) + closeBlock
  }
  
  def genScala(node:VarDecl) : String = {
    val vardecl = "var " + node.id.toString + ":" + genScala(node.T) + " = "
    val init : String = if(node.init==NoExpr()) {
      node.T match {
        case arr: ConstantArrayType =>
           {
            val dim = new scala.collection.mutable.ListBuffer[String]()
            val addstr = "Array ofDim[" + genScala(arr.ElementType) + "]("
            dim += genScala(arr.size)
            var t = arr.ElementType
            while(t.isInstanceOf[ConstantArrayType]) {
              dim += genScala(arr.size)
              t= t.asInstanceOf[ConstantArrayType].ElementType
            } 
            addstr + dim.mkString(",") + ")"
          }
          
        //not array    
        case int: IntType => "0"
        case float: FloatType => "0"
        case bool: BooleanType => "false"
        case un : UnresolvedType => "new " + un.Name.toString
      }
    }
    else genScala(node.init)
    
    vardecl + init
  }
  def genScala(node: Type) : String = node match {
    case arr : ConstantArrayType => "Array[" + genScala(arr.ElementType) + "]"
    case float : FloatType => "Double"
    case un : UnresolvedType => un.toString()
    case _ => 
      val str = node.toString()
      str.head.toUpper + str.tail
  }
  
  def genScala(node: IfStmt) : String = {
    val ifstmt = "if(" + genScala(node.Condition) + ")" + genScala(node.ifBody)
    val elsestmt : String = 
      if(node.elseBody!=NoStmt())
        "else" + genScala(node.elseBody)
      else ""
    ifstmt + elsestmt
  }
  
  def genScala(node: ReturnStmt) : String = genScala(node.Value)
  
  def genScala(node : DoWhileStmt) : String =
    "do" + genScala(node.Body) + "while(" + genScala(node.Condition) + ")" + newLine
  
  def genScala(body: CompoundStmt) : String = openBlock + 
    body.stmts.map(stmt=>genScala(stmt)).mkString(newLine) + closeBlock
  
    
  def isMain(node: IOTypeSpec) : Boolean = node.InT.toString=="void" &&
    node.OutT.toString()=="void"
  
  def genScala(node: ConstructAnonFilterExpr) : String = {
    "g.add(" + openBlock +
    "val name = " + "\"" + node.id.toString + "\"" + newLine +
    "val intT = " + "\"" + node.iotype.InT.toString + "\"" + newLine +
    "val outT = " + "\"" + node.iotype.OutT.toString + "\"" + newLine +
    "var push : Option[Int] = None" + newLine +
    "var pop : Option[Int] = None" + newLine +
    "var peek : Option[Int] = None" + newLine +
    genScala(node.body) +
    "FilterNode(None, name, intT, outT, push, pop, peek)" +
    closeBlock + ")"
  }
  def genScala(node: AnonymousPipelineExpr) : String = "g.add(" + openBlock +
    "val g = new PipeLine(" + genScala(node.iotype) + ")" + newLine + newLine + genScala(node.body) + newLine + "g" + closeBlock + ")"
  
  def genScala(node: AnonymousSplitJoinExpr) : String = "g.add(" + openBlock +
    "val g = new SplitJoin()" + newLine + 
    newLine + genScala(node.body) + newLine + "g" + closeBlock + ")"
  
  //same as pipelinedecl
  def genScala(node: SplitJoinDecl) : String = {
    "def " + node.id.toString + genScala(node.parmvardecls) + " : SplitJoin" + " = " + newLine +
    openBlock + "val g = new SplitJoin(\"" + node.id.toString + "\")" + 
    newLine + newLine + genScala(node.body) + newLine + "g" + closeBlock
  }
  def genScala(node: ProgramDecl) : String = {
    genScala(node.decls)
  }

  def genScala(node: DeclList) : String = {
    println("DeclList")
    node.decls.map(decl=>genScala(decl)).mkString(newLine)
  }
  
  def genScala(node : ExprList) : String = "(" + node.exprs.map(expr=>genScala(expr)).mkString(", ") + ")"
  
  def genScala(node: VarDeclList) : String = {
    println("VarDeclList")
    node.vardecls.map(vardecl=>genScala(vardecl)).mkString(newLine) + newLine
  }
  
  def genScala(node:ParmVarDecl) : String =
    node.id.toString() + ":" + genScala(node.T)
  
  def genScala(node: ParmVarDeclList) : String = "(" + 
    node.parmvardecls.map(decl=>genScala(decl)).mkString(", ") + ")"
  
  
  def genScala(node: GlobalDecl) : String = {
    genScala(node.decls) + newLine + genScala(node.initDecl)
  }
  
  def genScala(node: SplitStmt) : String = ""
  
  def genScala(node: JoinStmt) : String = ""
  
  def genScala(iot:IOTypeSpec) : String = 
    "(" + "\"" +iot.InT.toString + "\"" + ", " + "\"" + iot.OutT.toString + "\"" + ")"
  
    def genScala(node: PipelineDecl) : String = {
    println("pipelinedecl")
    if(isMain(node.iotype))
      mainPipeName = node.id.toString
    "def " + node.id.toString + genScala(node.parmvardecls) + " : PipeLine" + " = " +
    openBlock + "val g = new PipeLine" + newLine + newLine +
    genScala(node.body) + newLine + "g" + closeBlock
  }
  def genScala(node: FilterDecl) : String = {
    "def " + node.id.toString + genScala(node.parmvardecls) + " : FilterNode" + " =" +
    openBlock +
    "val name = " + "\"" + node.id.toString + "_" + Counter.getNumber + "\"" + newLine +
    "val intT = " + "\"" + node.iotype.InT.toString + "\"" + newLine +
    "val outT = " + "\"" + node.iotype.OutT.toString + "\"" + newLine +
    "var push : Option[Int] = None" + newLine +
    "var pop : Option[Int] = None" + newLine +
    "var peek : Option[Int] = None" + newLine +
    genScala(node.body) +
    "FilterNode(None, name, intT, outT, push, pop, peek)" +
    closeBlock
  }

  /**
   * Filterbodydecl consists of vardecllist, initfunction, workfunction.
   * Push, pop, peek rate eval codes are written here.
   */
  def genScala(node: FilterBodyDecl) : String = {
    node.decls.decls.map { f=> 
      f match {
      case vardecllist: VarDeclList =>
        genScala(vardecllist) + newLine
      case initf: InitFunctionDecl =>
        genScala(initf.Body) + newLine
      //push, pop, peek rate eval
      case workf: WorkFunctionDecl =>
        genScala(workf.IO_rates) //watch out!
      case _=> println("unimplemented") 
      ""
      }
    }.mkString(newLine) + newLine
  }
  def genScala(node: InitFunctionDecl) : String = genScala(node.Body)
  def genScala(node: WorkFunctionDecl) : String = genScala(node.IO_rates)
  def genScala(node: DataRates) : String = {
    println("datarates")
    node.Rates.decls.map {
      f=> f match {
        case push :PushDecl=> "push = " + "Some(" + genScala(push.PushRate) + ")"
        case pop: PopDecl=> "pop = " + "Some(" + genScala(pop.PopRate) + ")"
        case peek: PeekDecl=> "peek = " + "Some(" + genScala(peek.PeekRate) + ")"
        case _=> 
          println("inimplemented")
          unimplemented
      }
    }.mkString(newLine) + newLine
  }
  
  def genScala(node: HelperFunctionDecl) : String = {
    "def " + node.id.toString + genScala(node.parmvardecls) +
    " : " + node.ReturnType.toString + " = " + genScala(node.Body)
  }
    
  
  def genScala(node: StreamConstructExpr) : String = {
    "g.add(" + node.id.toString + genScala(node.Parms) + ")"
  }
    
  
  def genScala(node: UnaryExpr) : String = {
    val uop = node.opc.toString
    if(uop=="++")
      genScala(node.input) + " = " + genScala(node.input) + " + 1"
    else if(uop=="--")
      genScala(node.input) + " = " + genScala(node.input) + " - 1" 
    else uop + genScala(node.input)
  }
  
  
  def genScala(node: BinaryExpr) : String = genScala(node.lhs) + 
  " " + node.opc.toString + " " + genScala(node.rhs)
  def genScala(node: ParenExpr) : String = "(" + genScala(node.input) + ")"
  
  def genScala(node: CallExpr) : String = { 
    node.id.toString + node.Parms
  }
  def genScala(node: ArraySubscriptExpr) : String = genScala(node.base) + "(" + genScala(node.idx) + ")"
  def genScala(node: FieldExpr) : String = genScala(node.base) + "."+ genScala(node.field)
  
  def genScala(node: InitArrayExpr) : String = {
    "List" + genScala(node.array)
  }
  def genScala(node : DeclStmt) : String = 
    node.Decls.decls.map(decl=>genScala(decl)).mkString(", ")

  def genScala(node: StructDecl) : String = {
    "class " + node.id.toString + newLine + openBlock +
     genScala(node.fielddecls) + closeBlock
  }
  def genScala(fd : FieldDecl) : String = {
    val vardecl = "var " + fd.id.toString + ":" + genScala(fd.T) + " = "
    val init : String = {
      fd.T match {
        case arr: ConstantArrayType =>
           {
            val dim = new scala.collection.mutable.ListBuffer[String]()
            val addstr = "Array ofDim[" + genScala(arr.ElementType) + "]("
            dim += genScala(arr.size)
            var t = arr.ElementType
            while(t.isInstanceOf[ConstantArrayType]) {
              dim += genScala(arr.size)
              t= t.asInstanceOf[ConstantArrayType].ElementType
            } 
            addstr + dim.mkString(",") + ")"
          }
          
        //not array    
        case int: IntType => "0"
        case float: FloatType => "0"
        case bool: BooleanType => "false"
      }
    }
    vardecl + init
  }
  //pattern matcher
  def genScala(node : AST) : String = node match {
    case decl : Decl ⇒ decl match {
      case vd : VarDecl ⇒ 
        genScala(vd)
      case nd: NoDecl => ""
      
      case progd: ProgramDecl=> genScala(progd)
      
      // Parameter declaration:
      case pv:ParmVarDecl=> genScala(pv)
      
      case fd:FilterDecl=> genScala(fd)
      case pd:PipelineDecl=> genScala(pd)
      
      case sj:SplitJoinDecl=> genScala(sj)
      case fl:FeedbackLoopDecl=>""
      case sd:StructDecl=>genScala(sd)
      case bfd:BuiltInFunctionDecl=> ""
      case bsd:BuiltInStreamDecl=> ""
      case gd:GlobalDecl=>genScala(gd)
      case ifd:InitFunctionDecl=> genScala(ifd)
      case d:PreWorkFunctionDecl=> ""
      case wfd:WorkFunctionDecl=> genScala(wfd)
      
      case hfd:HelperFunctionDecl=> genScala(hfd)
      
      case handlerd:HandlerFunctionDecl=> handlerd.toString
      
      case pushdecl:PushDecl=> genScala(pushdecl)
      
      case popdecl:PopDecl=> genScala(popdecl)
      
      case peekdecl:PeekDecl=> genScala(peekdecl)
      
      case fbd:FilterBodyDecl=> genScala(fbd)
      
      case fielddecl:FieldDecl=> genScala(fielddecl)
      case vdl:VarDeclList=> genScala(vdl)
    }
    case dr:DataRates=> genScala(dr)
      
    case stmt : Stmt ⇒ stmt match {
      case NoStmt() => "" 
      case ds : DeclStmt => genScala(ds)
        
      case ct : CompoundStmt ⇒ genScala(ct)
      case push : PushStmt ⇒  push.toString
      case splitstmt : SplitStmt ⇒  genScala(splitstmt)
      case joinStmt : JoinStmt⇒  genScala(joinStmt)
      case enqueuestmt : EnqueueStmt⇒ enqueuestmt.toString
      case forstmt : ForStmt⇒ genScala(forstmt)
        
      case ifstmt : IfStmt ⇒ 
        genScala(ifstmt)
        
      case ws : WhileStmt⇒ 
        genScala(ws)
        
      case retstmt: ReturnStmt⇒ 
        genScala(retstmt)
        
      case dowhilestmt: DoWhileStmt⇒
        genScala(dowhilestmt)
        
      case BreakStmt() ⇒ "break"
      case ContinueStmt() ⇒ "continue"
      case CommaStmt() ⇒ ", "
      case msg:MsgStmt⇒ ""
      case expr : Expr ⇒ expr match {
        case NoExpr()⇒ ""
        case PopExpr()⇒ ""
        case StarExpr()⇒ ""
        case peek:PeekExpr ⇒ ""
        case range: RangeExpr ⇒ ""
        case call: CallExpr⇒ genScala(call)
        case initarray:InitArrayExpr⇒ genScala(initarray)
        case construct: ConstructExpr⇒ construct match { 
          case as : AnonymousStreamExpr ⇒ genScala(as)
          case af: ConstructAnonFilterExpr ⇒ genScala(af)
          case ap : AnonymousPipelineExpr ⇒ genScala(ap)
          case asj: AnonymousSplitJoinExpr⇒ genScala(asj)
          case afb: AnonymousFeedbackLoopExpr⇒ genScala(afb.body)
          case streamconstruct : StreamConstructExpr⇒  genScala(streamconstruct)
        }
        case  pse: PortalSpecExpr⇒ pse.toString
        case id: ID ⇒ id.toString
        case lit : Literal⇒ lit.lexeme
        case strlit : StringLiteral⇒ strlit.lexeme
        case intlit: IntLiteral⇒ intlit.lexeme
        case complexlit: ComplexLiteral ⇒ complexlit.lexeme
        case floatlit: FloatLiteral⇒ floatlit.lexeme
        case boollit: BooleanLiteral⇒ boollit.lexeme
        case pilit: PiLiteral⇒ "3.141592"
        case terExpr: TernaryExpr ⇒ " ? "
        case bop: BinaryExpr ⇒
          genScala(bop)            
        case uop: UnaryExpr⇒ 
          genScala(uop)
        case paren: ParenExpr⇒ 
          genScala(paren)
        case cast: CastExpr⇒ cast.toString
        case array: ArraySubscriptExpr⇒ genScala(array)
        case field: FieldExpr⇒ genScala(field)
        case dre:DeclRefExpr=> dre.toString
        case _=>
        println("unimplemented")
        "unimplemented"
      }
      case _⇒ 
        println("exception")
        "exception"
    }
    case iot: IOTypeSpec=> genScala(iot)
    case sgs: ScatherGatherSpec=> sgs.toString
    case ty: Type => ty.toString
  }  
}