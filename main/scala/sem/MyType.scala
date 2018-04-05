package sem

trait MyType {
  def getName : String
  def getTypeIndex : Int = 0 //for compile
  def canAssignTo(destType : MyType) : Boolean = false//for compile
}