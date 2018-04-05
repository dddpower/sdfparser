package sem

class BuiltInTypeSymbol(name: String, typeindex : Int) extends MySymbol(name) with MyType {
  override def getTypeIndex = typeindex
  override def toString() = getName
}