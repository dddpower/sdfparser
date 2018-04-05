//package interpreter
//import ast._
//
//
//object ConstantPropergation {
//  private var hasQOp : Boolean = false
//  private var unaryStmt : Boolean = false
//  val helperFuncs : List[String] = Nil
//  val vardecllist : List[VarDecl] = Nil
//  
//  def getIters(forstmt: ForStmt) : List[Int] = {
//  // name of the iterative variable
//        var iterVar : String = ""
//        // to calculate loop iter num
//        var lowerBound : Int = 0
//        var upperBound : Int = 0
//        var interval : Int = 0
//        // partial evaluators
//        val initEval = new PartialEvaluator(vardeclList)
//        var initVal : Int = 0
//        val condEval = new PartialEvaluator(vardeclList)
//        var condVal : Int = 0
//        var condOp : Int = -1
//        val incrEval = new PartialEvaluator(vardeclList)
//
//        var resolved = true
//        // evalute init
//        if (resolved && stmt.getInit() != null) {
//            // e.g., int i = 0
//            if (stmt.getInit() instanceof StmtVarDecl) {
//                assert ((StmtVarDecl)stmt.getInit()).getNumVars() == 1
//                iterVar = ((StmtVarDecl)stmt.getInit()).getName(0)
//                initVal = (int) ((StmtVarDecl)stmt.getInit()).getInit(0).accept(initEval)
//                if (!initEval.isResolved()){
//                    resolved = false
//                }
//            // e.g., i = 0
//            } else if (stmt.getInit() instanceof StmtAssign) {
//                iterVar = (String)((StmtAssign)stmt.getInit()).getLHS().toString()
//                initVal = (int)((StmtAssign)stmt.getInit()).getRHS().accept(initEval)
//                if (!initEval.isResolved()){
//                    resolved = false
//                }
//            } else {
//            // other stmt types are regarded as not-resolvable
//                resolved = false
//            }
//        }
//
//        // evaluate cond
//        if (resolved && stmt.getCond() != null) {
//            if (stmt.getCond() instanceof ExprBinary) {
//                // record op for later use.
//                condOp = ((ExprBinary)stmt.getCond()).getOp()
//                // check if the iteration variable is consistant
//                //          : iter var should be on the left side
//                String localIterVar = 
//                    (String)((ExprBinary)stmt.getCond()).getLeft().toString()
//                if (!iterVar.equals(localIterVar)) {
//                    resolved = false        
//                }
//                condVal = (int) ((ExprBinary)stmt.getCond()).getRight().accept(condEval)
//                if (!condEval.isResolved()){
//                    resolved = false
//                }
//            } else {
//            // Other expr are regarded as not-resolvable
//                resolved = false
//            }
//        }
//
//        // evaluate incr
//        if (resolved && stmt.getIncr() != null){
//            // e.g., i += 1
//            if (stmt.getIncr() instanceof StmtAssign){
//                String localIterVar = (String)((StmtAssign)stmt.getIncr()).getLHS().toString()
//                if (!iterVar.equals(localIterVar)) {
//                    resolved = false        
//                }
//                
//                interval = (int) ((StmtAssign)stmt.getIncr()).getRHS().accept(incrEval)
//                if (!incrEval.isResolved()){
//                    resolved = false
//                }
//                int op = ((StmtAssign)stmt.getIncr()).getOp() 
//                if (op == ExprBinary.BINOP_ADD){
//                }else if (op == ExprBinary.BINOP_SUB){
//                    interval = interval*(-1)
//                }else{
//                    resolved = false
//                }
//            // Yousun Ko : e.g., i++
//            }else if (stmt.getIncr() instanceof StmtExpr){
//                Expression expr = ((StmtExpr)stmt.getIncr()).getExpression()
//                if (expr instanceof ExprUnary){
//                    String localIterVar = (String)((ExprUnary) expr).getExpr().toString()
//                    if (!iterVar.equals(localIterVar)) {
//                        resolved = false        
//                    }
//
//                    int op = ((ExprUnary)expr).getOp()
//                    if (op == ExprUnary.UNOP_POSTINC
//                            || op == ExprUnary.UNOP_PREINC){
//                        interval = 1
//                    }else if (op == ExprUnary.UNOP_POSTDEC
//                            || op == ExprUnary.UNOP_PREDEC){
//                        interval = -1
//                    }else{
//                        resolved = false
//                    }
//                }else{
//                    resolved = false
//                }
//            } else {
//                resolved = false
//            }
//        }
//        /* TBD: ????*/
//        /*
//        // check if the iterVar has been updated in body
//        if (resolved){
//            // backup the previous values
//            VarBlock oldConstList = new VarBlock(varBlockList.get(varBlockList.size()-1))
//            varBlockList.get(varBlockList.size()-1).update(iterVar, Integer.toString(initVal))
//            VarBlock newConstList = new VarBlock(varBlockList.get(varBlockList.size()-1))
//            // if iterVar is updated within a loop, then 
//            if (!(Integer.toString(initVal).equals(newConstList.getVal(iterVar)))){
//                resolved = false
//            }
//
//            // restore the constList
//            varBlockList.set(varBlockList.size()-1, oldConstList)
//        }
//        */
//        /**/
//  }
//
//  def visitexprArray : Unit = {
//    
//  }
//  def visitexprBinary : Unit = {
//    
//  }
//  def exprpeek = {
//    
//  }
//  
//  def exprpop = {
//    
//  }
//  def exprunary = {
//    
//  }
//  
//  def exprvar = {
//    
//  }
//  
//  def visitFieldDecl = {
//    
//  }
//  
//  def visitFunction = {
//    
//  }
//  def workfunction = {
//    
//  }
//  def visitprogram = {
//    
//  }
//  
//  def visitStmtAssign = {
//    
//  }
//  
//  def visitstmtblock = {
//    
//  }
//  
//  def dowhile = {
//    
//  }
//  
//  def visitfor = {
//    
//  }
//  def visitif = {
//    
//  }
//  def visitpush = {
//    
//  }
//  def visitvardecl = {
//    
//  }
//  def visitwhile = {
//    
//  }
//  def visitStreamspec = {
//    
//  }
////    public Object visitTypeHelper(TypeHelper th) {
////    public Object visitStreamType(StreamType type)
////    public Object visitOther(FENode node)
//  //pattern matcher
//  def constPropergate(node : AST) : String = node match {
//    case decl : Decl ⇒ decl match {
//      case vd : VarDecl ⇒ 
//        constPropergate(vd)
//      case nd: NoDecl => ""
//      
//      case progd: ProgramDecl=> constPropergate(progd)
//      
//      // Parameter declaration:
//      case pv:ParmVarDecl=> constPropergate(pv)
//        
//      case fd:FilterDecl=> constPropergate(fd)
//      case pd:PipelineDecl=> constPropergate(pd)
//      
//      case sj:SplitJoinDecl=> constPropergate(sj)
//      case fl:FeedbackLoopDecl=>""
//      case sd:StructDecl=>constPropergate(sd)
//      case bfd:BuiltInFunctionDecl=> ""
//      case bsd:BuiltInStreamDecl=> ""
//      case gd:GlobalDecl=>constPropergate(gd)
//      case ifd:InitFunctionDecl=> constPropergate(ifd)
//      case d:PreWorkFunctionDecl=> ""
//      case wfd:WorkFunctionDecl=> ""
//      
//      case hfd:HelperFunctionDecl=> constPropergate(hfd)
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
//      case fielddecl:FieldDecl=> constPropergate(fielddecl)
//      case vdl:VarDeclList=> constPropergate(vdl)
//    }
//    case dr:DataRates=> dr.toString
//      
//    case stmt : Stmt ⇒ stmt match {
//      case NoStmt() => "" 
//      case ds : DeclStmt => constPropergate(ds)
//        
//      case ct : CompoundStmt ⇒ constPropergate(ct)
//      case push : PushStmt ⇒  push.toString
//      case splitstmt : SplitStmt ⇒  constPropergate(splitstmt)
//      case joinStmt : JoinStmt⇒  constPropergate(joinStmt)
//      case enqueuestmt : EnqueueStmt⇒ enqueuestmt.toString
//      case forstmt : ForStmt⇒ constPropergate(forstmt)
//        
//      case ifstmt : IfStmt ⇒ 
//        constPropergate(ifstmt)
//        
//      case ws : WhileStmt⇒ 
//        constPropergate(ws)
//        
//      case retstmt: ReturnStmt⇒ 
//        constPropergate(retstmt)
//        
//      case dowhilestmt: DoWhileStmt⇒
//        constPropergate(dowhilestmt)
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
//        case call: CallExpr⇒ constPropergate(call)
//        case initarray:InitArrayExpr⇒ constPropergate(initarray)
//        case construct: ConstructExpr⇒ construct match { 
//          case as : AnonymousStreamConstructExpr ⇒ constPropergate(as)
//          case af: AnonymousFilterConstructExpr ⇒ constPropergate(af)
//          case ap : AnonymousPipelineConstructExpr ⇒ constPropergate(ap)
//          case asj: AnonymousSplitJoinConstructExpr⇒ constPropergate(asj)
//          case afb: AnonymousFeedbackLoopConstructExpr⇒ constPropergate(afb.body)
//          case streamconstruct : StreamConstructExpr⇒  constPropergate(streamconstruct)
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
//        case terOperator: TernaryOperator ⇒ " ? "
//        case bop: BinaryOperator ⇒
//          constPropergate(bop)            
//        case uop: UnaryOperator⇒ 
//          constPropergate(uop)
//        case paren: ParenExpr⇒ 
//          constPropergate(paren)
//        case cast: CastExpr⇒ cast.toString
//        case array: ArraySubscriptExpr⇒ constPropergate(array)
//        case field: FieldExpr⇒ constPropergate(field)
//        case dre:DeclRefExpr=> dre.toString
//      }
//      case _⇒ "exception"
//    }
//    case iot: IOTypeSpec=> iot.toString
//    case sgs: ScatherGatherSpec=> sgs.toString
//    case ty: Type => ty.toString
//  }  
//}