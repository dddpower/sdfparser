package sem
import scala.collection.mutable._
import scala.collection.AbstractMap

abstract class BaseScope(parent : Option[Scope]) extends Scope {
  //var enclosingScope : Option[Scope] = parent  // None if global (outermost) scope
  enclosingScope = parent
  if(parent!=None) {//set this to parent's child
     enclosingScope.get.surroundingScopeQueue.enqueue(this)
  }
  val symbols = new LinkedHashMap[String, MySymbol]
  
  def resolve(name : String) : Option[MySymbol] = {
    
    if(symbols.get(name)!=None) {
      symbols.get(name) 
    }
    else if(enclosingScope!=None) {
      enclosingScope.get.resolve(name)
    }
    else {
      None
    }
  }
  def resolveCurrent(name : String) : Option[MySymbol] = {
    if(symbols.get(name)!=None) {
      symbols.get(name)
    }
    else None
  }
  def define(sym : MySymbol) : Unit = {
    println(sym.getName + " has defined")
    symbols += ( sym.getName -> sym)
    sym.scope = Some(this)  // track the scope in each symbol
  }
  
  //def enclosingScope = enclosingScope
  
  override def toString() = symbols.keySet.toString()
}