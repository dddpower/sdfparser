//package interpreter
//import ast._
//import common.Counter 
//import WritingTool._
//import common.WritingTool
//
//object LaminarWriter2 {
//  Counter.reSet
//  var mainPipeName:String = _
//  
//  val importstr = "import graph.GraphSpace._" + newLine
//  def genToDotCommand(outputfilename: String) : String = {
//    newLine + mainPipeName + ".toDotFile(\"" + 
//    outputfilename + "\")" + newLine
//  }
//  def genSDF(node: WhileStmt) : String =
//    "while(" + genSDF(node.Condition) + ")" + openBlock +
//    genSDF(node.Body) +
//    closeBlock
//  
//  def genSDF(node : ForStmt) : String =
//    "for(" + genSDF(node.Init) + semcol + 
//    genSDF(node.Condition) + semcol + 
//    genSDF(node.Incr) +")" + openBlock +
//    genSDF(node.Body) + closeBlock
//  
//    def genSDF(node:VarDecl) : String = {
//    val vardecl = node.T.toString()+node.id.toString()
//    val init : String = 
//      if(node.init==NoExpr())""
//      else genSDF(node.init)   
//    vardecl + init
//  }
//  
//  def genSDF(node: IfStmt) : String = {
//    val ifstmt = "if(" + genSDF(node.Condition) + ")" + genSDF(node.ifBody)
//    val elsestmt : String = 
//      if(node.elseBody!=NoStmt())
//        "else" + genSDF(node.elseBody)
//      else ""
//    ifstmt + elsestmt
//  }
//  def genSDF(node: ReturnStmt) : String = genSDF(node.Value)
//  
//  def genSDF(node : DoWhileStmt) : String =
//    "do" + openBlock + 
//    genSDF(node.Body) + 
//    closeBlock + "while(" + genSDF(node.Condition) + ")" + 
//    newLine
//  
//  def genSDF(body: CompoundStmt) : String = openBlock + 
//    body.stmts.map(stmt=>genSDF(stmt)).mkString(endStmt) + closeBlock
//  
//    
//  def isMain(node: IOTypeSpec) : Boolean = node.InT.toString=="VoidType()" &&
//    node.OutT.toString()=="VoidType()"
//  
//  def genSDF(node: PipelineDecl) : String = {
//    if(isMain(node.iotype)) mainPipeName = node.id.toString
//    sdf + " " + mainPipeName + openBlock + closeBlock
//    
//  }
//  def genSDF(node: AnonymousFilterExpr) : String = "g.add(" + openBlock +
//    "val g = new Filter("+ genSDF(node.iotype) + ")" + genSDF(node.body) + newLine + "g" + closeBlock + ")"
//  
//  def genSDF(node: AnonymousPipelineExpr) : String = "g.add(" + openBlock +
//    "val g = new PipeLine(" + genSDF(node.iotype) + ")" + newLine + newLine + genSDF(node.body) + newLine + "g" + closeBlock + ")"
//  
//  def genSDF(node: AnonymousSplitJoinExpr) : String = "g.add(" + openBlock +
//    "val g = new SplitJoin("+ genSDF(node.iotype) + ")" + 
//    newLine + newLine + genSDF(node.body) + newLine + "g" + closeBlock + ")"
//  
//  //same as pipelinedecl
//  def genSDF(node: SplitJoinDecl) : String = {
//    "def " + node.id.toString + genSDF(node.parmvardecls) + " : SplitJoin" + " = " + newLine +
//    openBlock + "val g = new SplitJoin(" + genSDF(node.iotype) + ")" + 
//    newLine + newLine + genSDF(node.body) + newLine + "g" + closeBlock
//  }
//  def genSDF(node: ProgramDecl) : String = {
//    genSDF(node.decls)
//  }
//
//  def genSDF(node: DeclList) : String = node.decls.map(decl=>genSDF(decl)).mkString(newLine)
//  
//  def genSDF(node : ExprList) : String = "(" + node.exprs.map(expr=>genSDF(expr)).mkString(", ") + ")"
//  
//  def genSDF(node: VarDeclList) : String = node.vardecls.map(vardecl=>genSDF(vardecl)).mkString(newLine) + newLine
//  
//  def genSDF(node:ParmVarDecl) : String =
//    node.id.toString() + ":" + genSDF(node.T)
//  def genSDF(node: ParmVarDeclList) : String = "(" + 
//  node.parmvardecls.map(decl=>genSDF(decl)).mkString(", ") + ")"
//  
//  
//  def genSDF(node: GlobalDecl) : String = {
//    genSDF(node.decls) + newLine + genSDF(node.initDecl)
//  }
//  
//  def genSDF(node: InitFunctionDecl) : String = genSDF(node.Body)
//  
//  def genSDF(node: SplitStmt) : String = ""
//  
//  def genSDF(node: JoinStmt) : String = ""
//  
//  def genSDF(iot:IOTypeSpec) : String = 
//    "(" + "\"" +iot.InT.toString + "\"" + ", " + "\"" + iot.OutT.toString + "\"" + ")"
//  
//  def genSDF(node: FilterDecl) : String = {
//    "def " + node.id.toString + genSDF(node.parmvardecls) + 
//    " = new Filter(" + genSDF(node.iotype) + "," + 
//    "\"" + node.id.toString +"\"" + "," + 
//    //"None" +
//    "Some(" + node.toString +")" +
//    ")"
//  }
//    
//  def genSDF(node: HelperFunctionDecl) : String = {
//    "def " + node.id.toString + genSDF(node.parmvardecls) +
//    " : " + node.ReturnType.toString + " = " + genSDF(node.Body)
//  }
//    
//  
//  def genSDF(node: StreamConstructExpr) : String = {
//    "g.add(" + node.id.toString + genSDF(node.Parms) + ")"
//  }
//    
//  
//  def genSDF(node: UnaryExpr) : String = {
//    val uop = node.opc.toString
//    if(uop=="++")
//      genSDF(node.input) + " = " + genSDF(node.input) + " + 1"
//    else if(uop=="--")
//      genSDF(node.input) + " = " + genSDF(node.input) + " - 1" 
//    else uop + genSDF(node.input)
//  }
//  
//  
//  def genSDF(node: BinaryExpr) : String = genSDF(node.lhs) + 
//  " " + node.opc.toString + " " + genSDF(node.rhs)
//  def genSDF(node: ParenExpr) : String = "(" + genSDF(node.input) + ")"
//  
//  def genSDF(node: CallExpr) : String = { 
//    node.id.toString + node.Parms
//  }
//  def genSDF(node: ArraySubscriptExpr) : String = genSDF(node.base) + "(" + genSDF(node.idx) + ")"
//  def genSDF(node: FieldExpr) : String = genSDF(node.base) + "."+ genSDF(node.field)
//  
//  def genSDF(node: InitArrayExpr) : String = {
//    "List" + genSDF(node.array)
//  }
//  def genSDF(node : DeclStmt) : String = 
//    node.Decls.decls.map(decl=>genSDF(decl)).mkString(", ")
//
//  def genSDF(node: StructDecl) : String = {
//    "class " + node.id.toString + newLine + openBlock +
//     genSDF(node.fielddecls) + closeBlock
//  }
//  def genSDF(fd : FieldDecl) : String = {
//    val vardecl = "var " + fd.id.toString + ":" + genSDF(fd.T) + " = "
//    val init : String = {
//      fd.T match {
//        case arr: ConstantArrayType =>
//           {
//            val dim = new scala.collection.mutable.ListBuffer[String]()
//            val addstr = "Array ofDim[" + genSDF(arr.ElementType) + "]("
//            dim += genSDF(arr.size)
//            var t = arr.ElementType
//            while(t.isInstanceOf[ConstantArrayType]) {
//              dim += genSDF(arr.size)
//              t= t.asInstanceOf[ConstantArrayType].ElementType
//            } 
//            addstr + dim.mkString(",") + ")"
//          }
//          
//        //not array    
//        case int: IntType => "0"
//        case float: FloatType => "0"
//        case bool: BooleanType => "false"
//      }
//    }
//    vardecl + init
//  }
//  //pattern matcher
//  def genSDF(node : AST) : String = node match {
//    case decl : Decl ⇒ decl match {
//      case vd : VarDecl ⇒ 
//        genSDF(vd)
//      case nd: NoDecl => ""
//      
//      case progd: ProgramDecl=> genSDF(progd)
//      
//      // Parameter declaration:
//      case pv:ParmVarDecl=> genSDF(pv)
//      
//      case fd:FilterDecl=> genSDF(fd)
//      case pd:PipelineDecl=> genSDF(pd)
//      
//      case sj:SplitJoinDecl=> genSDF(sj)
//      case fl:FeedbackLoopDecl=>""
//      case sd:StructDecl=>genSDF(sd)
//      case bfd:BuiltInFunctionDecl=> ""
//      case bsd:BuiltInStreamDecl=> ""
//      case gd:GlobalDecl=>genSDF(gd)
//      case ifd:InitFunctionDecl=> genSDF(ifd)
//      case d:PreWorkFunctionDecl=> ""
//      case wfd:WorkFunctionDecl=> ""
//      
//      case hfd:HelperFunctionDecl=> genSDF(hfd)
//      
//      case handlerd:HandlerFunctionDecl=> handlerd.toString
//      
//      case pushdecl:PushDecl=> pushdecl.toString
//      
//      case popdecl:PopDecl=> popdecl.toString
//      
//      case peecdecl:PeekDecl=> peecdecl.toString
//      
//      case fbd:FilterBodyDecl=> ""
//      
//      case fielddecl:FieldDecl=> genSDF(fielddecl)
//      case vdl:VarDeclList=> genSDF(vdl)
//    }
//    case dr:DataRates=> dr.toString
//      
//    case stmt : Stmt ⇒ stmt match {
//      case NoStmt() => "" 
//      case ds : DeclStmt => genSDF(ds)
//        
//      case ct : CompoundStmt ⇒ genSDF(ct)
//      case push : PushStmt ⇒  push.toString
//      case splitstmt : SplitStmt ⇒  genSDF(splitstmt)
//      case joinStmt : JoinStmt⇒  genSDF(joinStmt)
//      case enqueuestmt : EnqueueStmt⇒ enqueuestmt.toString
//      case forstmt : ForStmt⇒ genSDF(forstmt)
//        
//      case ifstmt : IfStmt ⇒ 
//        genSDF(ifstmt)
//        
//      case ws : WhileStmt⇒ 
//        genSDF(ws)
//        
//      case retstmt: ReturnStmt⇒ 
//        genSDF(retstmt)
//        
//      case dowhilestmt: DoWhileStmt⇒
//        genSDF(dowhilestmt)
//        
//      case BreakStmt() ⇒ "break"
//      case ContinueStmt() ⇒ "continue"
//      case CommaStmt() ⇒ ", "
//      case msg:MsgStmt⇒ ""
//      case expr : Expr ⇒ expr match {
//        case NoExpr()⇒ ""
//        case PopExpr()⇒ ""
//        case StarExpr()⇒ ""
//        case peek:PeekExpr ⇒ ""
//        case range: RangeExpr ⇒ ""
//        case call: CallExpr⇒ genSDF(call)
//        case initarray:InitArrayExpr⇒ genSDF(initarray)
//        case construct: ConstructExpr⇒ construct match { 
//          case as : AnonymousStreamExpr ⇒ genSDF(as)
//          case af: AnonymousFilterExpr ⇒ genSDF(af)
//          case ap : AnonymousPipelineExpr ⇒ genSDF(ap)
//          case asj: AnonymousSplitJoinExpr⇒ genSDF(asj)
//          case afb: AnonymousFeedbackLoopExpr⇒ genSDF(afb.body)
//          case streamconstruct : StreamConstructExpr⇒  genSDF(streamconstruct)
//        }
//        case  pse: PortalSpecExpr⇒ pse.toString
//        case id: ID ⇒ id.toString
//        case lit : Literal⇒ lit.lexeme
//        case strlit : StringLiteral⇒ strlit.lexeme
//        case intlit: IntLiteral⇒ intlit.lexeme
//        case complexlit: ComplexLiteral ⇒ complexlit.lexeme
//        case floatlit: FloatLiteral⇒ floatlit.lexeme
//        case boollit: BooleanLiteral⇒ boollit.lexeme
//        case pilit: PiLiteral⇒ "3.141592"
//        case terExpr: TernaryExpr ⇒ " ? "
//        case bop: BinaryExpr ⇒
//          genSDF(bop)            
//        case uop: UnaryExpr⇒ 
//          genSDF(uop)
//        case paren: ParenExpr⇒ 
//          genSDF(paren)
//        case cast: CastExpr⇒ cast.toString
//        case array: ArraySubscriptExpr⇒ genSDF(array)
//        case field: FieldExpr⇒ genSDF(field)
//        case dre:DeclRefExpr=> dre.toString
//      }
//      case _⇒ "exception"
//    }
//    case iot: IOTypeSpec=> genSDF(iot)
//    case sgs: ScatherGatherSpec=> sgs.toString
//    case ty: Type => ty.toString
//  }  
//} 