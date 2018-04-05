//package interpreter
//import ast._
//
//object ConstantPropagation {
//  private var hasQOp : Boolean = false
//  // on when a statement contains only a unary expression
//  private var unaryStmt : Boolean = false
//  private var constFile : String = _
//  
//  //record name of global helper functions
//  val helperFuncs : List[String] = Nil
//  //container for list of var list
//  val valdecllist : List[VarDecl] = Nil
//  
//  
////   def getIters(forstmt: ForStmt) : List[Integer] = {
////        // name of the iterative variable
////        var iterVar : String = "";
////        // to calculate loop iter num
////        var lowerBound : Int = 0;
////        var upperBound : Int = 0;
////        var interval : Int= 0;
////        // partial evaluators
////        PartialEvaluator initEval = new PartialEvaluator(varBlockList);
////        int initVal = 0;
////        PartialEvaluator condEval = new PartialEvaluator(varBlockList);
////        int condVal = 0;
////        int condOp = -1;
////        PartialEvaluator incrEval = new PartialEvaluator(varBlockList);
////
////        boolean resolved = true;
////        // evalute init
////        if (resolved && stmt.getInit() != null) {
////            // e.g., int i = 0
////            if (stmt.getInit() instanceof StmtVarDecl) {
////                assert ((StmtVarDecl)stmt.getInit()).getNumVars() == 1;
////                iterVar = ((StmtVarDecl)stmt.getInit()).getName(0);
////                initVal = (int) ((StmtVarDecl)stmt.getInit()).getInit(0).accept(initEval);
////                if (!initEval.isResolved()){
////                    resolved = false;
////                }
////            // e.g., i = 0
////            } else if (stmt.getInit() instanceof StmtAssign) {
////                iterVar = (String)((StmtAssign)stmt.getInit()).getLHS().toString();
////                initVal = (int)((StmtAssign)stmt.getInit()).getRHS().accept(initEval);
////                if (!initEval.isResolved()){
////                    resolved = false;
////                }
////            } else {
////            // other stmt types are regarded as not-resolvable
////                resolved = false;
////            }
////        }
////
////        // evaluate cond
////        if (resolved && stmt.getCond() != null) {
////            if (stmt.getCond() instanceof ExprBinary) {
////                // record op for later use.
////                condOp = ((ExprBinary)stmt.getCond()).getOp();
////                // check if the iteration variable is consistant
////                //          : iter var should be on the left side
////                String localIterVar = 
////                    (String)((ExprBinary)stmt.getCond()).getLeft().toString();
////                if (!iterVar.equals(localIterVar)) {
////                    resolved = false;        
////                }
////                condVal = (int) ((ExprBinary)stmt.getCond()).getRight().accept(condEval);
////                if (!condEval.isResolved()){
////                    resolved = false;
////                }
////            } else {
////            // Other expr are regarded as not-resolvable
////                resolved = false;
////            }
////        }
////
////        // evaluate incr
////        if (resolved && stmt.getIncr() != null){
////            // e.g., i += 1
////            if (stmt.getIncr() instanceof StmtAssign){
////                String localIterVar = (String)((StmtAssign)stmt.getIncr()).getLHS().toString();
////                if (!iterVar.equals(localIterVar)) {
////                    resolved = false;        
////                }
////                
////                interval = (int) ((StmtAssign)stmt.getIncr()).getRHS().accept(incrEval);
////                if (!incrEval.isResolved()){
////                    resolved = false;
////                }
////                int op = ((StmtAssign)stmt.getIncr()).getOp(); 
////                if (op == ExprBinary.BINOP_ADD){
////                }else if (op == ExprBinary.BINOP_SUB){
////                    interval = interval*(-1);
////                }else{
////                    resolved = false;
////                }
////            // Yousun Ko : e.g., i++
////            }else if (stmt.getIncr() instanceof StmtExpr){
////                Expression expr = ((StmtExpr)stmt.getIncr()).getExpression();
////                if (expr instanceof ExprUnary){
////                    String localIterVar = (String)((ExprUnary) expr).getExpr().toString();
////                    if (!iterVar.equals(localIterVar)) {
////                        resolved = false;        
////                    }
////
////                    int op = ((ExprUnary)expr).getOp();
////                    if (op == ExprUnary.UNOP_POSTINC
////                            || op == ExprUnary.UNOP_PREINC){
////                        interval = 1;
////                    }else if (op == ExprUnary.UNOP_POSTDEC
////                            || op == ExprUnary.UNOP_PREDEC){
////                        interval = -1;
////                    }else{
////                        resolved = false;
////                    }
////                }else{
////                    resolved = false;
////                }
////            } else {
////                resolved = false;
////            }
////        }
////        /* TBD: ????*/
////        /*
////        // check if the iterVar has been updated in body
////        if (resolved){
////            // backup the previous values
////            VarBlock oldConstList = new VarBlock(varBlockList.get(varBlockList.size()-1));
////            varBlockList.get(varBlockList.size()-1).update(iterVar, Integer.toString(initVal));
////            VarBlock newConstList = new VarBlock(varBlockList.get(varBlockList.size()-1));
////            // if iterVar is updated within a loop, then 
////            if (!(Integer.toString(initVal).equals(newConstList.getVal(iterVar)))){
////                resolved = false;
////            }
////
////            // restore the constList
////            varBlockList.set(varBlockList.size()-1, oldConstList);
////        }
////        */
////        /**/
////
////        // do traverse
////        List<Integer> iters = new ArrayList<Integer>();
////        if (resolved) {
////            // calculate the number of iteration
////            if (condOp == ExprBinary.BINOP_LE){
////                for ( int i = initVal; i <= condVal; i+=interval ) {
////                    iters.add(i);
////                }
////            }else if (condOp == ExprBinary.BINOP_LT){
////                for ( int i = initVal; i < condVal; i+=interval ) {
////                    iters.add(i);
////                }
////            }else if (condOp == ExprBinary.BINOP_GE){
////                for ( int i = initVal; i >= condVal; i+=interval ) {
////                    iters.add(i);
////                }
////            }else if (condOp == ExprBinary.BINOP_GT){
////                for ( int i = initVal; i > condVal; i+=interval ) {
////                    iters.add(i);
////                }
////            // Other expr are regarded as not-resolvable
////            }else{
////                return null;
////            }
////            return iters;
////        } else {  // not resolved
////            return null;
////        }
////    }
////        val result : Stmt = forstmt
////
////        // get number of iteration
////        val iters : List[Int] = getIters(stmt);
////        String iterVar = getIterVar(stmt);
////        // constantness of the iterVar ends here
////        Statement newInit = (Statement) stmt.getInit().accept(this);
////        Expression newCond = (Expression) stmt.getCond().accept(this);
////        Statement newIncr = (Statement) stmt.getIncr().accept(this);
////        hasQOp = false;
////        Statement newBody = (Statement) stmt.getBody().accept(this);
////        // do not unroll if the body does not contain queue operations
////        if (!hasQOp){
////            iters = null;
////        }
////
////        stmt.setIters(iters);
////        stmt.setIterVar(iterVar);
////
////        if (!(newInit == stmt.getInit() && newCond == stmt.getCond() &&
////            newIncr == stmt.getIncr() && newBody == stmt.getBody())) {
////            result = new StmtFor(stmt.getContext(), newInit, newCond, newIncr, newBody, stmt.getIterVar(), stmt.getIters());
////        }
////        return result;
////    }
////  
////  //pattern matcher
////  def visit(node : AST) : String = node match {
////    case decl : Decl ⇒ decl match {
////      case vd : VarDecl ⇒ 
////        visit(vd)
////      case nd: NoDecl => ""
////      
////      case progd: ProgramDecl=> visit(progd)
////      
////      // Parameter declaration:
////      case pv:ParmVarDecl=> visit(pv)
////      
////      case fd:FilterDecl=> visit(fd)
////      case pd:PipelineDecl=> visit(pd)
////      
////      case sj:SplitJoinDecl=> visit(sj)
////      case fl:FeedbackLoopDecl=>""
////      case sd:StructDecl=>visit(sd)
////      case bfd:BuiltInFunctionDecl=> ""
////      case bsd:BuiltInStreamDecl=> ""
////      case gd:GlobalDecl=>visit(gd)
////      case ifd:InitFunctionDecl=> visit(ifd)
////      case d:PreWorkFunctionDecl=> ""
////      case wfd:WorkFunctionDecl=> ""
////      
////      case hfd:HelperFunctionDecl=> visit(hfd)
////      
////      case handlerd:HandlerFunctionDecl=> handlerd.toString
////      
////      case pushdecl:PushDecl=> pushdecl.toString
////      
////      case popdecl:PopDecl=> popdecl.toString
////      
////      case peecdecl:PeekDecl=> peecdecl.toString
////      
////      case fbd:FilterBodyDecl=> ""
////      
////      case fielddecl:FieldDecl=> visit(fielddecl)
////      case vdl:VarDeclList=> visit(vdl)
////    }
////    case dr:DataRates=> dr.toString
////      
////    case stmt : Stmt ⇒ stmt match {
////      case NoStmt() => "" 
////      case ds : DeclStmt => visit(ds)
////        
////      case ct : CompoundStmt ⇒ visit(ct)
////      case push : PushStmt ⇒  push.toString
////      case splitstmt : SplitStmt ⇒  visit(splitstmt)
////      case joinStmt : JoinStmt⇒  visit(joinStmt)
////      case enqueuestmt : EnqueueStmt⇒ enqueuestmt.toString
////      case forstmt : ForStmt⇒ visit(forstmt)
////        
////      case ifstmt : IfStmt ⇒ 
////        visit(ifstmt)
////        
////      case ws : WhileStmt⇒ 
////        visit(ws)
////        
////      case retstmt: ReturnStmt⇒ 
////        visit(retstmt)
////        
////      case dowhilestmt: DoWhileStmt⇒
////        visit(dowhilestmt)
////        
////      case BreakStmt() ⇒ "break"
////      case ContinueStmt() ⇒ "continue"
////      case CommaStmt() ⇒ ", "
////      case msg:MsgStmt⇒ ""
////      case expr : Expr ⇒ expr match {
////        case NoExpr()⇒ ""
////        case PopExpr()⇒ ""
////        case StarExpr()⇒ ""
////        case peek:PeekExpr ⇒ ""
////        case range: RangeExpr ⇒ ""
////        case call: CallExpr⇒ visit(call)
////        case initarray:InitArrayExpr⇒ visit(initarray)
////        case construct: ConstructExpr⇒ construct match { 
////          case as : AnonymousStreamExpr ⇒ visit(as)
////          case af: AnonymousFilterExpr ⇒ visit(af)
////          case ap : AnonymousPipelineExpr ⇒ visit(ap)
////          case asj: AnonymousSplitJoinExpr⇒ visit(asj)
////          case afb: AnonymousFeedbackLoopExpr⇒ visit(afb.body)
////          case streamconstruct : StreamConstructExpr⇒  visit(streamconstruct)
////        }
////        case  pse: PortalSpecExpr⇒ pse.toString
////        case id: ID ⇒ id.toString
////        case lit : Literal⇒ lit.lexeme
////        case strlit : StringLiteral⇒ strlit.lexeme
////        case intlit: IntLiteral⇒ intlit.lexeme
////        case complexlit: ComplexLiteral ⇒ complexlit.lexeme
////        case floatlit: FloatLiteral⇒ floatlit.lexeme
////        case boollit: BooleanLiteral⇒ boollit.lexeme
////        case pilit: PiLiteral⇒ "3.141592"
////        case terOperator: TernaryOperator ⇒ " ? "
////        case bop: BinaryOperator ⇒
////          visit(bop)            
////        case uop: UnaryOperator⇒ 
////          visit(uop)
////        case paren: ParenExpr⇒ 
////          visit(paren)
////        case cast: CastExpr⇒ cast.toString
////        case array: ArraySubscriptExpr⇒ visit(array)
////        case field: FieldExpr⇒ visit(field)
////        case dre:DeclRefExpr=> dre.toString
////      }
////      case _⇒ "exception"
////    }
////    case iot: IOTypeSpec=> iot.toString
////    case sgs: ScatherGatherSpec=> sgs.toString
////    case ty: Type => ty.toString
////  }
//}