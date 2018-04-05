package ast 

import scala.util.parsing.input.Positional
import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

import common.WritingTool._
import Opcode._
import sem._

abstract sealed class AST {
  def extractList : List[Any] 
  def extractSDF : String = toString
  def extractSDF(extra:String): String = extractSDF
}

///////////////////////////////////////////////////////////////////////////////
// Type:
///////////////////////////////////////////////////////////////////////////////

abstract class Type extends AST
trait LeafNode
case class NoType() extends Type //for optional AST type information
with LeafNode {
  override def extractList = Nil
  override def extractSDF = ""
}
//for Error type during the semantic analysis
case class ErrorType() extends Type with LeafNode {
  override def toString = "error"
  override def extractList = Nil
  override def extractSDF = ""
}

abstract class PrimitiveType extends Type with LeafNode

case class IntType() extends PrimitiveType {
  override def toString = "int"
  override def extractList = Nil
  override def extractSDF = toString
}

case class FloatType() extends PrimitiveType {
  override def toString = "float"
  override def extractList = Nil
  override def extractSDF = toString
}

case class BooleanType() extends PrimitiveType {
  override def toString = "boolean"
  override def extractList = Nil
  override def extractSDF = toString
}


/**TBD: bitType in Laminar*/
case class BitType() extends PrimitiveType {
  override def toString = "bit"
  override def extractList = Nil
  override def extractSDF = toString
}


/**TBD: ComplexType in Laminar*/
case class ComplexType() extends PrimitiveType {
  override def toString = "complex"
  override def extractList = Nil
  override def extractSDF = toString
}

case class VoidType() extends Type with LeafNode {
  override def toString = "void"
  override def extractList = Nil
  override def extractSDF = toString
}

abstract class ArrayType extends Type {
  //var ElementType: Type
}

case class ConstantArrayType (
  ElementType: Type,
  size: Expr
) extends Type {
  override def extractList = List(ElementType,size)
  override def extractSDF = ElementType.extractSDF + "[" + size.extractSDF + "]"
  override def extractSDF(extra:String) = ElementType.extractSDF(extra) + "[" + size.extractSDF(extra) + "]"
}

case class RecordType (
  id: ID,
  fieldDecls: FieldDeclList
) extends Type {
  override def extractList = List(id, fieldDecls)
  override def extractSDF = id.toString()
}


//abstract class VectorType extends Type

// UnresolvedType is a type introduced during parsing to temporarily store
// type-names that will be resolved during semantic analysis:
case class UnresolvedType (
  Name: ID  //Name: Expr
) extends Type {
  override def toString = Name.toString
  override def extractList : List[AST] = List(Name)
  override def extractSDF = Name.toString
}

case class QualType (
  var Value: Type, //var because most not resolved until sem analysis
  isConst:   Boolean = false, //type qualifier
  pos:       Position = NoPosition
) extends Type {
  override def extractList = List(isConst,pos)
  override def extractSDF = Value.toString
}

case class IOTypeSpec (
  InT: Type,
  OutT: Type
) extends AST {
  override def extractList = List(InT,OutT)
  override def extractSDF = InT.toString + " -> " + OutT.toString()
}

/**not been used yet*/
case class PortalType (
  id: ID
) extends Type {
  override def extractList = List(id)
  override def extractSDF = ""
}

///////////////////////////////////////////////////////////////////////////////
// Decl:
///////////////////////////////////////////////////////////////////////////////

abstract class Decl extends AST {
  override def extractList : List[AST] = {
    println("Decl extractList shouldn't reach")
    Nil
  }
}

case class DeclList(decls: List[Decl]) extends Decl {
  override def extractList : List[Decl] = {
    //println("DeclList")
    decls
  }
  override def extractSDF = decls.map(f=>f.extractSDF).mkString(endStmt) + endStmt
  
  override def extractSDF(extra:String) = decls.map(f=>f.extractSDF(extra)).mkString(endStmt) + endStmt
}
case class NoDecl() extends Decl {
  
} //temporary dummy value

//case class ProgramDecl(
//  decls: List[Decl]
//) extends Decl
case class ProgramDecl(decls:DeclList)
extends Decl {
  override def extractList : List[AST] = {
    println("programDecl")
    List(decls)
  }
  override def extractSDF = decls.decls.map(f=>f.extractSDF).mkString(endStmt)
  override def extractSDF(extra:String) = decls.decls.map(f=>f.extractSDF(extra)).mkString(endStmt)
}

abstract class TypeDecl extends Decl {
  val Name: ID
  val TypeValue: Type
  override def extractList : List[AST] = List(Name,TypeValue)
}

// Parameter declaration:
case class ParmVarDecl (
  val id: ID,
  val T:  Type
) extends Decl {
  override def extractList : List[AST] = List(id, T)
  override def extractSDF = T.toString() + space + id.extractSDF
  override def extractSDF(extra:String) = T.toString() + space + id.extractSDF(extra)
}

//case class RecordDecl extends TypeDecl

case class DeclRefExpr (
  val Name: Expr,
  val decl: Decl
) extends Expr {
  override def extractList : List[AST] = List(Name,decl)
}

abstract class StreamDecl extends Decl {
  val iotype : IOTypeSpec
  val stateful : Boolean
  val id : ID
  val parmvardecls : ParmVarDeclList
  val body : AST //filterbodyDecl or compuound
}

trait FilterSpec {
  def iotype: IOTypeSpec
  def stateful : Boolean
  def id : ID
  def parmvardecls: ParmVarDeclList
  def body: FilterBodyDecl
}
case class FilterDecl (
  iotype:        IOTypeSpec,
  stateful:  Boolean,
  id:            ID,
  parmvardecls:  ParmVarDeclList,
  body:          FilterBodyDecl
) extends StreamDecl with FilterSpec {
  
  override def extractList : List[AST] = List(iotype,id,parmvardecls,body)
}
case class FilterBodyDecl (
  decls: DeclList
) extends Decl {
    override def extractList : List[AST] = List(decls)
    /**continues from extractSDF of filterDecl*/
}
 
case class PipelineDecl (
  iotype:       IOTypeSpec,
  stateful: Boolean,
  id:           ID,
  parmvardecls : ParmVarDeclList,
  body:         CompoundStmt 
) extends StreamDecl {
  override def extractList : List[AST] = List(iotype,id,parmvardecls,body)
}

case class SplitJoinDecl (
  iotype:       IOTypeSpec,
  stateful: Boolean,
  id:           ID,
  parmvardecls : ParmVarDeclList,
  body:         CompoundStmt 
) extends StreamDecl {
  override def extractList : List[AST] = List(iotype,id,parmvardecls,body)
}

case class FeedbackLoopDecl (
  iotype:       IOTypeSpec,
  stateful: Boolean,
  id:           ID,
  parmvardecls : ParmVarDeclList,
  body:         CompoundStmt 
) extends StreamDecl {
  override def extractList : List[AST] = List(iotype,id,parmvardecls,body)
}
case class ParmVarDeclList (
    parmvardecls : List[ParmVarDecl]) extends Decl {
  override def extractList : List[AST] = parmvardecls
  override def extractSDF = parmvardecls.map(f=>f.extractSDF).mkString(", ")
  override def extractSDF(extra:String) = parmvardecls.map(f=>f.extractSDF(extra)).mkString(", ")
}
case class StructDecl (
  id:           ID,
  fielddecls:   FieldDeclList
) extends Decl {
  override def extractList : List[AST] = List(id,fielddecls)
}
case class FieldDeclList(fielddecls:List[FieldDecl])
extends Decl {
  override def extractList : List[AST] = fielddecls
}
//case class NativeDecl (
//  id:              ID,
//  nativefuncdecl : List[NativeFuncDecl]
//) extends Decl
case class BuiltInFunctionDecl (
  id:              ID,
  T:               Type, 
  parmvardecls:    ParmVarDeclList
) extends Decl//TBD 
{
  override def extractList : List[AST] = List(id,T,parmvardecls)
}
  
case class BuiltInStreamDecl(
  //T:            Type, 
  id:           ID,
  parmvardecls:    ParmVarDeclList
) extends Decl {
  override def extractList : List[AST] = List(id,parmvardecls)
}


//for static keyword
case class GlobalDecl (
  decls:           VarDeclList,
  initDecl:        InitFunctionDecl
) extends Decl {
  override def extractList : List[AST] = List(decls,initDecl)
}

abstract class BaseFunctionDecl extends Decl

case class InitFunctionDecl (
  Body: CompoundStmt 
) extends BaseFunctionDecl  {
  override def extractList : List[AST] = List(Body)
  override def extractSDF : String = Body.extractSDF
  override def extractSDF(extra:String) : String = Body.extractSDF(extra)
}

case class PreWorkFunctionDecl (
  IO_rates: DataRates, 
  Body:     CompoundStmt 
) extends BaseFunctionDecl {
  override def extractList : List[AST] = List(IO_rates, Body)
}

case class WorkFunctionDecl (
  IO_rates: DataRates, 
  Body:     CompoundStmt 
) extends BaseFunctionDecl {
  override def extractList : List[AST] = List(IO_rates, Body)
  override def extractSDF : String = Body.extractSDF
  override def extractSDF(extra:String) : String = Body.extractSDF(extra)
}

case class HelperFunctionDecl (
  ReturnType:   Type, 
  id:           ID,
  parmvardecls: ParmVarDeclList,
  IO_rates:     DataRates, 
  Body:         CompoundStmt 
) extends BaseFunctionDecl {
  override def extractList : List[AST] = List(ReturnType, id, parmvardecls, IO_rates, Body)
}

case class HandlerFunctionDecl (
  id:           ID,
  parmvardecls: ParmVarDeclList,
  Body:         CompoundStmt 
) extends BaseFunctionDecl {
  override def extractList : List[AST] = List(id, parmvardecls, Body)
}

case class PushDecl(
  PushRate: Expr
) extends Decl {
  override def extractList : List[AST] = List(PushRate)
}

case class PopDecl(
  PopRate: Expr
) extends Decl {
  override def extractList : List[AST] = List(PopRate)
}

case class PeekDecl(
  PeekRate: Expr
) extends Decl {
  override def extractList : List[AST] = List(PeekRate)  
}

case class DataRates (
  Rates: DeclList
) extends AST {
  override def extractList : List[AST] = List(Rates)
}
abstract class RateDecl


case class FieldDecl (
  id:   ID,
  T:    Type
) extends Decl// In the Struct
{
    override def extractList : List[AST] = List(id,T)
}
case class VarDecl (
  id:   ID,
  T:    Type,
  init : Expr
) extends Decl {
  override def extractList : List[AST] = List(id,T,init)
  override def extractSDF = {
    T.extractSDF + space + id.extractSDF + {
      if(init==NoExpr())
        ""
      else " = " + init.extractSDF
    }
  }
  
  override def extractSDF(extra:String) = {
    T.extractSDF(extra) + space + id.extractSDF(extra) + {
      if(init==NoExpr())
        ""
      else " = " + init.extractSDF(extra)
    }
  }
}


/**VarDeclList is an implicit AST node for potential repeated variable declaration. it will not shown to AST result
 * explicitly
 * ex: 1.int i,j,k; => VarDeclList(List(VarDecl,VarDecl,VarDecl))
 *     2.int i; => VarDeclList(List(VarDecl))
 */
case class VarDeclList (
  vardecls : List[VarDecl]
) extends Decl {  
  override def extractList : List[AST] = vardecls
  override def extractSDF = vardecls.map(f=>f.extractSDF).mkString(";") + semcol
  override def extractSDF(extra:String) = vardecls.map(f=>f.extractSDF(extra)).mkString(";") + semcol
}

///////////////////////////////////////////////////////////////////////////////
// Stmt:
///////////////////////////////////////////////////////////////////////////////
abstract class Stmt extends AST

case class NoStmt() extends Stmt {// To fill optional parts like else
  override def extractList = Nil
}
                                 // branches or for-loop expressions 

/**According to LLVM, DeclStmt is Adaptor class for mixing declarations with statements 
 * and expressions.For example, CompoundStmt mixes statements, expressions and declarations (variables, types). 
 * Another example is ForStmt, where the first statement can be an expression or a declaration.  
 */
case class DeclStmt (
  Decls: DeclList
) extends Stmt {
  override def extractList : List[AST] = List(Decls)
  override def extractSDF : String = this.Decls.decls.map(f=>f.extractSDF).mkString(endStmt)
  override def extractSDF(extra:String) : String = this.Decls.decls.map(f=>f.extractSDF(extra)).mkString(endStmt)
}

case class CompoundStmt (
  stmts: List[Stmt]
) extends Stmt {
  override def extractList : List[AST] = stmts
  override def extractSDF = openBlock + 
  stmts.map(f=>f.extractSDF).mkString(endStmt) + semcol +
  closeBlock
  override def extractSDF(extra:String) = openBlock + 
  stmts.map(f=>f.extractSDF(extra)).mkString(endStmt) + semcol +
  closeBlock
}

case class PushStmt (
  Value: Expr
) extends Stmt {
  override def extractList : List[AST] = List(Value)
  override def extractSDF = "push(" + Value.extractSDF + ")"
  override def extractSDF(extra:String) = "push(" + Value.extractSDF(extra) + ")"
}

abstract class ScatherGatherSpec extends AST

case class RRSpec (
  Parms: ExprList
) extends ScatherGatherSpec {
  override def extractList : List[AST] = List(Parms)
}


case class DuplicateSpec() extends ScatherGatherSpec {
  override def extractList : List[AST] = Nil
}

case class SplitStmt(
  ScatherSpec: ScatherGatherSpec
) extends Stmt {
  override def extractList : List[AST] = List(ScatherSpec)
}

case class JoinStmt(
  GatherSpec: ScatherGatherSpec
) extends Stmt {
  override def extractList : List[AST] = List(GatherSpec)
}

case class EnqueueStmt(
  Value: Expr
) extends Stmt {
  override def extractList : List[AST] = List(Value)
}

case class ForStmt(
  Init : Stmt,
  Condition: Expr,
  Incr : Expr,
  Body : Stmt
) extends Stmt {
  override def extractList : List[AST] = List(Init,Condition,Incr,Body)
  override def extractSDF = "for(" + Init.extractSDF + semcol + Condition.extractSDF + semcol +
  Incr.extractSDF + ")" + newLine + Body.extractSDF
  
  override def extractSDF(extra:String) = "for(" + Init.extractSDF(extra) + semcol + Condition.extractSDF(extra) + semcol +
  Incr.extractSDF(extra) + ")" + newLine + Body.extractSDF(extra)
}

case class IfStmt(
  Condition : Expr,
  ifBody : Stmt,
  elseBody : Stmt
) extends Stmt {
  override def extractList : List[AST] = List(Condition,ifBody,elseBody)
  override def extractSDF = 
    "if(" + Condition.extractSDF + ")" + ifBody.extractSDF + "else" +
    newLine + elseBody
  override def extractSDF(extra:String) = 
    "if(" + Condition.extractSDF(extra) + ")" + ifBody.extractSDF(extra) + "else" +
    newLine + elseBody
  
}

case class WhileStmt(
  Condition : Expr,
  Body : Stmt
) extends Stmt {
  override def extractList : List[AST] = List(Condition,Body)
  override def extractSDF = "while(" + Condition.extractSDF +")" + Body.extractSDF
  override def extractSDF(extra:String) = 
    "while(" + Condition.extractSDF(extra) +")" + Body.extractSDF(extra)
}

case class ReturnStmt(
  Value: Expr
) extends Stmt {
  override def extractList : List[AST] = List(Value)
}

case class DoWhileStmt(
  Body : Stmt,
  Condition : Expr
) extends Stmt {
  override def extractList : List[AST] = List(Body,Condition)
  override def extractSDF = "do" + Body.extractSDF + "while(" + Condition.extractSDF +")"
  override def extractSDF(extra:String) = 
    "do" + Body.extractSDF(extra) + "while(" + Condition.extractSDF(extra) +")"
}

case class BreakStmt(
) extends Stmt {
  override def extractList : List[AST] = Nil
  override def extractSDF = "break"
}

case class ContinueStmt(
) extends Stmt {
  override def extractList : List[AST] = Nil
  override def extractSDF = "continue"
}

case class CommaStmt(
) extends Stmt {
  override def extractList : List[AST] = Nil
}

case class MsgStmt(
  baseid  :   ID,
  id      :   ID,
  Parms   : ExprList,
  min     : Expr,
  max     : Expr
) extends Stmt {
  override def extractList : List[AST] = List(baseid,id,Parms,min,max)
}

///////////////////////////////////////////////////////////////////////////////
// Expr:
///////////////////////////////////////////////////////////////////////////////
abstract class Expr extends Stmt {
  var scope : Option[Scope] = None
  var symbol : Option[MySymbol] = None
  var evalType : Option[MyType] = None
  var promoteToType : Option[MyType] = None
}
case class ExprList(exprs : List[Expr]) extends Expr {
  override def extractList : List[AST] = exprs
  override def extractSDF = exprs.map(_.extractSDF).mkString(", ")
  override def extractSDF(extra:String) = exprs.map(_.extractSDF(extra)).mkString(", ")
}

case class NoExpr() extends Expr // for not present optional expressions
with LeafNode {
  override def extractList = Nil
  override def extractSDF = ""
}
case class PopExpr() extends Expr
with LeafNode {
  override def extractList = Nil
  override def extractSDF = "pop()"
}
case class StarExpr() extends Expr
with LeafNode {
  override def extractList = Nil  
}  

case class PeekExpr(
  Value: Expr
) extends Expr {
  override def extractList : List[AST] = List(Value)
  override def extractSDF = "peek(" + Value.extractSDF + ")"
  override def extractSDF(extra:String) = "peek(" + Value.extractSDF(extra) + ")"
}

case class RangeExpr(
  min: Expr,
  ave: Expr,
  max: Expr
) extends Expr {
  override def extractList : List[AST] = List(min,ave,max)
}

case class CallExpr (
  id:    ID,
  Parms: ExprList
) extends Expr {
  override def extractList : List[AST] = List(id,Parms)
  override def extractSDF = id.extractSDF + "(" + Parms.extractSDF + ")"
  override def extractSDF(filtername:String) = id.extractSDF(filtername) + "(" + Parms.extractSDF(filtername) + ")"
}

case class InitArrayExpr(//For Array Init
  array: ExprList
) extends Expr {
  override def extractList : List[AST] = List(array)
  override def extractSDF = "{" + array.extractSDF + "}"
  override def extractSDF(extra:String) = "{" + array.extractSDF(extra) + "}"
}

abstract class ConstructExpr extends Expr

case class AnonymousStreamExpr(
  TypeParm:     Type,
  Parms:        ExprList,
  portalSpec:   Expr
) extends ConstructExpr //TBD for prototyping only -> modified
{
  override def extractList : List[AST] = List(TypeParm,Parms,portalSpec)
}
case class ConstructAnonFilterExpr(
  id:            ID,
  iotype:        IOTypeSpec,//TBD For Setting NoIOTypeSpec
  stateful:  Boolean,
  body:          FilterBodyDecl,
  portalSpec:    Expr
) extends ConstructExpr with FilterSpec {
  override def parmvardecls = ParmVarDeclList(Nil)
  override def extractList : List[Any] = List(iotype,stateful,body,portalSpec)
}

case class AnonymousPipelineExpr(
  iotype:        IOTypeSpec,//TBD For Setting NoIOTypeSpec
  stateful:  Boolean,
  body:          CompoundStmt,
  portalSpec:    Expr
) extends ConstructExpr {
  override def extractList = List(iotype,stateful,body,portalSpec)
}

case class AnonymousSplitJoinExpr(
  iotype:        IOTypeSpec,//TBD For Setting NoIOTypeSpec
  stateful:  Boolean,
  body:          CompoundStmt,
  portalSpec:    Expr
) extends ConstructExpr {
  override def extractList : List[Any] = List(iotype,stateful,body,portalSpec)
}

case class AnonymousFeedbackLoopExpr(
  iotype:        IOTypeSpec,//TBD For Setting NoIOTypeSpec
  stateful:  Boolean,
  body:          CompoundStmt,
  portalSpec:    Expr
) extends ConstructExpr {
  override def extractList : List[Any] = List(iotype,stateful,body,portalSpec)
}

case class StreamConstructExpr(
  id:           ID,
  TypeParm:     Type,
  Parms:        ExprList,
  portalSpec:   Expr
) extends ConstructExpr {
  override def extractList : List[AST] = List(id,TypeParm,Parms,portalSpec)
}

case class PortalSpecExpr(
  Portals:  ExprList
) extends Expr {
  override def extractList : List[AST] = List(Portals)
}

case class ID(lexeme: String) extends Expr {
  var declAST: Option[AST] = None
  override def toString = lexeme
  override def extractList : List[Any] = List(lexeme)
  override def extractSDF = lexeme
  override def extractSDF(extra:String) = lexeme + "_" + extra
} 

case class Literal(pos:Position, lexeme: String, value: Any) extends Expr
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme,value)
}

case class StringLiteral(pos: Position, lexeme: String) extends Expr
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
}
case class IntLiteral(pos: Position, lexeme: String, value: Int) extends Expr 
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
  override def extractSDF = lexeme
}
case class ComplexLiteral(pos: Position, lexeme: String, value: String) extends Expr 
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
  override def extractSDF = lexeme
}
case class FloatLiteral(pos: Position, lexeme: String, value: Float) extends Expr 
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
  override def extractSDF = lexeme
}
case class BooleanLiteral(pos: Position, lexeme: String, value: Boolean) extends Expr 
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
  override def extractSDF = lexeme
}
case class PiLiteral(pos: Position, lexeme: String, value: Double) extends Expr 
with LeafNode {
  override def extractList : List[Any] = List(pos,lexeme)
  override def extractSDF = lexeme
}
case class TernaryExpr(
  opc: Opcode,
  e1: Expr,
  e2: Expr,
  e3: Expr
) extends Expr {
  override def extractList : List[Any] = List(opc,e1,e2,e3)
}

case class BinaryExpr(
  opc: Opcode,
  var lhs: Expr,//It can be changed during the TypeSystem
  var rhs: Expr
) extends Expr {
  override def extractList : List[Any] = List(opc,lhs,rhs)
  override def extractSDF = lhs.extractSDF + space + opc + space + rhs.extractSDF
  override def extractSDF(extra:String) = lhs.extractSDF(extra) + space + opc + space + rhs.extractSDF(extra)
}

case class UnaryExpr(
  opc: Opcode,
  input: Expr
) extends Expr {
  override def extractList : List[Any] = List(opc, input)
  override def extractSDF = {
    if(opc==Opcode.UO_PostDec || opc==Opcode.UO_PostInc)
      input.extractSDF + opc.toString()
    else
      opc.toString() + input.extractSDF
  }
  override def extractSDF(extra:String) = {
    if(opc==Opcode.UO_PostDec || opc==Opcode.UO_PostInc)
      input.extractSDF(extra) + opc.toString()
    else
      opc.toString() + input.extractSDF(extra)
  }
}

case class ParenExpr(
  input: Expr
) extends Expr {
  override def extractList : List[AST] = List(input)
  override def extractSDF = "(" + input.extractSDF + ")"
  override def extractSDF(extra:String) = "(" + input.extractSDF(extra) + ")"
}

case class TypeList(types:List[Type]) extends Type {
  override def extractList = types
}
case class CastExpr(
  casts: TypeList,
  input: Expr
) extends Expr {
  override def extractList : List[AST] = List(casts,input)
  override def extractSDF = "(" + casts.extractSDF + ")" + input.extractSDF
  override def extractSDF(extra:String) = "(" + casts.extractSDF(extra) + ")" + input.extractSDF(extra)
}

case class ArraySubscriptExpr(
  base: Expr, //containing array 
  idx: Expr   //index
) extends Expr {
  override def extractList : List[AST] = List(base, idx)
}

case class FieldExpr(
  base: Expr, //containing structure
  field: Expr
) extends Expr {
  override def extractList : List[AST] = List(base,field)
}

case class ImplicitTypeCastExpr(
  from: Type,
  to: Type,
  input: Expr
  ) extends Expr {
  override def extractList : List[AST] = List(from,to,input)
}
