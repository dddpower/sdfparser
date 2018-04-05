package sem
import ast._
import scala.collection.mutable.LinkedHashMap
import java.io._

object SymbolTable {
  val builtins = new BuiltInScope
  val statics = new StaticScope(Some(builtins))
  // types in increasing order of complexity ("wideness")
  val tUSER : Int = 0 // user-defined type
  val tBOOLEAN : Int = 1
  val tBIT : Int = 2
  val tINT : Int = 3
  val tFLOAT : Int = 4
  val tCOMPLEX : Int = 5
  val tVOID : Int = 6
  //modification for stream it
  val tFILTER : Int = 7
  val tPIPELINE : Int = 8
  val tSPLITJOIN : Int = 9
  val tFEEDBACK : Int = 10
  //arraytype
  val tAUSER : Int = 11 // user-defined array type
  val tABOOLEAN : Int = 12
  val tABIT : Int = 13
  val tAINT : Int = 14
  val tAFLOAT : Int = 15
  val tACOMPLEX : Int = 16
  val tPORTAL : Int = 17
  val tAPORTAL : Int = 18
  
  
  val _user : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("user", tUSER)
  val _boolean : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("boolean", tBOOLEAN)
  val _int : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("int", tINT)
  val _float : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("float", tFLOAT)
  val _void : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("void", tVOID)
  val _bit : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("bit", tBIT)
  //need to be fixed
  val _complex : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("complex", tCOMPLEX)
  val _portal = new BuiltInTypeSymbol("portal", tPORTAL)
  ////////////////////////////
  val _arrayint : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("int[]", tAINT)
  val _arrayfloat : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("float[]", tAFLOAT)
  val _arrayboolean : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("boolean[]", tABOOLEAN)
  val _arraybit : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("bit[]", tABIT)
  val _arraycomplex : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("complex[]", tACOMPLEX)
  val _arrayportal = new BuiltInTypeSymbol("portal[]", tAPORTAL)
  /////////////////////////////////
  val _filter : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("filter", tFILTER)
  val _pipeline : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("pipeline", tPIPELINE)
  val _splitjoin : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("splitjoin", tSPLITJOIN)
  val _feedback : BuiltInTypeSymbol = 
    new BuiltInTypeSymbol("feedback", tFEEDBACK)
  val _abs = new FunctionSymbol("abs", Some(_float), Some(builtins))
  val _arg = new FunctionSymbol("arg", Some(_void), Some(builtins))
  val _exp = new FunctionSymbol("exp", Some(_void), Some(builtins))
  val _log = new FunctionSymbol("log", Some(_void), Some(builtins))
  val _sin = new FunctionSymbol("sin", Some(_void), Some(builtins))
  val _cos = new FunctionSymbol("cos", Some(_void), Some(builtins))
  val _sqrt = new FunctionSymbol("sqrt", Some(_void), Some(builtins))
  val _csqrt = new FunctionSymbol("csqrt", Some(_void), Some(builtins))
  val _floor = new FunctionSymbol("floor", Some(_void), Some(builtins))
  val _ceil = new FunctionSymbol("ceil", Some(_void), Some(builtins))
  val _print = new FunctionSymbol("print", Some(_void), Some(builtins))
  val _println = new FunctionSymbol("println", Some(_void), Some(builtins))
//  val _complex : StructSymbol = 
//    new StructSymbol("complex", Some(builtins), LinkedHashMap("real"->_float, "imag"->_float))
//  
  val _Identity = new BuiltInStreamSymbol(_void, "Identity", builtins)
  val _FileReader = new BuiltInStreamSymbol(_void, "FileReader", builtins)
  val _FileWriter = new BuiltInStreamSymbol(_void, "FileWriter", builtins)
  

//    Identity();"
//    FileReader();"
//    FileWriter();"
  //CymbolListener
  
  /** arithmetic types defined in order from narrowest to widest */
  val indexToType : List[Option[MyType]] = 
    List(Some(_user), Some(_bit), Some(_boolean), Some(_int), Some(_float), Some(_arrayint), Some(_arrayfloat),
        Some(_arrayboolean), Some(_arraybit), Some(_arraycomplex), Some(_void), 
        Some(_filter), Some(_pipeline), Some(_splitjoin), Some(_feedback), 
        Some(_portal), Some(_arrayportal))
  val numPrimitive = 6
  /** Map t1 op t2 to result type (_void implies illegal) */
  
  //ex: + - x /
  val arithmeticResultType : Array[Array[MyType]] =
                            /*struct boolean bit int float complex*/
    /*struct*/  Array(Array(_void,_void, _void, _void, _void, _void),
    /*boolean*/       Array(_void,_void, _void, _void, _void, _void),
    /*bit*/           Array(_void,_void, _bit, _int, _float, _complex),
    /*int*/           Array(_void, _void, _int, _int, _float, _complex),
    /*float*/         Array(_void, _void, _float, _float, _float, _complex),
    /*complex*/       Array(_void, _void, _complex, _complex, _complex, _complex))

    //ex : > < <=
    val relationalResultType : Array[Array[MyType]] = 
                         /*struct boolean bit int float complex*/
    /*struct*/  Array(Array(_void,_void, _void, _void, _void, _void),
    /*boolean*/       Array(_void,_void, _void, _void, _void, _void),
    /*bit*/           Array(_void,_void, _boolean, _boolean, _boolean, _boolean),
    /*int*/           Array(_void, _void, _boolean, _boolean, _boolean, _boolean),
    /*float*/         Array(_void, _void, _boolean, _boolean, _boolean, _boolean),
    /*complex*/       Array(_void, _void, _boolean, _boolean, _boolean, _boolean))

    //==,!=
    val equalityResultType : Array[Array[MyType]] = 
                        /*struct  boolean  bit   int float complex*/
    /*struct*/  Array(Array(_void,_void, _void, _void, _void, _void),
    /*boolean*/       Array(_void,_boolean, _void, _void, _void, _void),
    /*bit*/           Array(_void,_void, _boolean, _boolean, _boolean, _boolean),
    /*int*/           Array(_void, _void, _boolean, _boolean, _boolean, _boolean),
    /*float*/         Array(_void, _void, _boolean, _boolean, _boolean, _boolean),
    /*complex*/       Array(_void, _void, _boolean, _boolean, _boolean, _boolean))
//
//    /** Indicate whether a type needs a promotion to a wider type.
//     *  If not null, implies promotion required.  Null does NOT imply
//     *  error--it implies no promotion.  This works for
//     *  arithmetic, equality, and relational Exprs in Cymbol.
//     */
    
    //void means promotion doesn't needed
    val promoteFromTo : Array[Array[MyType]] = 
                          /*struct boolean bit   int   float  complex*/
    /*struct*/  Array(Array(_void,_void, _void, _void, _void, _void),
    /*boolean*/       Array(_void,_void, _void, _void, _void, _void),
    /*bit*/           Array(_void,_void, _void, _int, _float, _complex),
    /*int*/           Array(_void, _void, _void, _void, _float, _complex),
    /*float*/         Array(_void, _void, _void, _void, _void, _complex),
    /*complex*/       Array(_void, _void, _void, _void, _void, _void))
  def initTypeSystem = {
    println("initTypeSystem")
    indexToType.foreach { 
      x => 
        if(x.isDefined) {
          builtins.define(x.get.asInstanceOf[BuiltInTypeSymbol]) 
        }
      }
    builtins.define(_abs)
    builtins.define(_arg) 
    builtins.define(_exp)
    builtins.define(_log)
    builtins.define(_sin) 
    builtins.define(_cos)
    builtins.define(_sqrt) 
    builtins.define(_csqrt) 
    builtins.define(_floor)
    builtins.define(_ceil)
    builtins.define(_print) 
    builtins.define(_println) 
    builtins.define(_Identity)
    builtins.define(_FileReader) 
    builtins.define(_FileWriter)
    //builtins.define(_complex)
  }
//  def evalType(expr: Expr) : Option[MyType] = expr match {
//    case b: BinaryExpr =>
//      symtab.getResultType(symtab.arithmeticResultType, evalType(b.lhs), evalType(b.rhs))
//    case u: UnaryExpr => None
//    case _=> None
//  }
  
  //def getTypeIndex(id:ID) = id.symbol.get.tYpe.get.getTypeIndex
  
    
  
  /**getResultType evaluats arithmatic expression.
   * this 
   * lhs, rhs are promoted if it is needed
   */
//  def getResultType(typeTable : Array[Array[Option[MyType]]], lhs: Expr, rhs: Expr) : Option[MyType] = {
//    
//    val result = typeTable(lhs_index)(rhs_index)    // operation result type
//    if ( result==Some(_void) ) {
//        println("incompatible type")
//    }
//    else {
//        lhs.promoteToType = promoteFromTo(lhs_index)(rhs_index);
//        rhs.promoteToType = promoteFromTo(rhs_index)(lhs_index);
//    }
//    return result;
//  }
  def isLeaf(expr:Expr) : Boolean = expr match {
      case id: ID => true
      case intlit : IntLiteral => true
      case floatlit:FloatLiteral => true
      case booleanlit:BooleanLiteral => true
      case complexlit:ComplexLiteral => true
      case _=> false
  }
  
  /**evalType evaluates expression node*/
  def evalType(expr:Expr) : Option[MyType] = {
    expr match {
      case id: ID => {
        println("in evalType method, id = "+ id.toString())
        id.symbol.get.tYpe
      }
      
      case intlit : IntLiteral => Some(_int)
      case floatlit:FloatLiteral => Some(_float)
      case booleanlit:BooleanLiteral => Some(_boolean)
      case complexlit:ComplexLiteral => Some(_complex)
      case string: StringLiteral => Some(_void)
      //assume b.lhs, b.rhs promoteToType are already set
      /*
       * 
  
  BO_BitAnd = Value("&")
  BO_BitOr = Value("|")
  BO_BitXor = Value("^")
  
  BO_ShlAssign = Value("<<=")
  BO_ShrAssign = Value(">>=")
  BO_AndAssign = Value("&=")
  BO_OrAssign = Value("|=")
  BO_XorAssign = Value("^=")
       */
      case b: BinaryExpr =>
        b.opc match {
          case Opcode.BO_EQ | Opcode.BO_GE | Opcode.BO_GT | Opcode.BO_LE |
          Opcode.BO_LT | Opcode.BO_NE | Opcode.BO_LogicAnd |Opcode.BO_LogicOr 
          => Some(_boolean)
          
          case Opcode.BO_Mul | Opcode.BO_Div | Opcode.BO_Rem | Opcode.BO_Add | 
        Opcode.BO_Sub | Opcode.BO_Shl |Opcode.BO_Shr | Opcode.BO_Assign |
        Opcode.BO_MulAssign | Opcode.BO_DivAssign | Opcode.BO_RemAssign | 
          Opcode.BO_AddAssign | Opcode.BO_SubAssign=>
            if(b.lhs.promoteToType.isDefined) {
              if(b.lhs.promoteToType==Some(_void)) {
                b.lhs.evalType
              }
              else b.lhs.promoteToType
            }   
            else {//b.lhs.promoteToType is empty
              println("exception: type promotion has not been checked")
              println("lhs test : " + b.lhs)
              println("rhs.evaltype test: " + b.rhs.evalType)
              //println("getTypeIndex test lhs : " + b.lhs.evalType.get.getTypeIndex)
              None
            }
          case _ =>
            println("did I miss something? " + b.opc.toString())
            None
        }
        
      case u: UnaryExpr =>
        u.opc match {
          case Opcode.UO_BitNot=> u.input.evalType
          case Opcode.UO_LogicNot=> Some(_boolean)
          case Opcode.UO_Minus=> u.input.evalType
          case Opcode.UO_Plus=> u.input.evalType
          case Opcode.UO_PostDec=> u.input.evalType
          case Opcode.UO_PostInc=> u.input.evalType
          case Opcode.UO_PreDec=> u.input.evalType
          case Opcode.UO_PreInc=> u.input.evalType
        }
      case paren: ParenExpr => paren.input.evalType
      case ase : ArraySubscriptExpr => ase.base.evalType
      case call : CallExpr => call.id.evalType
      case fe: FieldExpr => {
        //val struct = fe.base.symbol.get.tYpe.get.asInstanceOf[StructSymbol]
        val struct = fe.base.evalType.get.asInstanceOf[StructSymbol]
        struct.resolveMember(fe.field.toString()).get.tYpe
      }
      
      /**
       * evaltype of PopExpr is already solved in makeScopeTree, 
       * it doesn't need to happen twice.
       */
      //case popexpr: PopExpr => 
    }
  }
//  def bop(a: Expr, b: Expr) : Option[MyType] = 
//    getResultType(arithmeticResultType, a, b)
//  
//    def bop(a : Option[MyType], b: Option[MyType]): Option[MyType] =
//      getResultType(arithmeticResultType, a, b)
//  def relop(a: Expr, b: Expr) = {
//    getResultType(relationalResultType, a, b);
//    _boolean
//  }
//  def eqop(a: Expr, b: Expr) = {
//    getResultType(equalityResultType, a,b)
//    _boolean
//  }
  
  
  def uminus(a: AST) = {
    _void
  }
  def unot(a: AST) = {
    _boolean
  }
  
  
  /***things need to be modified***/
//  def arrayIndex(id: Expr, index: Expr) : MyType = {
//    //val s: Option[MySymbol] = id.scope.get.resolve(id.toString)
//    val vs : VariableSymbol = s.get.asInstanceOf[VariableSymbol]
//    id.symbol = Some(vs)
//    return vs.tYpe.asInstanceOf[ArrayType].elementType
//  }

  def call(id : Expr, args : List[Any]) : Option[MyType] = None //for compile

  
  def member(expr : Expr, field: Expr) : Option[MyType] = {
    val scope : StructSymbol = expr.evalType.get.asInstanceOf[StructSymbol]
    val s : MySymbol = scope.resolveMember(field.toString).get
    field.symbol = Some(s)
    return s.tYpe
  }
  override def toString = builtins.toString
  def printOut : Unit = {
    val fout = new PrintWriter("ScopeTree result2.gv")
    fout.write("digraph G {\n")
    printOut(this.builtins, fout)
    fout.write("}")
    fout.close
  }
  def printOut(symbol : Scope, fout: PrintWriter) : Unit = {
      symbol match {
        case bs : BaseScope => {
          fout.write("  " + bs.scopeName.get+ "[shape=record, label=\"{" + bs.scopeName.get + " scope|")
          fout.write("{symbols: ")
          bs.symbols.foreach(f => {
          fout.write(f._1 + "|")  
          })
          fout.write("}}\"];\n")
        }
        case ss : StreamSymbol => {
          fout.write("  " + ss.getName + "[shape=record, label=\"{ Stream Symbol: " + ss.getName + "|")
          fout.write("{arguments: ")
          ss.orderedArgs.foreach( f=>{
            fout.write(f._1 + "|")
          })
          fout.write("}}\"];\n")
        }
        case fs : FunctionSymbol => {
          fout.write("  " + fs.getName + "[shape=record, label=\"{ Function Symbol: " + fs.getName + "|")
          fout.write("{arguments: ")
          fs.orderedArgs.foreach( f=>{ 
            fout.write(f._1 + "|")
          })
          fout.write("}}\"];\n")
        }
        case ss : StructSymbol => {
          fout.write("  " + ss.scopeName.get + "[shape=record, label=\"{ Struct Symbol: " + ss.getName + "|")
          fout.write("{field members: ")
          ss.fields.foreach( f=>{ 
            fout.write(f._1 + "|")
          })
          fout.write("}}\"];\n")
        }
      }
      //println("surrounding scope queue " + symbol.surroundingScopeQueue)
      symbol.surroundingScopeQueue.foreach {
      f=> {
        //println(T"scope name test " + symbol.scopeName.get + " " + f.scopeName.get)
        fout.write("  " + symbol.scopeName.get + "->" + f.scopeName.get + "\n")
        printOut(f, fout)
      }
    }
  }
}