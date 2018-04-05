package sem
import scala.collection.AbstractMap
import scala.collection.mutable.LinkedHashMap
class StructSymbol(name : String, parent : Option[Scope], fieldmember : LinkedHashMap[String, MySymbol]) 
  extends ScopedSymbol(name, Some(SymbolTable._user), parent) {
  def this(name : String, parent : Option[Scope]) {
    this(name, parent, new LinkedHashMap[String, MySymbol])
  }
  val fields = fieldmember
  
  /** For a.b, only look in fields to resolve b, not up scope tree */
  def resolveMember(name : String) = fields.get(name) 
  def getMembers = fields
  def define(sym : MySymbol) : Unit = {
    getMembers += (sym.getName -> sym)
    sym.scope = Some(this)
  }
  scopeName = Some(name)
  override def getTypeIndex = SymbolTable.tUSER
  override def toString = name
  //scopeName = Some(name.get + Counter.getNumber)
}
