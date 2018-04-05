package sem 

import scala.collection.mutable.ArrayBuffer

import ast._
import Opcode._

abstract class DeclContext 

object GlobalDeclContext extends DeclContext {

    // Declarations of types provided by StreamIt, as part of the
    // ``global'' declaration context:
    val T_Int = IntType()
    val T_Float = FloatType()
    val T_Boolean = BooleanType()
    val T_Complex = ComplexType()
    val T_Bit = BitType()
    val T_Void = VoidType()
}

/*
class TranslationUnitDeclContext extends DeclContext {
  //var Decls: ArrayBuffer[ast.Decl]
  var Decls = new scala.collection.mutable.HashMap[String, ast.Decl]
  //Decls += FloatType(),
}
*/
