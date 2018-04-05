//package sem
//
//import common.ErrorReporter._
//
//import scala.util.parsing.input.Position
//import scala.util.parsing.input.NoPosition
//import java.io._
//import scala.io.Source
//import ast._
//
//import scala.reflect.runtime.currentMirror
//import scala.tools.reflect.ToolBox
//import java.io.File
//

//object MySemanticAnalysis {
//  val symtab = SymbolTable
//  var topleveldecl : Option[PipelineDecl] = None
//  symtab.initTypeSystem
////  println("inital symboltable")
////  println(symtab.globals)
//  //push global scope at start
//  var currentScope : Option[Scope] = Some(symtab.builtins)
//  var filterTypeSpec : Option[IOTypeSpec] = None
//  /* basic methods for building Scope Tree */
//  // S C O P E S
//  
//  def pushBlock : Unit = {
//    currentScope = Some(new LocalScope(fromParentScope))
//  }
//  def popBlock : Unit = {
//    println("  in Block : locals: " + currentScope.get)
//    currentScope = toParentScope
//  }
//  def pushInit : Unit = {
////    currentScope = Some(new InitFuncSymbol(fromParentScope))
//  }
//  def popInit : Unit = {
//    currentScope = toParentScope
//  }
//  def pushWork : Unit = {
////    currentScope = Some(new WorkFuncSymbol(fromParentScope))
//  }
//  def popWork : Unit = {
//    currentScope = toParentScope
//  }
//  def pushPreWork : Unit = {
////    currentScope = Some(new PreWorkFuncSymbol(fromParentScope))
//  }
//  def popPreWork : Unit = {
//    currentScope = toParentScope
//  } 
//  def enterStruct(id: ID) : Unit = {
//    //println("in enter struct, fromParentScope value is " + fromParentScope)
//    val ss = new StructSymbol(id.lexeme, fromParentScope)
//    currentScope.get.define(ss) // def struct in current scope
//    //idAST nodes points node of scope tree
//    id.symbol = Some(ss)
//    currentScope = Some(ss)
//  }
//  def exitStruct : Unit = {
//    currentScope = toParentScope
//  }
//  def fromParentScope : Option[Scope] = {
//    if(currentScope.get.scopeName.equals(Some("builtin")))
//      Some(symtab.statics)
//    else currentScope
//  }
//  def toParentScope : Option[Scope] = {
//    if(currentScope.get.enclosingScope.get.scopeName.equals(Some("static")))
//      currentScope.get.enclosingScope.get.enclosingScope //go up one more time to reach global Scope
//    else
//      currentScope.get.enclosingScope
//  }
//  def enterStream(id: ID, typespec: (MyType, MyType, MyType)) : Unit = { //typespec : typeof stream(filter, pipeline,...), input type, output type
////    val ss = new StreamSymbol(typespec, id.toString(), fromParentScope)
////    currentScope.get.define(ss)
////    //idAST points scope tree node
////    id.symbol = Some(ss)
////    currentScope = Some(ss)
//  }
//  def enterFunction(id : ID, t: MyType) : Unit = {
//   val fs = new FunctionSymbol(id.toString(), Some(t), fromParentScope)
//   
//  }
//  
//  //for work,prework, init function : they are only referenced, not declared.
//  
//  
//  
//  def enterStream(node : AST) : Unit = {
//    var typespec : Option[(MyType, MyType, MyType)] = None //type of stream, input type, output type
//    var tt : Option[MyType] = None
//    var iot : Option[IOTypeSpec] = None
//    var idd : Option[ID] = None
//    node match {
//      case FilterDecl(iott,_,id,parms,body) =>
//        idd = Some(id)
//        tt = Some(currentScope.get.resolve("filter").get.asInstanceOf[BuiltInTypeSymbol])
//        iot = Some(iott)
//      case PipelineDecl(iott,_,id,parms,body) =>
//        idd = Some(id)
//        tt = Some(currentScope.get.resolve("pipeline").get.asInstanceOf[BuiltInTypeSymbol])
//        iot = Some(iott)
//      case SplitJoinDecl(iott,_,id,parms,body) =>
//        idd = Some(id)
//        tt = Some(currentScope.get.resolve("splitjoin").get.asInstanceOf[BuiltInTypeSymbol])
//        iot = Some(iott)
//      case FeedbackLoopDecl(iott,_,id,parms,body) =>
//        idd = Some(id)
//        tt = Some(currentScope.get.resolve("feedback").get.asInstanceOf[BuiltInTypeSymbol])
//        iot = Some(iott)
////      case HelperFunctionDecl(t,id,parms,iorate,body) =>
////        idd = Some(id.toString())
////        tt = Some(currentScope.get.resolve("filter").get.asInstanceOf[BuiltInTypeSymbol])
////      case HandlerFunctionDecl(id,parms,body) =>
////        idd = Some(id.toString())
////        tt = Some(currentScope.get.resolve("filter").get.asInstanceOf[BuiltInTypeSymbol])
//      case _ => println("exception in enterStream")
//    }
//    val inT = currentScope.get.resolve(iot.get.InT.toString()).get.asInstanceOf[MyType] //strange code
//    val outT = currentScope.get.resolve(iot.get.OutT.toString()).get.asInstanceOf[MyType] //strange code
//    typespec = Some(tt.get, inT, outT)
//    //type must be declared in current or enclosing scope, id should not declared
//    if(currentScope.get.resolve(idd.get.toString()) == None && tt !=None) {
//      enterStream(idd.get, typespec.get)
//    }
//  }
//    
//  def exitStream : Unit = {
//    currentScope = toParentScope
//  }
//  // D e f i n e  s y m b o l s
//  
//  //Start: var
//  def varDeclaration(id: Option[ID], t: Option[MyType]) : Unit = {
//    val vs = new VariableSymbol(id.get.toString,t)
//    currentScope.get.define(vs)
//  }
//  def portalDeclaration(varname : String, portalid : String) {
//    val ps = new PortalSymbol(varname, portalid)
//    currentScope.get.define(ps)
//  }
//
//  def refID(node: ID) : Boolean = {
//    refID(node.toString())
//  }
//  def refID(id: String) : Boolean = {
//    return currentScope.get.resolve(id) != None
//  }
//  def refStructField(node: FieldExpr) : Boolean = {
//    val strID = currentScope.get.resolve(node.base.asInstanceOf[ID].toString())
//    if(strID != None)
//      return strID.get.asInstanceOf[StructSymbol].resolveMember(node.field.asInstanceOf[ID].toString()) != None
//    else return false
//  }
//     
//  def canDefined(id: ID, t: Type) : Boolean = {
//    currentScope.get.resolveCurrent(id.toString).isEmpty && 
//    currentScope.get.resolve(t.toString).isDefined
//  }
//  def canDefined(id: ID, t1 : Type, t2 : Type) :Boolean = {
//    currentScope.get.resolve(id.toString()).isEmpty &&
//    currentScope.get.resolve(t1.toString()).isDefined &&
//    currentScope.get.resolve(t2.toString()).isDefined
//  }
//  def canDefined(id: ID) : Boolean = {
//    currentScope.get.resolveCurrent(id.toString).isEmpty
//  }
//  def canDefined(node : FieldDecl) : Boolean = canDefined(node.id, node.T)
//  def canDefined(node : VarDecl) : Boolean = canDefined(node.id, node.T)
//  def canDefined(node : StreamDecl) : Boolean = 
//    canDefined(node.id,node.iotype.InT,node.iotype.OutT)
//  def canDefined(node : HelperFunctionDecl) : Boolean = 
//    canDefined(node.id,node.ReturnType)
//  def canDefined(node : HandlerFunctionDecl) : Boolean =
//    canDefined(node.id)
//  def canDefined(node : StructDecl) : Boolean = canDefined(node.id)
//  
//    
//  def defineVariableSymbol(id: ID, t : Type) : Unit = {
//    val mytype = getTypeSymbol(t)
//    val vs = new VariableSymbol(id.toString, Some(mytype))
//    currentScope.get.define(vs)
//  }
//  def defineVariableSymbol(node : FieldDecl) : Unit = defineVariableSymbol(node.id, node.T)
//  def defineVariableSymbol(node : VarDecl) : Unit = defineVariableSymbol(node.id, node.T)
//  def defineHelperSymbol(node : HelperFunctionDecl) : Unit = {
//    val mytype = getTypeSymbol(node.ReturnType)
//    val fs = new FunctionSymbol(node.id.toString(), Some(mytype), fromParentScope)
//    currentScope.get.define(fs)
//    currentScope = Some(fs)
//  }
//  
//  //function symbol with void type
//  def defineHandlerSymbol(node : HandlerFunctionDecl) = {
//    val fs = new FunctionSymbol(node.id.toString(), None, fromParentScope)
//    currentScope.get.define(fs)
//    currentScope = Some(fs)
//  }
//  
//  def defineStructSymbol(node : StructDecl) = {
//    val ss = new StructSymbol(node.id.toString(), fromParentScope)
//    currentScope.get.define(ss)
//    currentScope = Some(ss)
//    //println("in definesturctsymbol, current scope name = " + currentScope.get.scopeName.get)
//  }
//  
//    
//  def getTypeSymbol(node : Type) : MyType = {
//    if(node.isInstanceOf[ConstantArrayType]) {
//      val arraynode = node.asInstanceOf[ConstantArrayType]
//      println("array get type symbol test!")
//      println(arraynode.ElementType.toString())
//      currentScope.get.resolve(arraynode.ElementType.toString()).getOrElse(println("type resolution failure")).asInstanceOf[MyType]  
//    }
//    else currentScope.get.resolve(node.toString()).getOrElse(println(node + " type resolution failure")).asInstanceOf[MyType]
//  }
//  def getTypeSymbol(node : IOTypeSpec) : (MyType, MyType) = (getTypeSymbol(node.InT), getTypeSymbol(node.OutT))
//  
//  //defining Scoped Symbol changes currentScope pointer
//  def defineFilterSymbol(node : FilterDecl) = {
//    val typespec = getTypeSymbol(node.iotype)
//    val ss = new FilterSymbol(typespec, node.id.toString, fromParentScope)
//    ss.declAST = Some(node)
//    currentScope.get.define(ss)
//    currentScope = Some(ss)
//  }
//  def definePipelineSymbol(node : PipelineDecl) = {
//    val typespec = getTypeSymbol(node.iotype)
//    if(typespec.equals((SymbolTable._void, SymbolTable._void)))
//      topleveldecl = Some(node)
//    val ps = new PipelineSymbol(typespec, node.id.toString, fromParentScope)
//    ps.declAST = Some(node)
//    currentScope.get.define(ps)
//    currentScope = Some(ps)
//  }
//  def defineSplitJoinSymbol(node : SplitJoinDecl) = {
//    val typespec = getTypeSymbol(node.iotype)
//    val sj = new SplitJoinSymbol(typespec, node.id.toString, fromParentScope)
//    sj.declAST = Some(node)
//    currentScope.get.define(sj)
//    currentScope = Some(sj)
//  }
////  def defineFeedbackSymbol(node : FeedbackLoopDecl) = {
////    val typespec = getTypeSymbol(node.iotype)
////    val fs = new FeedbackLoopSymbol(typespec, node.id
////    currentScope = Some(fs)
////  }  
//  
//  def traverseInside(node : Any, visitfn : Any => Unit) { 
//    MyASTTraversal.visit(node, visitfn)
//  }
//  def setIDScope(id : ID) {
//    if(id.scope == None) {
//      println("(id) " + id.toString() + " scope has set")
//      id.scope = currentScope
//    }
//    else println("(id) " + id.toString() + " scope is already set")
//  }
//  
//  /**
//   * symbol def, make scope tree
//   * decorate type of PopExpr
//   */
//  def makeScopeTree(node : Any) {
//    node match {
//      /**
//       * symbol declaration
//       */
//      case d: Decl=> d match {
//        /**when 'ID' appears under Decl? -> ParmVarDecl, StreamDecl, StructDecl, 
//         *Native Decl, Native FuncsDecl, NativeFuncDecl, HelperFunctionDecl
//         *HandlerFunctionDecl, FieldDecl, VarDecl
//         */
//        
//        case p : ProgramDecl =>
//          traverseInside(p, makeScopeTree)
//        case d : GlobalDecl =>
//          currentScope = Some(symtab.statics)
//          traverseInside(node, makeScopeTree)
//          currentScope = Some(symtab.builtins)
//        case v: VarDecl => if(canDefined(v.id)) {
//          defineVariableSymbol(v)
//          traverseInside(v, makeScopeTree)
//        }
//        case field : FieldDecl => if(canDefined(field.id)) {
//          defineVariableSymbol(field)
//          traverseInside(field, makeScopeTree)
//        }
//        case ParmVarDecl(id,t) => if(canDefined(id)) {
//          defineVariableSymbol(id,t)
//          traverseInside(node, makeScopeTree)
//        }
//        case filter : FilterDecl => if(canDefined(filter.id)) {
//          filterTypeSpec = Some(filter.iotype)
//          defineFilterSymbol(filter)
//          traverseInside(filter, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case pipe : PipelineDecl => if(canDefined(pipe.id)) {
//          definePipelineSymbol(pipe)
//          traverseInside(pipe, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case sj : SplitJoinDecl => if(canDefined(sj.id)) {
//          defineSplitJoinSymbol(sj)
//          traverseInside(sj, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case fb : FeedbackLoopDecl => if(canDefined(fb.id)) {
//          //defineFeedbackSymbol(fb)
//          traverseInside(fb, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case f: HelperFunctionDecl => if(canDefined(f.id)) {
//          defineHelperSymbol(f)
//          traverseInside(f, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case h: HandlerFunctionDecl => if(canDefined(h.id)) {
//          defineHandlerSymbol(h)
//          traverseInside(h, makeScopeTree)
//          currentScope = toParentScope
//        }
//        case struct: StructDecl => if(canDefined(struct.id)){
//          defineStructSymbol(struct)  // symbol def, push block
//          //pushBlock
//          //println("in makescopetree sturctDecl, fielddecls = " + struct.fielddecls)
//          traverseInside(struct, makeScopeTree)
//          //popBlock
//          currentScope = toParentScope
//        }
//        case filterbody: FilterBodyDecl => {
//          pushBlock
//          traverseInside(filterbody, makeScopeTree)
//          popBlock
//        }
////      case StructDecl => // symbol def, push block is already done at strctDecl
////        println("this StructDecl should never be reached")
//      
//        case _ => traverseInside(node, makeScopeTree)
//      }
//      
//      case anonymousfilter : AnonymousFilterExpr => {
//          filterTypeSpec = Some(anonymousfilter.iotype)
//        }
//      
//      case fOr : ForStmt => {
//        pushBlock
//        traverseInside(fOr, makeScopeTree)
//        popBlock
//      }
//      /**
//       * DecoratePopExpr in makeScopeTree phase
//       * principle : PopExpr always shows up under filterDecl and filterDecl
//       * cannot be nested : each time traverser enter filterdecl, it is grabbing
//       * filterdecl's typespec for decorating popExpr
//       */
//      case pop: PopExpr => {
//          pop.evalType = Some(getTypeSymbol(filterTypeSpec.get.InT))
//          //println("PopExpr eval type : " + pop.evalType)
//        }
//      /**enter additional scope ("{" "}")
//       * for anonymous, name is not necessary because annoymous symbol 
//       * always ends at current scope which means scope resolving is not used
//       */
//      //case of block{ } : struct, helper decl, global decl, filter body, compoundstmt
//      case c : CompoundStmt => {
//        println("enter Compound Stmt")
//        pushBlock
//        traverseInside(c, makeScopeTree)
//        popBlock
//      }
//        
//      /**
//       * every ID are either defined or referred. 
//       * except stream symbol, every ID can be resolved in first traversal
//       * scope property is added in this stage. symbol property is added in
//       * second traversal  
//       */
//      case id: ID =>
//        /**decl id scope is already set
//         * only ref id should be matched here
//         */
//        setIDScope(id)
//      
//      case _ => {
//        //println("etc " + node)
//        traverseInside(node, makeScopeTree)
//      }
//    }
//    //symtab.printOut
//  }
//  
//  /**
//   * resolve symbol,decorate AST(declStmt, declRefStmt)
//   */
//  def decorateAST(node: Any) {
//    currentScope = Some(symtab.builtins) //reset currentScope
//    node match {
////      case d: Decl => d match {
////        case p: ProgramDecl => traverseInside(p, decorateAST)
////        case _=> //pass
////      }
//        /**
//         *streamconstruct id can be resolved in specific way in this stage :
//         * search both surrounding scope and enclosing scope
//         */
////      case scexpr : StreamConstructExpr =>
////        val id = scexpr.id
////        println("decorate AST : " + id.toString())
////        id.symbol = id.scope.get.resolve(id.toString())
////        if(id.symbol==None) println("scope resolution failure : " + id.toString())
////        else println("scope resolution success : " + id.toString())
////        id.symbol.get.asInstanceOf[StreamSymbol].declAST =
////        traverseInside(node, decorateAST)
//      case id: ID =>//resolve ID
//        println("decorate AST : " + id.toString())
//        if(id.symbol==None)
//          id.symbol = id.scope.get.resolve(id.toString())
//        if(id.symbol==None) println("scope resolution failure : " + id.toString())
//        else println("scope resolution success : " + id.toString())
//      case _ => traverseInside(node, decorateAST)
//    }
//  }
//  
//  def setEvalType(expr: Expr) : Unit = {
//    if(expr.evalType.isEmpty) {
//      expr.evalType = symtab.evalType(expr)
//      println(expr + ".evalType = " + expr.evalType)
//    }
//    else println(expr.toString() + " .evalType is already set")
//  }
//  
//  /**type promotion occurs in 3 cases
//   * 1. lhs, rhs in arithmatic expression evaluation
//   * 2. arguments in function call
//   * 3. array index in array referencing
//   */
//  
//  //for func parm promotion : simular to arithmaticResultType 
//  def setPromoteToType(oritype: Option[MyType], inputexpr : Expr) : Unit = {
//    val right_index = oritype.get.getTypeIndex; // type index of right operand
//    val left_index = inputexpr.evalType.get.getTypeIndex // type index of left operand
//    inputexpr.promoteToType = Some(symtab.promoteFromTo(left_index)(right_index))
//    println("input expr promoteTotype = " + inputexpr.promoteToType)
//  }
////  def setPromoteToType(uop:UnaryOperator) :Unit = {
////    
////  }
////  def 
//  def setPromoteToType(bop:BinaryOperator) : Unit = 
//    bop.opc match {
//        case Opcode.BO_Mul | Opcode.BO_Div | Opcode.BO_Rem | Opcode.BO_Add | 
//        Opcode.BO_Sub | Opcode.BO_Shl |Opcode.BO_Shr | Opcode.BO_Assign  => 
//          setPromoteToType(symtab.arithmeticResultType, bop.lhs, bop.rhs)
//          
//        case Opcode.BO_EQ | Opcode.BO_NE | Opcode.BO_LogicAnd | Opcode.BO_LogicOr |
//          Opcode.BO_LT | Opcode.BO_GT | Opcode.BO_LE | Opcode.BO_GE => 
////          if(bop.lhs.evalType!=Some(symtab._boolean))
////            bop.lhs.promoteToType = Some(symtab._boolean)
////          if(bop.rhs.evalType!=Some(symtab._boolean))
////            bop.rhs.promoteToType = Some(symtab._boolean)
//            setPromoteToType(symtab.relationalResultType, bop.lhs, bop.rhs)
//        
//        case Opcode.BO_BitAnd =>
//        case Opcode.BO_BitOr =>
//        case Opcode.BO_BitXor =>
//        
//        case Opcode.BO_MulAssign => 
//        case Opcode.BO_DivAssign =>
//        case Opcode.BO_RemAssign =>
//        case Opcode.BO_AddAssign =>
//        case Opcode.BO_SubAssign =>
//        case Opcode.BO_ShlAssign =>
//        case Opcode.BO_ShrAssign =>
//        case Opcode.BO_AndAssign =>
//        case Opcode.BO_OrAssign =>
//        case Opcode.BO_XorAssign =>
//          }
//  
//  /**
//   * setPromoteToType sets promoteToType value of lhs and rhs
//   */
//  def setPromoteToType(typeTable : Array[Array[MyType]], lhs: Expr, rhs : Expr) : MyType = { 
//    println("in setPromoteToType, lhs = " + lhs + " lhs.evalType = " + lhs.evalType)
//    val lhs_index = lhs.evalType.get.getTypeIndex; // type index of left operand
//    val rhs_index = rhs.evalType.get.getTypeIndex // type index of right operand
//    val result = typeTable(lhs_index)(rhs_index)
//    if ( result==symtab._void ) {
//      println("incompatible type")
//    }
//    else {
//      lhs.promoteToType = Some(symtab.promoteFromTo(lhs_index)(rhs_index))
//      rhs.promoteToType = Some(symtab.promoteFromTo(rhs_index)(lhs_index))
//    }
//    result
//  }
//  /**
//   * type promotion, also does type evaluation.
//   * decorate promoteToType attribute to the AST
//   * promoteToType = None means it is not decided yet
//   * promoteToType = void means it doesn't need type promotion
//   */
//def typePromotion(node : Any) { 
//    node match {
//      case expr: Expr => 
//        /**cases are all the possible expressions.
//         * only expressions that have type attribute is evaluated.
//         * Others are blinded by comment.
//         */
//        //println("expr check " + expr.toString())
//        expr match {
//        //case no : NoExpr => //pass
//        //case pop : PopExpr => //pass
//        //case star : StarExpr => //pass, but think again
//        //case peek : PeekExpr => //pass
//        //case range : RangeExpr => //pass
//          case call : CallExpr => {
//          //function args promotion.
//          //function return type evaluation
//            setEval(call.id)
//            call.Parms.foreach(f=> typePromotion(f))
//            setPromoteThenEval(call)
//          }
//          //case initlist : InitListExpr => //??
//          //case construct : ConstructExpr => //pass
//          //case portalspec : PortalSpecExpr => //pass?
//        
//          case id : ID => setEval(id)
//        
//        
//          case sl : StringLiteral =>
//            println("streamit doesn't support char type")
//            setEval(sl)
//          case intl : IntLiteral => setEval(intl)
//          case compl : ComplexLiteral => setEval(compl)
//          case fl : FloatLiteral => setEval(fl)
//          case bl : BooleanLiteral => setEval(bl)
//          case pi : PiLiteral => setEval(pi)
//          
//          
//          case t : TernaryOperator => //?
//          case b : BinaryOperator => 
//            /**lhs, rhs are promoted, bop is evaluated*/
////            setEvalType(b.lhs)
////            setEvalType(b.rhs)
//            typePromotion(b.lhs) //recursively evaluates lhs
//            typePromotion(b.rhs) //recursively evaluates rhs
////            println("in typepromotion bop, b.lhs.evaltype = " + b.lhs.evalType
////                + ", b.rhs.evaltype = " + b.rhs.evalType )
//            setPromoteThenEval(b)
//          
//          case u : UnaryOperator => 
//            typePromotion(u.input)
//            setPromoteThenEval(u)
//          case paren : ParenExpr =>
//            typePromotion(paren.input)
//            paren.evalType = paren.input.evalType
//            
//          case ase : ArraySubscriptExpr =>
//            typePromotion(ase.base)
//            typePromotion(ase.idx)
//            setPromoteThenEval(ase)
//          case fe : FieldExpr => 
//            typePromotion(fe.base)
////            val struct = fe.base.evalType.get.asInstanceOf[StructSymbol]
////            struct.resolveMember(fe.field.toString()).get.tYpe
//            setEval(fe)
//            
//          case cast: CastExpr =>
//            typePromotion(cast.casts)
//            typePromotion(cast.input)
//            setPromoteThenEval(cast)
//          //case itc : ImplicitTypeCastExpr =>
//              
//           /**
//           * evaltype of PopExpr is already solved in makeScopeTree, 
//           * it doesn't need to happen twice.
//           */
//          //case pop: PopExpr => setEvalType(pop)
//          case _ => traverseInside(node, typePromotion)
//        }
//      case _=>
//        traverseInside(node, typePromotion)
//    }
//}
//  /**
//   * setPromote chagnes input, lhs, or rhs of target(argument) expression
//   */
//  /**
//   * for these function to work, we assume evaltype of (inside of target expression) is already set
//   */
//  def setPromoteThenEval(expr : BinaryOperator) = {
//    if(expr.lhs.promoteToType.isDefined || expr.rhs.promoteToType.isDefined)
//      println("exception: either "+ expr.lhs.toString() + " or " + expr.rhs.toString + 
//        "promoteToType is already defined")
//    else {
//      var result : Option[MyType] = None
//      expr.opc match {
//        /*
//         *  *,/,%,+,-,<<,>>,=
//         */
//        case Opcode.BO_Mul | Opcode.BO_Div | Opcode.BO_Rem | Opcode.BO_Add | Opcode.BO_Sub | 
//          Opcode.BO_Shl |Opcode.BO_Shr | Opcode.BO_Assign | Opcode.BO_MulAssign | Opcode.BO_DivAssign | 
//          Opcode.BO_RemAssign | Opcode.BO_AddAssign | Opcode.BO_SubAssign | Opcode.BO_ShlAssign |
//          Opcode.BO_ShrAssign =>
//          result = Some(setPromoteToType(symtab.arithmeticResultType, expr.lhs, expr.rhs))
//            
//        /*
//         * ==,!=,&&,||,<,>,<=,>=
//         */
//        case Opcode.BO_LogicAnd | Opcode.BO_LogicOr |
//            Opcode.BO_LT | Opcode.BO_GT | Opcode.BO_LE | Opcode.BO_GE => 
//          result = Some(setPromoteToType(symtab.relationalResultType, expr.lhs, expr.rhs))
//        /*
//         * ==, !=
//         */
//        case Opcode.BO_EQ | Opcode.BO_NE =>
//          result = Some(setPromoteToType(symtab.equalityResultType, expr.lhs, expr.rhs))
//          
//        case Opcode.BO_BitAnd =>
//          println(expr.opc + "hasn't matched")
//        case Opcode.BO_BitOr => println(expr.opc + "hasn't matched")
//        case Opcode.BO_BitXor => println(expr.opc + "hasn't matched")
//        
//        case Opcode.BO_AndAssign => println(expr.opc + "hasn't matched")
//        case Opcode.BO_OrAssign => println(expr.opc + "hasn't matched")
//        case Opcode.BO_XorAssign => println(expr.opc + "hasn't matched")
//      }
//      expr.evalType = result
//    }
//  }
//  
//  def setPromoteThenEval(expr : UnaryOperator) = {
//    /*
//     * "++" "--" "++" "--" "+" "-" "~" 
//     */
//    expr.opc match {
//      case Opcode.UO_PreInc | Opcode.UO_PreDec | Opcode.UO_PostInc | Opcode.UO_PostDec |
//        Opcode.UO_Plus | Opcode.UO_Minus | Opcode.UO_BitNot =>
//        expr.input.promoteToType = Some(symtab._void)
//        expr.evalType = expr.input.evalType
//        
//      //"!"
//      case Opcode.UO_LogicNot => 
//        expr.input.promoteToType = Some(symtab._boolean)
//        expr.evalType = Some(symtab._boolean)
//    }
//  }
//  
//  def setPromoteThenEval(expr : ArraySubscriptExpr) = {
//    val index = expr.idx
//    index.evalType.get match {
//      case symtab._int => index.promoteToType = Some(symtab._void)
//      case symtab._float => index.promoteToType = Some(symtab._int)
//      case _ => println("type promotion error: incompatible type!")
//    }
//    expr.evalType = expr.base.evalType 
//  }
//  
//  //this may be wrong, need to be tested more
//  def setPromoteThenEval(expr : CallExpr) = {
//    expr.id.symbol.get.asInstanceOf[FunctionSymbol].getMembers.values.foreach {
//      f=> expr.Parms.foreach{
//        g=> {
//          println("setPromoteThenEval test: f.type = " + f.tYpe + " g = " + g)
//          setPromoteToType(f.tYpe,g)
//        }
//      }
//    }
//    expr.evalType = expr.id.evalType
//  }
//  
//  def setPromoteThenEval(expr : CastExpr) = {
//    //expr.input.promoteToType = expr.casts.map(f=>)
//  }
//
//  /**
//   * eval function set evalType of expr
//   */
//  
//  /**
//   * streamIt doesn't support stringliteral evalutation but 
//   * only support as input of print function
//   */
//  def setEval(expr : FieldExpr) = {
//    //this can't solve a.b.c(nested field expr), this must be fixed
//    //val struct = expr.base.symbol.get.tYpe
//    val struct = expr.base.evalType
//    expr.evalType = 
//      struct.get.asInstanceOf[StructSymbol].resolveMember(expr.field.toString()).get.tYpe
//  }
//  
//  def setEval(expr : ID) = {
//    expr.evalType = expr.symbol.get.tYpe
//  }
//  
//  def setEval(expr : StringLiteral) = {
//    expr.evalType = Some(SymbolTable._void)
//  }
//  
//  def setEval(expr : IntLiteral) = {
//    expr.evalType = Some(SymbolTable._int)
//  }
//  def setEval(expr : ComplexLiteral) = {
//    expr.evalType = Some(SymbolTable._complex)
//  }
//  
//  def setEval(expr : FloatLiteral) = {
//    expr.evalType = Some(SymbolTable._float)
//  }
//  
//  def setEval(expr : BooleanLiteral) = {
//    expr.evalType = Some(SymbolTable._boolean)
//  }
//  
//  def setEval(expr : PiLiteral) = {
//    expr.evalType = Some(SymbolTable._float)
//  }
//}