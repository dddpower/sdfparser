package ast
import common.ErrorReporter._

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition
import java.io._
import scala.io.Source
import scala.reflect.ClassTag
object MyASTTraversal {
  def printList : Boolean = true
 
  def printVarDecl(v : Type) {
    //TBD
  }
  
    //case xs @ (_::_) => xs flatMap {immediateASTNodes(_)}
  

  def flatten(l: List[Any]): List[Any] = l flatMap {
    case ls: List[_] => flatten(ls)
    case h => List(h)
  }
  
  def findASTNode[T:ClassTag](node:Any) : List[T] = {
    def flattenFilter[T](node:Any,boolfunc: Any=>Boolean) : List[T]= {
      val tmp = node match {
        case list:List[Any] =>flatten(list)
        case _=> node
      }
      val result = tmp match {
        case list:List[Any]=>list.filter(boolfunc)
        case _=> List(tmp)
      }
      result.asInstanceOf[List[T]]
    }
    val result =
      node match {
      case target: T=> target
      case other: AST =>
        val result = other.extractList
        if(result!=Nil)
          result.filter(p=>p!=Nil).map(findASTNode[T])
      case _=>
      }
    flattenFilter(result,e=> e match{
      case _:T=>true
      case _=> false
      }
    )//returns List
  }
  
  def Traverse[T:ClassTag](node: Any) : List[Any] = {
    node match {
      case ast:AST=> 
        //println(ast.toString())
        ast.extractList.map(Traverse)
      case list: List[Any] => list.map(Traverse)
      case _=> 
        println("unexpected result")
        Nil
    }
  }
  

  //  def extractMap[T](node: AST,  visitfn: AST=> List[T] ): List[T] = 
//    node match {
//    //Decl(), Scathergather,Stmt(),Type()
//    case leaf: LeafNode => 
//      println("leaf shouldn't reach!")
//      Nil
//    case DeclList(list) => list.flatMap(visitfn)
//    case ProgramDecl(decls) => 
//      visitfn(decls) 
//    case IOTypeSpec(intype, outtype) =>
//      visitfn(intype):::visitfn(outtype)
//    case FilterDecl(iotype, stateful : Boolean, id, parmvardecl, body) =>
//      visitfn(iotype):::
//      visitfn(id):::
//      visitfn(parmvardecl):::
//      visitfn(body)
//    case FilterBodyDecl(decls) =>
//      visitfn(decls)
//    case PreWorkFunctionDecl(iorates, body) =>
//      visitfn(iorates):::
//      visitfn(body)
//    case WorkFunctionDecl(iorates, body) =>
//      visitfn(iorates):::
//      visitfn(body)
//    //case NoDecl() =>
//     
//    case StreamConstructExpr(id, typeparm, parms, portalspec) =>
//      visitfn(id):::
//      visitfn(typeparm):::
//      visitfn(parms):::
//      visitfn(portalspec)
//    case AnonymousFilterExpr(iotype, stateful : Boolean, body, portalspec) => //stateful is Boolean
//      visitfn(iotype):::
//      visitfn(body):::
//      visitfn(portalspec)
//    case AnonymousPipelineExpr(iotype, stateful : Boolean, body, portalspec) =>
//      visitfn(iotype):::
//      visitfn(body):::
//      visitfn(portalspec)
//    case AnonymousSplitJoinExpr(iotype, stateful : Boolean, body, portalspec) =>
//      visitfn(iotype):::
//      visitfn(body):::
//      visitfn(portalspec)
//    case AnonymousFeedbackLoopExpr(iotype, stateful : Boolean, body, portalspec) =>
//      visitfn(iotype):::
//      visitfn(body):::
//      visitfn(portalspec)
//    case PortalSpecExpr(ps) =>
//      visitfn(ps)
//    case CastExpr(casts, input) =>
//      visitfn(casts):::
//      visitfn(input)
//    case ImplicitTypeCastExpr(from, to, input) =>
//      visitfn(from):::
//      visitfn(to):::
//      visitfn(input)
//    case ParenExpr(input) =>
//      visitfn(input)
//    case PeekExpr(value) =>
//      visitfn(value)
//    case RangeExpr(min, ave, max) =>
//      visitfn(min):::
//      visitfn(ave):::
//      visitfn(max)
//    case InitFunctionDecl(body) =>
//      visitfn(body)
//    case PipelineDecl(iotype, stateful : Boolean, id, parmdecl, body) =>
//      visitfn(iotype):::
//      visitfn(id):::
//      visitfn(parmdecl):::
//      visitfn(body)
//    case SplitJoinDecl(iotype, stateful : Boolean, id, parmdecl, body) =>
//      visitfn(iotype):::
//      visitfn(id):::
//      visitfn(parmdecl)::: 
//      visitfn(body)
//    case strd : StructDecl =>
//      visitfn(strd.id):::
//      visitfn(strd.fielddecls) 
//    case BuiltInFunctionDecl(t, id, parmvardecls) =>
//      visitfn(t):::
//      visitfn(id):::
//      visitfn(parmvardecls)
//    case BuiltInStreamDecl(id, parmvardecls) =>
//      visitfn(id):::
//      visitfn(parmvardecls)
//    case GlobalDecl(stmts, initdecl) =>
//      visitfn(stmts)::: 
//      visitfn(initdecl)
//    case FeedbackLoopDecl(iotype, stateful, id, parmvaldecl, body) =>
//      visitfn(iotype):::
//      visitfn(id):::
//      visitfn(parmvaldecl):::
//      visitfn(body)
//    case HelperFunctionDecl(returntype, id, parmvardecls, io_rate, body ) =>
//      visitfn(returntype):::
//      visitfn(id):::
//      visitfn(parmvardecls):::
//      visitfn(io_rate):::
//      visitfn(body)
//    case CallExpr(ids, parms) =>
//      visitfn(ids):::
//      visitfn(parms) 
//    case ArraySubscriptExpr(base, idx) =>
//      visitfn(base):::
//      visitfn(idx)
//    case FieldExpr(base, field) =>
//      visitfn(base):::
//      visitfn(field)
//    case vdl: VarDeclList=> visitfn(vdl)
//    case VarDecl(id, t, init) =>
//      visitfn(id):::
//      visitfn(t):::
//      visitfn(init)
//    case FieldDecl(id, t) =>
//      visitfn(id):::
//      visitfn(t)
//    case InitArrayExpr(arr) =>
//      visitfn(arr) 
//    case DataRates(rates) =>
//      visitfn(rates) 
//    case PushDecl(rate) =>
//      visitfn(rate)
//    case PopDecl(rate) =>
//      visitfn(rate)
//    case PeekDecl(rate) =>
//      visitfn(rate)
//    case ParmVarDecl(id, t) =>
//      visitfn(id):::
//      visitfn(t)
//    case HandlerFunctionDecl(id, parmvaldecls, body) =>
//      visitfn(id):::
//      visitfn(parmvaldecls)::: 
//      visitfn(body)
//    case cstmt: CompoundStmt => cstmt.stmts.flatMap(visitfn)
//    case PushStmt(value) =>
//      visitfn(value)
//    case SplitStmt(scatherspec) =>
//      visitfn(scatherspec)
//    case JoinStmt(gatherspec) =>
//      visitfn(gatherspec)
//    case DeclStmt(decls) =>
//      visitfn(decls)
//    case ForStmt(ini, cond, inc, body) =>
//      visitfn(ini):::
//      visitfn(cond):::
//      visitfn(inc):::
//      visitfn(body)
//    case IfStmt(cond, i, e) =>
//      visitfn(cond):::
//      visitfn(i):::
//      visitfn(e)
//    case WhileStmt(condition, body) =>
//      visitfn(condition):::
//      visitfn(body)
//    case ReturnStmt(value) =>
//      visitfn(value)
//    case DoWhileStmt(body, condition) =>
//      visitfn(body):::
//      visitfn(condition)
//    case EnqueueStmt(value) =>
//      visitfn(value)
//    case MsgStmt(baseid, id, parms, min, max) =>
//      visitfn(baseid):::
//      visitfn(id):::
//      visitfn(parms)::: 
//      visitfn(min):::
//      visitfn(max)
//    case RRSpec(parms) =>
//      visitfn(parms)
////    case ID(p, l) => Nil
////    case IntLiteral(p, l, v) => 
////    case FloatLiteral(p, l, v) =>
////    case BooleanLiteral(p, l, v) =>
////    case ComplexLiteral(p, l, v) =>
////    case PiLiteral(p, l, v) =>
//    case TernaryExpr(opc, e1, e2, e3) =>
//     //opc : Opcode is not AST
//      visitfn(e1):::
//      visitfn(e2):::
//      visitfn(e3)
//    case BinaryExpr(opc, lhs, rhs) =>
//     //opc : Opcode
//      visitfn(lhs):::
//      visitfn(rhs)
//    case UnaryExpr(opc, input) =>
//     //opc : Opcode
//      visitfn(input)
//    case QualType(value, isconst, pos) =>
//      visitfn(value)
////    case NoType() =>
////  
////    case ErrorType() =>
////    
////    case IntType() =>
////    
////    case FloatType() =>
////    
////    case BooleanType() =>
////    
////    case BitType() =>
////    
////    case ComplexType() =>
////    
////    case VoidType() =>
//      
//    case ConstantArrayType(e,_) =>
//       visitfn(e)
//    case UnresolvedType(name) =>
//      visitfn(name)
//  }
}