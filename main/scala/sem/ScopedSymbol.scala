package sem
import scala.collection.AbstractMap
import scala.collection.mutable._

abstract class ScopedSymbol(name : String, tYpe : Option[MyType],  enclosingscope: Option[Scope]) 
extends MySymbol(name, tYpe) with MyType with Scope {
  
//  def this(name: String, enclosingscope: Scope) {
//    this(Some(name), Some(SymbolTable._void), Some(enclosingscope))
//  }
  enclosingScope = enclosingscope
  
  if(enclosingscope !=None) {
    enclosingscope.get.surroundingScopeQueue.enqueue(this)
  }
  protected def this(name : String, enclosingScope : Option[Scope]) {
    this(name,None,enclosingScope)
  }
//  protected def this(tyPe: MyType, parent : Option[Scope]) {
//    this(Some("anonymous"+ tyPe.getName), Some(tyPe), parent)
//  }

//  def resolve(name : String) 
//  = getMembers.get(name).orElse{getEnclosingScope.get.resolve(name)}.orElse(None)
  def resolve(name : String) : Option[MySymbol] = {
    //symbols.get(name).orElse{enclosingScope.get.resolve(name)orElse(None)}
    //println("getMembers " + getMembers)
    if(getMembers.get(name)!=None) {
      getMembers.get(name)
    }
    else if(enclosingScope!=None) {
      enclosingScope.get.resolve(name)
    }
    else None
  }
  def resolveCurrent(name : String) : Option[MySymbol] 
  = getMembers.get(name).orElse(None)
  def resolveType(name : String) = resolve(name)
  
  //def define(sym : MySymbol) : Unit = {
  //  getMembers += (sym.getName -> sym)
  //  sym.scope = Some(this)
  //}
  //scopeName = Some(name.get + Counter.getNumber)

  /** Indicate how subclasses store scope members. Allows us to
   *  factor out common code in this class.
   */
  def getMembers : AbstractMap[String, MySymbol]
}