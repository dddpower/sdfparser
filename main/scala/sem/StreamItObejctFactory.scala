package sem

import ast._


trait StreamItobject {
  type DType
  var valofObj : DType
  def getStreamItObject: DType = valofObj
  def SetValue(v: DType) = {
    println("Base SetValue")
  }
}
case class IntStreamItobject() extends StreamItobject {
  type DType = Int
  override var valofObj: DType = 0
  override def SetValue(v: Int) {
    valofObj = v
  }
  override def toString = valofObj.toString
}
case class BitStreamItobject() extends StreamItobject {
  type DType = Int//Using Int of data type instead of Bit of data type
  override var valofObj: DType = 0
}
case class ComplexStreamItobject() extends StreamItobject {
  type DType = Float//Using Float of data type instead of Complex of data type
  override var valofObj: DType = 0.0f
}
case class FloatStreamItobject() extends StreamItobject {
  type DType = Float
  override var valofObj: DType = 0.0f
  override def SetValue(v: Float) {
    valofObj = v
  }
  override def toString = valofObj.toString
}
case class ArrayStreamItobject[T]() extends StreamItobject {
  type DType = scala.collection.mutable.LinkedList[T]
  override var valofObj: DType = 
    new scala.collection.mutable.LinkedList[T]
  override def SetValue(v: scala.collection.mutable.LinkedList[T]) {
    valofObj = v
  }
}
case class RecordStreamItobject() extends StreamItobject {
  type DType = scala.collection.mutable.HashMap[String, StreamItobject]
  override var valofObj: DType = 
    new scala.collection.mutable.HashMap[String, StreamItobject]
}
object StreamItobjectFactory {
  def streamItobjectFactory(dtype: ast.Type): StreamItobject = {
    dtype match {
      case i: IntType =>
        return IntStreamItobject()
      case f: BitType =>
        return BitStreamItobject()
      case f: FloatType =>
        return FloatStreamItobject()
      case f: ComplexType =>
        return ComplexStreamItobject()
      //case c: ConstantArrayType =>
        //return constantArrayFactory(c.ElementType.Value)
      case r: RecordType =>
        return RecordStreamItobject()
    }
  }

  private def constantArrayFactory(dtype: Type): StreamItobject  = {
    dtype match {
      case i: IntType =>
        ArrayStreamItobject[IntStreamItobject]()
      case i: BitType =>
        ArrayStreamItobject[BitStreamItobject]()
      case f: FloatType =>
        ArrayStreamItobject[FloatStreamItobject]()
//      case c: ConstantArrayType =>
//        constantArrayFactory(c.ElementType.Value)
      case f: RecordType =>
        ArrayStreamItobject[RecordStreamItobject]()
      case t: Any =>
        println("constantArrayFactory(t: Any) : " + t + " needs")
        null//Error
    }
  }
}