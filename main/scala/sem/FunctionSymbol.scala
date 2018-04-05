package sem
import scala.collection.AbstractMap
import scala.collection.mutable.LinkedHashMap
class FunctionSymbol(name : String, retType : Option[MyType], parent : Option[Scope]) 
extends ScopedSymbol(name, retType, parent){
  val orderedArgs = new LinkedHashMap[String, MySymbol]
  scopeName = Some(name)
  def getMembers = orderedArgs
  def define(sym : MySymbol) : Unit = {
    getMembers += (sym.getName -> sym)
    sym.scope = Some(this)    
  }
  override def getName = name /*+ "("+ MySymbol.stripBrackets ( orderedArgs.keySet.toString())+")"*/
}