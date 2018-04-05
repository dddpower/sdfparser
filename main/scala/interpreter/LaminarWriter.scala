package interpreter
import ast._
import common.Counter
import graph.GraphSpace._
import graph._
import scala.reflect.ClassTag
import ast.MyASTTraversal._
import ScalaWriter._    
import common.WritingTool._

object LaminarWriter {
  var mainPipeName:String = _
  var graph :PipeLine = null
  var prog : ProgramDecl = null
  
  def genSDF: String = preamble + newLine + sdf + mainPipeName + openBlock +
    graph.body.edges.iterator.map{f=>
      var incoming = f._1.value.name
      println("incoming = " + incoming)
      var outgoing = f._2.value.name
      incoming = incoming takeRight 5 match {
        case "split"=> incoming + "(1)"
        case "_join" => incoming + "(2)"
        case _=> incoming
      }
      outgoing = outgoing takeRight 5 match {
        case "split"=> outgoing + "(1)"
        case "_join" => outgoing + "(2)"
        case _=> outgoing
      }
    
    {
      if(f.label=="") {
        if(f._1.value.outT=="")
          f._2.value.inT
        else f._1.value.outT
      }
      else f.label
    } + " " + incoming + "->" + 
    outgoing}.mkString(endStmt) + endStmt + 
    graph.body.nodes.iterator.map{f=>
    val dipre : Set[String] = f.diPredecessors.map(_.value.name)
    val disuc : Set[String] = f.diSuccessors.map(_.value.name)
    f.value.toSDF(dipre,disuc)}.mkString(newLine) +
    closeBlock + newLine + postamble
    
  def postamble : String = {
    "postamble " + openBlock +
    "int main(void) " + openBlock +
    mainPipeName + "()" + endStmt +
    "return 0;" + closeBlock +
    closeBlock
  }
  //  def genLaminar : String = {
//    sdf + mainPipeName + openBlock +
//    genGraph + genFilterDecl +
//    closeBlock
//  }
  def triggerGenSDF = graph.body.nodes.iterator
  //I don't like side effect but can't find other way for now
  def triggerMapping = {
    graph.body.nodes.iterator.foreach(f=>mapFilterDecl(prog,f.value))

    //checking
    println("mapping result")
    graph.body.nodes.iterator.foreach(f=>println(f.value.decl))
  }
    
  
  //side effect with Boolean
  def mapFilterDecl(astnode:Any, graphnode:FilterNode):Unit = {
    //traverse ast until find filterDecl and map it if name is same as graph return true,
    //when traversal reach to the end, return false
    astnode match {
      case afil: ConstructAnonFilterExpr=>
        println("anonymous")
        if(afil.id.toString()==graphnode.name)
          graphnode.decl = Some(afil)
      case fil:FilterDecl=> 
        println("in filterDecl")
        if(fil.id.toString==graphnode.name.dropRight(2)) {
          graphnode.decl = Some(fil)
          println("connected")
        }
      case ast:AST=> ast.extractList.foreach(f=>mapFilterDecl(f,graphnode))
      case list:List[Any]=> list.foreach(f=>mapFilterDecl(f,graphnode))
      case _=>
    }
  }
  def preamble : String = {
    val include = "#include "
    "preamble " + openBlock + include + "<stdio.h>" + newLine +
    include + "<stdlib.h>" + newLine + include + "<math.h>" + closeBlock
  }
  def simpleEval(node: Expr) : Int = node match {
    case intl: IntLiteral => intl.value
    case _=> 
      println(unimplemented)
      0
  }

//  def genGraph : String = {
//    val graphget = graph
//    graphget.body.edges.iterator.map(f=>
//      f._1.value._1._2 + " " + f._1.value._2 + "->" + 
//      f._2.value._2).mkString(endStmt) + endStmt
//  }
//  def testgraphnode = {
//    graph.body.nodes.iterator.foreach{ f=>
//      println(f.value._2 + " predecssor : " + f.diPredecessors)
//      println(f.value._2 + " successor : " + f.diSuccessors)
//    }
//  }
//  

  def genSDF(node: DeclStmt) : String = {
    println("I entered DeclStmt")
    genSDF(node.Decls)
  }
  def genSDF(node: ParmVarDeclList) : String = {
    node.parmvardecls.map(genSDF).mkString(", ")
  }
  
  def genSDF(node: VarDeclList) : String = {
    println("vardecllist")
    node.vardecls.map(vardecl=>genSDF(vardecl)).mkString(endStmt) + endStmt
  }
  
  def genSDF(node:VarDecl) : String = {
    val vardecl = node.T.toString() + " " + node.id
    val init = 
      if(node.init==NoExpr())""
      else genSDF(node.init) 
    vardecl + init  
  }
  
  /**similar as VarDecl*/
  def genSDF(node:ParmVarDecl) : String = {
    node.T.toString + " " + node.id.toString()  
  }
  
  
//  def genSDF(node: FilterDecl,current : String, incoming: List[String], outgoing: List[String]) : String = {
//    val filterbody = node.body
//    actor + node.id + openBlock + genSDF(filterbody,current, incoming,outgoing) + closeBlock
//  }
  def genSDF(node: Stmt) : String = {
    println("Stmt")
    node match {
      case cstmt:CompoundStmt=> openBlock + 
        cstmt.stmts.map(stmt=>genSDF(stmt)).mkString(endStmt) + closeBlock
      
      case pushstmt: PushStmt => 
        push + lparen + genSDF(pushstmt.Value) + rparen
      case expr: Expr => expr match {
        case popexpr : PopExpr => pop
      
        case be: BinaryExpr=> genSDF(be.lhs) + 
          " " + be.opc.toString + " " + genSDF(be.rhs)
        case ue: UnaryExpr=> 
          if(ue.opc==Opcode.UO_PostDec || ue.opc==Opcode.UO_PostInc) {
            genSDF(ue.input) + ue.opc.toString()
          }
          else {
            println("rerere")
            ue.opc + genSDF(ue.input)
          }
        case id:ID => id.toString()
        case intlit: IntLiteral=>intlit.lexeme
        case floatlit: FloatLiteral=>floatlit.lexeme
        case _=> println(unimplemented)
          unimplemented 
      }
      //case push:PushStmt=>"push(" + push.Value + )
      //case pop:PopStmt=>
      case _=> println(unimplemented)
        unimplemented
    }
  }
  def genSDF(node:ProgramDecl) : String = {
//    val flatgraph = graph.get
//    val filterlist = findASTNode[FilterDecl](node)
//    flatgraph.genStr
//    flatgraph.body.edges.iterator.map(f=>
//        f._1.value._1._2 + " " + f._1.value._2 + "->" + 
//        f._2.value._2 + ";").mkString("\n")
//    filterlist.map(genActorString).mkString("\n")
//    ""
    genSDF(node.decls)
  }
  def genSDF(node: DeclList) : String = 
    node.decls.map(decl=>genStr(decl)).mkString(newLine)
  
  def genSDF(node: PipelineDecl) : String = {
    if(isMain(node.iotype))
      mainPipeName = node.id.toString
    "def " + node.id.toString + genStr(node.parmvardecls) + " : PipeLine" + " = " + newLine +
    openBlock + "val g = new PipeLine(" + genStr(node.iotype)+ ")" + newLine + newLine +
    genStr(node.body) + newLine + "g" + closeBlock
  }
  def genActorString(node: FilterDecl) : String = {
    class AstList(input:List[Decl]) {
      def alter_instanceOf[T: ClassTag](target: Any) : Boolean = {
        target match{
          case _:T =>true
          case _=>false
        }
      }
      def find_get_asInstanceOf[T:ClassTag] = 
        input.find(p=>alter_instanceOf[T](p)).asInstanceOf[Option[T]]
    }
    implicit def ToAstList(input:List[Decl]) = new AstList(input)
    
    val vardecllist = findASTNode[VarDecl](node)
    
    //ignore init, this should be fixed
    def vardeclString(node:VarDecl) : String = {
      node.T.toString + " " + node.id.toString + " = " +
      0 + ";"+ newLine
    }
//    def stateString : String = {
//      state + openBlock + 
//      vardecllist.map(vardeclString).mkString(";\n")+ closeBlock
//    }
    
    
    
    //find vardecls from body
    val decllist = node.body.decls.decls
    
    val initfunc = decllist.find_get_asInstanceOf[InitFunctionDecl].get
    val workfunc = decllist.find_get_asInstanceOf[WorkFunctionDecl].get
    val ratedecls = workfunc.IO_rates.Rates.decls
    val pushdecl = ratedecls.find_get_asInstanceOf[PushDecl]
    val popdecl = ratedecls.find_get_asInstanceOf[PopDecl]
    val peekdecl = ratedecls.find_get_asInstanceOf[PeekDecl]
    
    def initString : String = {
      init + openBlock + initfunc.Body.stmts.map(genStr).mkString(";")
    }
    //temporal func for push, pop, peek decl
    def evalRate(node:Option[Decl]) : Int = 
      if(node.isDefined) 
        node.get match {
          case pu:PushDecl=> pu.PushRate.asInstanceOf[IntLiteral].lexeme.toInt 
          case po:PopDecl=> po.PopRate.asInstanceOf[IntLiteral].lexeme.toInt
          case pe:PeekDecl=> pe.PeekRate.asInstanceOf[IntLiteral].lexeme.toInt
      }
      else 0
      
    
    //TBD, for now, we assume each rate as static int
    val pushrate = evalRate(pushdecl)
    val poprate = evalRate(popdecl)
    val peekrate = evalRate(peekdecl)
    
    "pushrate: " + pushrate + " poprate: " + poprate + " peekrate: " + peekrate
  }
  def genStr(node: WhileStmt) : String =
    "while(" + genStr(node.Condition) + ")" + genStr(node.Body)
  
  def genStr(node : ForStmt) : String = {
    println("forstmt enter check")
    genStr(node.Init) + newLine +
    "while(" + genStr(node.Condition) + ")" + openBlock +
    genStr(node.Body) + newLine + genStr(node.Incr) + closeBlock
  }
  def genStr(node:VarDecl) : String = {
    val vardecl = "var " + node.id.toString + ":" + genStr(node.T) + " = "
    val init : String = if(node.init==NoExpr()) {
      node.T match {
        case arr: ConstantArrayType =>
           {
            val dim = new scala.collection.mutable.ListBuffer[String]()
            val addstr = "Array ofDim[" + genStr(arr.ElementType) + "]("
            dim += genStr(arr.size)
            var t = arr.ElementType
            while(t.isInstanceOf[ConstantArrayType]) {
              dim += genStr(arr.size)
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
    else genStr(node.init)
    
    vardecl + init
  }
  def genStr(node: Type) : String = node match {
    case arr : ConstantArrayType => "Array[" + genStr(arr.ElementType) + "]"
    case float : FloatType => "Double"
    case un : UnresolvedType => un.toString()
    case _ => 
      val str = node.toString()
      str.head.toUpper + str.tail
  }
  
  def genStr(node: IfStmt) : String = {
    val ifstmt = "if(" + genStr(node.Condition) + ")" + genStr(node.ifBody)
    val elsestmt : String = 
      if(node.elseBody!=NoStmt())
        "else" + genStr(node.elseBody)
      else ""
    ifstmt + elsestmt
  }
  
  def genStr(node: ReturnStmt) : String = genStr(node.Value)
  
  def genStr(node : DoWhileStmt) : String =
    "do" + genStr(node.Body) + "while(" + genStr(node.Condition) + ")" + newLine
  
  def genStr(body: CompoundStmt) : String = openBlock + 
    body.stmts.map(stmt=>genStr(stmt)).mkString(newLine) + closeBlock
  
    
  def isMain(node: IOTypeSpec) : Boolean = 
    node.InT.toString=="void" && node.OutT.toString()=="void"
  
  def genStr(node: ConstructAnonFilterExpr) : String = "g.add(" + openBlock +
    "val g = new Filter("+ genStr(node.iotype) + ")" + genStr(node.body) + newLine + "g" + closeBlock + ")"
  
  def genStr(node: AnonymousPipelineExpr) : String = "g.add(" + openBlock +
    "val g = new PipeLine(" + genStr(node.iotype) + ")" + newLine + newLine + genStr(node.body) + newLine + "g" + closeBlock + ")"
  
  def genStr(node: AnonymousSplitJoinExpr) : String = "g.add(" + openBlock +
    "val g = new SplitJoin("+ genStr(node.iotype) + ")" + 
    newLine + newLine + genStr(node.body) + newLine + "g" + closeBlock + ")"
  
  //same as pipelinedecl
  def genStr(node: SplitJoinDecl) : String = {
    "def " + node.id.toString + genStr(node.parmvardecls) + " : SplitJoin" + " = " + newLine +
    openBlock + "val g = new SplitJoin(" + genStr(node.iotype) + ")" + 
    newLine + newLine + genStr(node.body) + newLine + "g" + closeBlock
  }
  def genStr(node: ProgramDecl) : String = {
    genStr(node.decls)
  }

  def genStr(node: DeclList) : String = node.decls.map(decl=>genStr(decl)).mkString(newLine)
  
  def genStr(node : ExprList) : String = "(" + node.exprs.map(expr=>genStr(expr)).mkString(", ") + ")"
  
  
  def genStr(node:ParmVarDecl) : String =
    node.id.toString() + ":" + genStr(node.T)
  def genStr(node: ParmVarDeclList) : String = "(" + 
  node.parmvardecls.map(decl=>genStr(decl)).mkString(", ") + ")"
  
  
  def genStr(node: GlobalDecl) : String = {
    genStr(node.decls) + newLine + genStr(node.initDecl)
  }
  
  def genStr(node: InitFunctionDecl) : String = genStr(node.Body)
  
  def genStr(node: SplitStmt) : String = ""
  
  def genStr(node: JoinStmt) : String = ""
  
  def genStr(iot:IOTypeSpec) : String = 
    "(" + "\"" +iot.InT.toString + "\"" + ", " + "\"" + iot.OutT.toString + "\"" + ")"
  
  def genStr(node: FilterDecl) : String = {
    "def " + node.id.toString + genStr(node.parmvardecls) + 
    " = new Filter(" + genStr(node.iotype) + "," + 
    "\"" + node.id.toString +"\"" + "," + 
    //"None" +
    "Some(" + node.toString +")" +
    ")"
  }
    
  def genStr(node: HelperFunctionDecl) : String = {
    "def " + node.id.toString + genStr(node.parmvardecls) +
    " : " + node.ReturnType.toString + " = " + genStr(node.Body)
  }
    
  
  def genStr(node: StreamConstructExpr) : String = {
    "g.add(" + node.id.toString + genStr(node.Parms) + ")"
  }
    
  
  def genStr(node: UnaryExpr) : String = {
    val uop = node.opc.toString
    if(uop=="++")
      genStr(node.input) + " = " + genStr(node.input) + " + 1"
    else if(uop=="--")
      genStr(node.input) + " = " + genStr(node.input) + " - 1" 
    else uop + genStr(node.input)
  }
  
  
  def genStr(node: BinaryExpr) : String = genStr(node.lhs) + 
  " " + node.opc.toString + " " + genStr(node.rhs)
  def genStr(node: ParenExpr) : String = "(" + genStr(node.input) + ")"
  
  def genStr(node: CallExpr) : String = { 
    node.id.toString + lparen + genStr(node.Parms) + rparen
  }
  def genStr(node: ArraySubscriptExpr) : String = genStr(node.base) + "(" + genStr(node.idx) + ")"
  def genStr(node: FieldExpr) : String = genStr(node.base) + "."+ genStr(node.field)
  
  def genStr(node: InitArrayExpr) : String = {
    "List" + genStr(node.array)
  }
  def genStr(node : DeclStmt) : String = 
    node.Decls.decls.map(decl=>genStr(decl)).mkString(", ")

  def genStr(node: StructDecl) : String = {
    "class " + node.id.toString + newLine + openBlock +
     genStr(node.fielddecls) + closeBlock
  }
  def genStr(fd : FieldDecl) : String = {
    val vardecl = "var " + fd.id.toString + ":" + genStr(fd.T) + " = "
    val init : String = {
      fd.T match {
        case arr: ConstantArrayType =>
           {
            val dim = new scala.collection.mutable.ListBuffer[String]()
            val addstr = "Array ofDim[" + genStr(arr.ElementType) + "]("
            dim += genStr(arr.size)
            var t = arr.ElementType
            while(t.isInstanceOf[ConstantArrayType]) {
              dim += genStr(arr.size)
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
  def genStr(node : AST) : String = node match {
    case decl : Decl ⇒ decl match {
      case vd : VarDecl ⇒ 
        genStr(vd)
      case nd: NoDecl => ""
      
      case progd: ProgramDecl=> genStr(progd)
      
      // Parameter declaration:
      case pv:ParmVarDecl=> genStr(pv)
      
      case fd:FilterDecl=> genStr(fd)
      case pd:PipelineDecl=> genStr(pd)
      
      case sj:SplitJoinDecl=> genStr(sj)
      case fl:FeedbackLoopDecl=>""
      case sd:StructDecl=>genStr(sd)
      case bfd:BuiltInFunctionDecl=> ""
      case bsd:BuiltInStreamDecl=> ""
      case gd:GlobalDecl=>genStr(gd)
      case ifd:InitFunctionDecl=> genStr(ifd)
      case d:PreWorkFunctionDecl=> ""
      case wfd:WorkFunctionDecl=> ""
      
      case hfd:HelperFunctionDecl=> genStr(hfd)
      
      case handlerd:HandlerFunctionDecl=> handlerd.toString
      
      case pushdecl:PushDecl=> pushdecl.toString
      
      case popdecl:PopDecl=> popdecl.toString
      
      case peecdecl:PeekDecl=> peecdecl.toString
      
      
      case fielddecl:FieldDecl=> genStr(fielddecl)
      case vdl:VarDeclList=> genStr(vdl)
    }
    case dr:DataRates=> dr.toString
      
    case stmt : Stmt ⇒ stmt match {
      case NoStmt() => "" 
      case ds : DeclStmt => genStr(ds)
        
      case ct : CompoundStmt ⇒ genStr(ct)
      case push : PushStmt ⇒  push.toString
      case splitstmt : SplitStmt ⇒  genStr(splitstmt)
      case joinStmt : JoinStmt⇒  genStr(joinStmt)
      case enqueuestmt : EnqueueStmt⇒ enqueuestmt.toString
      case forstmt : ForStmt⇒ genStr(forstmt)
        
      case ifstmt : IfStmt ⇒ 
        genStr(ifstmt)
        
      case ws : WhileStmt⇒ 
        genStr(ws)
        
      case retstmt: ReturnStmt⇒ 
        genStr(retstmt)
        
      case dowhilestmt: DoWhileStmt⇒
        genStr(dowhilestmt)
        
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
        case call: CallExpr⇒ genStr(call)
        case initarray:InitArrayExpr⇒ genStr(initarray)
        case construct: ConstructExpr⇒ construct match { 
          case as : AnonymousStreamExpr ⇒ genStr(as)
          case af: ConstructAnonFilterExpr ⇒ genStr(af)
          case ap : AnonymousPipelineExpr ⇒ genStr(ap)
          case asj: AnonymousSplitJoinExpr⇒ genStr(asj)
          case afb: AnonymousFeedbackLoopExpr⇒ genStr(afb.body)
          case streamconstruct : StreamConstructExpr⇒  genStr(streamconstruct)
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
          genStr(bop)            
        case uop: UnaryExpr⇒ 
          genStr(uop)
        case paren: ParenExpr⇒ 
          genStr(paren)
        case cast: CastExpr⇒ cast.toString
        case array: ArraySubscriptExpr⇒ genStr(array)
        case field: FieldExpr⇒ genStr(field)
        case dre:DeclRefExpr=> dre.toString
      }
      case _⇒ "exception"
    }
    case iot: IOTypeSpec=> genStr(iot)
    case sgs: ScatherGatherSpec=> sgs.toString
    case ty: Type => ty.toString
  }
}