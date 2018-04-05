//package sem
//
//import ast._
//import common.ErrorReporter._
//
//object TypeSystem {
//  
//  def checkTypeSystem(node: ast.AST): Unit = {
//    node match {
//      case b: BinaryOperator =>
//        println("case binaryOperator")
//        checkBinaryOperator(b)
//      case c: CastExpr =>
//        c.dataType = getCastExprType(c.casts.reverse)
//        println("obj typesystem-> checktypesystem " + c.dataType)
//      case f: FieldExpr =>
//        println("case FieldExpr")
//        f.dataType = getFieldExprType(f.field)
//      case a: ArraySubscriptExpr =>
//        println("check ArraySubscriptExpr")
//        a.dataType = getArraySubscriptExprType(a.base, a.idx)
//      case arr: ConstantArrayType =>
//        arr.dataType = getConstantArrayType(arr)
//        println("arr: ConstantArrayType => " + arr.dataType)
//      case lst: InitListExpr =>
//        println("lst: InitListExpr =>")
//        lst.dataType = getInitListExpr(lst)
//        println("lst: InitListExpr => " + lst.dataType)
//      case p: PeekExpr =>
//        println("this is peek expr")
//      case t: Any =>
//        //TODO Find more real examples
//    }
//  }
//  
//  private def checkBinaryOperator(b: ast.BinaryOperator) {
//    var lt = getType(b.lhs)
//    var rt = getType(b.rhs)
//
//    if(lt.isInstanceOf[ast.NoType] || rt.isInstanceOf[ast.NoType])
//      System.err.println("Figure out dataType")
//
//    if(b.opc == Opcode.BO_Assign) {
//      solveOpcodeDataType(lt, rt, b)
//      rt = getType(b.rhs)
//      if(lt.tID == 200 /*Array*/ && 
//          (rt.tID >= 1 && rt.tID <= 5)/*Primitive Type*/) {
//        //It needs to be changed to support multi dimenstions.
//        //It also needs to be other side eg). int a = arr[10]
//        lt = lt.asInstanceOf[ConstantArrayType].ElementType.Value.dataType
//      }
//      if(!lt.equals(rt)) {
//        reportError("Type Conflict between " + 
//            lt.toString() + " and " + 
//            rt.toString())
//        b.dataType = new ErrorType
//      }
//    } else if(b.opc == Opcode.BO_Add || 
//        b.opc == Opcode.BO_Sub ||
//        b.opc == Opcode.BO_Mul ||
//        b.opc == Opcode.BO_Div) {
//      solveOpcodeDataType(lt, rt, b)
//    } else if(b.opc == Opcode.BO_AddAssign) {
//      solveOpcodeDataType(lt, rt, b)
//      rt = getType(b.rhs)
//      if(!lt.equals(rt)) {
//        reportError("Type Conflict between " + 
//            lt.toString() + " and " + 
//            rt.toString())
//        b.dataType = new ErrorType
//      }
//    }
//  }
//
//  private def getInitListExpr(lst: InitListExpr): ast.Type = {
//    //Multi dimension need to be support.
//    lst.array.foreach( a =>
//      lst.dataType = a.dataType )
//    lst.dataType
//  }
//  private def getConstantArrayType(arr: ConstantArrayType): ast.Type = {
//    if(!checkArrayValidationIdx(arr.size)) {
//      reportError("A size of ConstatnArrayType  must int")
//      arr.dataType = new ErrorType 
//    }
//    arr.dataType
//  }
//
//  private def getArraySubscriptExprType(arrayBase: Expr, 
//      arrayIdx: Expr): ast.Type = {
//    if(!checkArrayValidationIdx(arrayIdx)) {
//      reportError("Array index must be int of datatype")
//    }
//    getArrayBaseType(arrayBase)
//  }
//
//  private def checkArrayValidationIdx(idx: ast.AST): Boolean = {
//    var isIndexType: Boolean = true
//    idx match {
//      case int: IntLiteral =>//Negative value is come to as an unary
//      case id: ID =>
//        if(id.declAST.isEmpty) {
//          reportError(id.lexeme + " decalration is not set before ", 
//              id.lexeme, 
//              id.pos)
//          isIndexType = false
//        } else {
//          if(!id.declAST.get.dataType.isInstanceOf[ast.IntType])
//            isIndexType = false
//        }
//      case f: FieldExpr =>
//        if(!f.dataType.isInstanceOf[ast.IntType])
//          isIndexType = false
//      //It put off until an evaluation step (at runtime)
//      case u: UnaryOperator =>
//      case b: BinaryOperator =>
//      case t : Any =>
//        isIndexType = false
//        reportError("Using index there is unexpected " + t)
//    }
//    isIndexType
//  }
//
//  private def getArrayBaseType(base: ast.AST): ast.Type = {
//    base match {
//      case id: ID =>
//        if(id.declAST.isEmpty) {
//          reportError(id.lexeme + 
//              " decalration is not set before ", 
//              id.lexeme, 
//              id.pos)
//          base.dataType = new ErrorType
//        } else {
//          if(id.declAST.get.dataType.isInstanceOf[ArrayType]) {
//            base.dataType = id.declAST.get.dataType
//          }
//        }
//      case f: FieldExpr =>
//        if(f.dataType.isInstanceOf[ast.ArrayType]){
//          base.dataType = f.dataType
//        }
//      case arr: ArraySubscriptExpr =>
//        base.dataType = getArrayBaseType(arr.base)
//      case t: Any =>
//        //TODO Find more real examples
//    }
//    if(!base.dataType.isInstanceOf[ArrayType]) {
//      reportError("Array base must be array of datatype")
//      base.dataType = new ErrorType
//    }
//    
//    base.dataType
//  }
//
//  private def solveOpcodeDataType(lt: ast.Type, 
//      rt: ast.Type, 
//      b: BinaryOperator) = {
//    if(lt == rt) {
//      b.dataType = lt
//    } else if(lt.tID > rt.tID && math.abs(lt.tID - rt.tID) == 1) {
//      b.dataType = lt
//      val implicitNode = new ast.ImplicitTypeCastExpr(rt, lt, b.rhs)
//      implicitNode.dataType = lt
//      b.rhs = implicitNode
//    } else if(lt.tID < rt.tID && math.abs(lt.tID - rt.tID) == 1) {
//      b.dataType = rt
//      val implicitNode = new ast.ImplicitTypeCastExpr(lt, rt, b.lhs)
//      implicitNode.dataType = rt 
//      b.lhs = implicitNode
//    } else if(lt.tID == 200 /*array*/) {
//      //No conversion needs.
//    } else {
//      //TBD Error Message
//      reportError("Type Conversion Error between " + lt + " and " + rt)
//      b.dataType = new ErrorType
//    }
//  }
//
//  private def getFieldExprType(field: ast.AST): ast.Type = {
//    var fieldId: Option[ast.ID] = None
//    
//    if(field.isInstanceOf[ast.ID]) {
//      fieldId = Some(field.asInstanceOf[ast.ID])
//      if(fieldId.get.declAST.isEmpty){
//        reportError(fieldId.get.lexeme + " decalration is not set before ", 
//            fieldId.get.lexeme, 
//            fieldId.get.pos)
//      } else {
//        return fieldId.get.declAST.get.dataType
//      }
//    }
//    return ast.NoType()
//  }
//
//  private def getCastExprType(node: Any): ast.Type = {
//    var t: ast.Type = ast.NoType()
//    node match {
//      case l: List[Any] => 
//        l.map { case k => t = getCastExprType(k) }
//      case q: QualType =>
//        t = q.Value.dataType
//      case t: Any =>
//        //TODO Find more real examples
//    }
//    return t
//  }
//
//  private def getType(node: ast.Expr): ast.Type = {
//    var nodeType = node.dataType 
//
//    node match {
//      case i: ID =>
//        i.dataType = i.declAST.get.dataType
//        nodeType = i.dataType
//      case arr: ArraySubscriptExpr =>
//        checkArrayUsedExpr(arr)
//        nodeType = getArrayType(arr)
//      case int: IntLiteral =>
//      case float: FloatLiteral =>
//      case f: FieldExpr =>
//        nodeType = f.dataType
//      case b: BinaryOperator =>
//        nodeType = b.dataType
//      case p: PeekExpr =>
//        println("Peek Expr getType")
//        println(p.Value)
//        nodeType = getPeekType(p.Value)
//      case t: Any =>
//        //TODO Find more real examples
//    }
//    nodeType
//  }
//
//  private def getPeekType(peekNode: ast.AST): ast.Type = {
//    var peekType = peekNode.dataType
//    println("getpeektype -> " + peekNode.toString())
//    peekNode match {
//      case id: ID =>
//        if(id.declAST.isEmpty) {
//          reportError(id.lexeme + " decalration is not set before ", 
//            id.lexeme, 
//            id.pos)
//          peekType = NoType()
//        } else {
//          peekType = getPeekType(id.declAST.get)
//        }
//      case vd: VarDecl =>
//        peekType = getPeekType(vd.T.Value)
//      case fd: FieldDecl =>
//        peekType = getPeekType(fd.T.Value)
//      case fe: FieldExpr =>
//        peekType = getPeekType(fe.field)
//      case arr: ArraySubscriptExpr =>
//        peekType = getPeekType(arr.base)
//      case carr: ConstantArrayType =>
//        peekType = getPeekType(carr.ElementType.Value)
//      case t: Any =>
//        println("is this any?")
//    }
//    peekType
//  }
//  private def getArrayType(arrayNode: ast.AST): ast.Type = {
//    var arrayType = arrayNode.dataType
//    arrayNode match {
//      case id: ID =>
//        if(id.declAST.isEmpty) {
//          reportError(id.lexeme + " decalration is not set before ", 
//            id.lexeme, 
//            id.pos)
//          arrayType = NoType()
//        } else {
//          arrayType = getArrayType(id.declAST.get)
//        }
//      case vd: VarDecl =>
//        arrayType = getArrayType(vd.T.Value)
//      case fd: FieldDecl =>
//        arrayType = getArrayType(fd.T.Value)
//      case fe: FieldExpr =>
//        arrayType = getArrayType(fe.field)
//      case arr: ArraySubscriptExpr =>
//        arrayType = getArrayType(arr.base)
//      case carr: ConstantArrayType =>
//        arrayType = getArrayType(carr.ElementType.Value)
//      case t: Any =>
//    }
//    arrayType
//  }
//
//  //int[10] aa; aa[1][2] = 10; //It is wrong
//  private def checkArrayUsedExpr(arrayNode: ast.AST): Unit = {
//    var arrayExprDimension: Int = 1
//    var arrayDeclDimension: Int = 1
//    var arrayExpr = arrayNode;
//    var arrayDecl = arrayNode.dataType;
//
//    while(arrayExpr.isInstanceOf[ArraySubscriptExpr]) {
//      arrayExpr = arrayExpr.asInstanceOf[ArraySubscriptExpr].base
//      arrayExprDimension += 1
//    }
//
//    while(arrayDecl.isInstanceOf[ConstantArrayType]) {
//      arrayDecl = arrayDecl.asInstanceOf[ConstantArrayType].ElementType.Value
//      arrayDeclDimension += 1
//    }
//
//    if(arrayExprDimension != arrayDeclDimension) {
//      reportError("Using array demension is differnt" +
//          " from declaration demention")
//    }
//  }
//}
