package sem

class ArrayType(elType : MyType) extends MySymbol(elType + "[]") with MyType{
  val elementType = elType
  //def getTypeIndex : Int = 0 //for compile
}