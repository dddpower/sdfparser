package ast 

object Opcode extends Enumeration {
  type Opcode = Value //Type alias

  val TO_Cond = Value("?")

  val BO_Mul = Value("*")
  val BO_Div = Value("/")
  val BO_Rem = Value("%")
  val BO_Add = Value("+")
  val BO_Sub = Value("-")
  val BO_Shl = Value("<<")
  val BO_Shr = Value(">>")
  val BO_LT = Value("<")
  val BO_GT = Value(">")
  val BO_LE = Value("<=")
  val BO_GE = Value(">=")
  val BO_EQ = Value("==")
  val BO_NE = Value("!=")
  val BO_LogicAnd = Value("&&")
  val BO_LogicOr = Value("||")
  val BO_BitAnd = Value("&")
  val BO_BitOr = Value("|")
  val BO_BitXor = Value("^")
  val BO_Assign = Value("=")
  val BO_MulAssign = Value("*=")
  val BO_DivAssign = Value("/=")
  val BO_RemAssign = Value("%=")
  val BO_AddAssign = Value("+=")
  val BO_SubAssign = Value("-=")
  val BO_ShlAssign = Value("<<=")
  val BO_ShrAssign = Value(">>=")
  val BO_AndAssign = Value("&=")
  val BO_OrAssign = Value("|=")
  val BO_XorAssign = Value("^=")

  val UO_PreInc = Value("++")
  val UO_PreDec = Value("--")
  val UO_PostInc = Value("++")
  val UO_PostDec = Value("--")
  val UO_Plus = Value("+")
  val UO_Minus = Value("-")
  val UO_BitNot = Value("~")
  val UO_LogicNot = Value("!")
}
