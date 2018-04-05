package sem
import ast._
import scala.collection.AbstractMap
import scala.collection.mutable.LinkedHashMap
abstract class StreamSymbol(typespec: (MyType, MyType), name : String, //typespec : streamtype, input type, output type 
    parent : Option[Scope], streamtype : MyType) extends ScopedSymbol(name, Some(streamtype), parent) {
  var declAST : Option[AST] = None
  val orderedArgs = new LinkedHashMap[String, MySymbol]
  val IOspec = (typespec._1, typespec._2)
  def getMembers = orderedArgs
  def define(sym : MySymbol) : Unit = {
    getMembers += (sym.getName -> sym)
    sym.scope = Some(this)
  }
  scopeName = Some(name)/**/
  override def getName = name
}

class FilterSymbol(typespec : (MyType, MyType), id : String, parent: Option[Scope]) 
  extends StreamSymbol(typespec,id,parent, SymbolTable._filter) {
  override def getTypeIndex = SymbolTable.tFILTER
}

class PipelineSymbol(typespec : (MyType, MyType), id : String, parent: Option[Scope]) 
  extends StreamSymbol(typespec,id,parent, SymbolTable._pipeline) {
  override def getTypeIndex = SymbolTable.tPIPELINE
}

class SplitJoinSymbol(typespec : (MyType, MyType), id : String, parent: Option[Scope]) 
  extends StreamSymbol(typespec,id,parent, SymbolTable._splitjoin) {
  override def getTypeIndex = SymbolTable.tSPLITJOIN
}

class FeedbackSymbol(typespec : (MyType, MyType), id : String, parent: Option[Scope]) 
  extends StreamSymbol(typespec,id,parent, SymbolTable._feedback) {
  override def getTypeIndex = SymbolTable.tFEEDBACK
}

/***built-in stream is always defined in built-in scope***/
class BuiltInStreamSymbol(typespec : MyType, name : String, parent : Scope) 
extends StreamSymbol((typespec, typespec), name, Some(parent), SymbolTable._filter)