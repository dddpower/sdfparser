//package sem
//
//
//import ast._
//import common.ErrorReporter._
//import scala.collection.mutable.MutableList
//import scala.collection.mutable.Map
//import scala.collection.mutable.Stack
//import scala.collection.mutable.ArrayBuffer
//
/////////////////////////////////////////////////////////////////////////////////
//// TreeNode:
/////////////////////////////////////////////////////////////////////////////////
//class IdEntry(var id: ast.ID, var declAST: ast.AST) {
//  var value: Option[StreamItobject] = None
//  
//  def valueInstance(dtype: ast.Type) = {
//    value = Some(StreamItobjectFactory.streamItobjectFactory(dtype))
//  }
//  
//  def setValue(v: Option[Any]) = {
//    println("value.get in setValue : " + value.get)
//    println("v.get in setValue : " + v.getOrElse("none"))
//    (value.get, v.getOrElse("none")) match {
//      case (l : IntStreamItobject, r : Int) =>
//        l.SetValue(r.asInstanceOf[Int])
//      case (l : IntStreamItobject, r : Float) =>
//        l.SetValue(v.get.asInstanceOf[Float].toInt)
//      case (l : FloatStreamItobject, r : Int) =>    
//        l.SetValue(r.asInstanceOf[Int].toFloat)
//      case (l : FloatStreamItobject, r : Float) =>
//        l.SetValue(r.asInstanceOf[Float])
//      case (l : ArrayStreamItobject[Any], r : Any) =>
//        println("Arraystreamitobject entered at setvalue")
//      case (t1: Any, t2: Any)  => println("ANY :: " + t1 + " " + t2)
//      //TBD
//    }
//  }
//  
//  def getValue: Any = {
//    value.get match {
//      case i: IntStreamItobject =>
//        return i.getStreamItObject
//      case f: FloatStreamItobject =>
//        return f.getStreamItObject
//      case a: ArrayStreamItobject[Any] =>
//        return a.getStreamItObject
//    }
//  }
//  
//  def copyIdEntry: IdEntry = {
//    new IdEntry(id, declAST)
//  }
//}
//
/////////////////////////////////////////////////////////////////////////////////
//// ScopeTreeNode:
/////////////////////////////////////////////////////////////////////////////////
//object ScopeTreeNode {
//  
//  var id: Int = 0
//  
//  var Root: ScopeTreeNode = new ScopeTreeNode(0, null)
//  //Stream or Struct
//  var idDeclBaseStreams: Map[String, ast.StreamDecl] = 
//    Map[String, ast.StreamDecl]()
//  
//  def getID: Int = {
//    id += 1
//    id
//  }
//  def hasStreamOrStructName(id: String): Boolean = {
//    idDeclBaseStreams.contains(id)
//  }
//  
//  // traverseBFSTree
//  def checkIdComparingStreamId = {
//    val queue = new scala.collection.mutable.Queue[ScopeTreeNode]
//    queue.enqueue(ScopeTreeNode.Root)
//    
//    while (!queue.isEmpty) {
//      val node: ScopeTreeNode = queue.dequeue()
//      node.listofChild.foreach(tmp => queue.enqueue(tmp))
//      
//      checkIdwithStreamId(node)
//      // Test
//      // debugPrint(node)
//    }
//  }
//  
//  def printOutScopeTree = {
//    val queue = new scala.collection.mutable.Queue[ScopeTreeNode]
//    queue.enqueue(ScopeTreeNode.Root)
//    
//    while (!queue.isEmpty) {
//      val node: ScopeTreeNode = queue.dequeue()
//      node.listofChild.foreach(tmp => queue.enqueue(tmp))
//
//      debugPrint(node)
//    }
//  }
//  
//  private def checkIdwithStreamId(node: ScopeTreeNode): Boolean = {
//    var isError: Boolean = false
//    if (!node.idDeclEntry.isEmpty && node.level > 0) {
//      for(ids <- node.idDeclEntry) {
//        if(idDeclBaseStreams.contains(ids._1)){
//          reportError(ids._1 + " has the same name as a stream or structure", 
//            ids._1, 
//            ids._2.id.pos)
//          isError = true
//        }
//      }
//    }
//    isError
//  }
//
//  private def debugPrint(node: ScopeTreeNode) = {
//    // Printing Decls
//    if (!node.idDeclEntry.isEmpty) {
//      println("Decl level is  " + node.level + " ")
//      for(ids <- node.idDeclEntry.values.toList.sortWith(sortFn)) {
//        print("          ")
//        print(" : " + ids.id.lexeme + " [")
//        if(ids.declAST.toString().length() > 10) {
//          print(ids.id.lexeme + ",  Value (" + ids.value + ") " 
//                + " " + ids.declAST.toString().substring(0, 10) + "...")
//        } else {
//          print(ids.id.lexeme + ",  Value (" + ids.value + ") " 
//                + " " + ids.declAST.toString())
//        }
//        println("]  ")
//      }
//    }
//  }
//  
//  private def sortFn = (a:IdEntry, b: IdEntry) => 
//    { a.id.pos.line + a.id.pos.column < b.id.pos.line + b.id.pos.column }
//
//  def findScopeTreeNode(searchNode: ast.AST): ScopeTreeNode = {
//    var findNode: Option[ScopeTreeNode] = None
//    val queue = new scala.collection.mutable.Queue[ScopeTreeNode]
//    queue.enqueue(ScopeTreeNode.Root)
//    while (!queue.isEmpty) {
//      val node: ScopeTreeNode = queue.dequeue()
//      node.listofChild.foreach(tmp => queue.enqueue(tmp))
//      if(node.nodeInfo == searchNode) {
//        findNode = Some(node)
//      }
//    }
//    findNode.get
//  }
//}
//
//
//
/////////////////////////////////////////////////////////////////////////////////
//// ScopeTreeNode:
/////////////////////////////////////////////////////////////////////////////////
//class ScopeTreeNode(var level: Int, var nodeInfo: ast.AST) {
//  var uId: Int = ScopeTreeNode.getID
//  var parent: Option[ScopeTreeNode] = None
//  var idDeclEntry: Map[String, IdEntry] = Map[String, IdEntry]()
//  var listofChild : MutableList[ScopeTreeNode] = MutableList[ScopeTreeNode]()
//  
//  def enterDeclEntry(id: ast.ID/*key*/, declAST: ast.AST) {
//    
//    val newEntry: IdEntry = new IdEntry(id, declAST)
//    /////////????
//    if(idDeclEntry.contains(id.lexeme)) {
//      reportError("Identifier redeclared " + id.lexeme,
//              id.lexeme,
//              id.pos)
//    } else {
//      idDeclEntry += (id.lexeme -> newEntry)
//    }
//  }
//
//  def copyScopeTreeNode(nodeInfo: ast.AST): ScopeTreeNode = {
//    val node = new ScopeTreeNode(level+1, nodeInfo)
//    node.parent = Some(ScopeTreeNode.this)
//    listofChild.foreach( 
//        s => node.listofChild += s.copyScopeTreeNode(s.nodeInfo)
//    )
//    node.listofChild.foreach(s => s.parent = Some(node))
//    listofChild += node
//    idDeclEntry.foreach(s => node.idDeclEntry += (s._1 -> s._2.copyIdEntry))
//    node
//  }
//
//  def newScopeTreeNode(nodeInfo: ast.AST): ScopeTreeNode = {
//    val node = new ScopeTreeNode(level+1, nodeInfo)
//    node.parent = Some(ScopeTreeNode.this)
//    listofChild += node
//    node
//  }
//
//  def getParent: Option[ScopeTreeNode] = { parent }
//
//  def checkUsedVarId(usedId: ast.ID): Unit = {
//    var curNode: ScopeTreeNode = this
//    var searched: Boolean = false
//    while(!searched) {
//      for(ids <- curNode.idDeclEntry) {
//        if(ids._2.id.lexeme == usedId.lexeme){
//          usedId.declAST = Some(ids._2.declAST)
//          return
//        } 
//      }
//      if(curNode.parent.isEmpty) {
//        reportError("Identifier undeclared " + usedId.lexeme,
//              usedId.lexeme,
//              usedId.pos)
//        return
//      } else {
//        curNode = curNode.parent.get
//      }
//    }
//  }
//
//  def checkUsedStFieldId(base: ast.AST, field: ast.AST): Unit = {
//    var baseId: Option[ast.ID] = None 
//    var fieldId: Option[ast.ID] = None
//
//    base match {
//      case f: FieldExpr =>
//        checkUsedStFieldId(f.field, field)
//        return
//      case i: ID =>
//        baseId = Some(i)
//      case t: Any =>
//        println("<unmatched: " + t)
//        reportWarning("checkUsedStFieldId unmatched node: " + t)
//        println("semvisit : " + t)
//        //Error
//    }
//    if(field.isInstanceOf[ast.ID])
//      fieldId = Some(field.asInstanceOf[ast.ID])
//
//    checkUsedStFieldId(baseId.get, fieldId.get)
//  }
//  
//  private def checkUsedStFieldId(base: ast.ID, field: ast.ID): Unit = {
//    if(base.declAST.isEmpty) {
//      reportError(base.lexeme + " is not decleared before", 
//            base.lexeme, 
//            base.pos)
//      return
//    }
//    
//    // Find a member in recode
//    var find: Boolean = false
//    
//    base.declAST.get match {
//      case v: VarDecl =>
//        find = isContanedField(v.T.Value/*Decl*/, field)
//      case f: FieldDecl =>
//        find = isContanedField(f.T.Value/*Decl*/, field)
//      case t: Any =>
//        println("<unmatched: " + t)
//        reportWarning("checkUsedStFieldId unmatched node: " + t)
//        println("semvisit : " + t)
//        //Error
//    }
//    
//    if(find == false) {
//      reportError(field.lexeme + " is not member of a " + base.lexeme, 
//            field.lexeme, 
//            field.pos)
//    }
//  }
//  
//  def idOfInstance(vDecl:ast.AST, searchID: ast.ID) = {
//    var dType: ast.Type = NoType()
//    vDecl match {
//      case v: VarDecl =>
//        dType = v.T.Value
//      case f: FieldDecl =>
//        dType = f.T.Value
//      case p: ParmVarDecl =>
//        dType = p.T.Value
//      case carr: ConstantArrayType =>
//        dType = carr.ElementType.Value
//      case t: Any =>
//        //
//    }
//    findDeclofId(searchID)._2.valueInstance(dType)
//  }
//  
//  private def findDeclofId(searchID: ast.ID): (Option[ast.AST], IdEntry)  = {
//    var curNode: ScopeTreeNode = this
//    var searched: Boolean = false
//    var declOfbaseId: Option[ast.AST] = None
//    var declOfIdEntry:IdEntry = null
//    while(!searched) {
//      for(ids <- curNode.idDeclEntry) {
//        if(ids._2.id.lexeme == searchID.lexeme){
//          declOfbaseId = Some(ids._2.declAST)
//          declOfIdEntry = ids._2
//          searched = true
//        }
//      }
//      if(curNode.parent.isEmpty && searched == false) {
//        reportError(searchID.lexeme + " is not decleared", 
//            searchID.lexeme, 
//            searchID.pos)
//        searched = true
//      } else if(curNode.parent.isDefined) {
//        curNode = curNode.parent.get
//      }
//    }
//    (declOfbaseId, declOfIdEntry)
//  }
//  
//  private def isContanedField(container: ast.AST, field: ast.ID): Boolean = {
//    if(container.isInstanceOf[RecordType]) {
//      for(f <-container.asInstanceOf[RecordType].fieldDecls) {
//        if(f.isInstanceOf[FieldDeclList]) {
//          f.asInstanceOf[FieldDeclList].decls.foreach(s => {
//            if(s.id.lexeme == field.lexeme){
//              field.declAST = Some(s)//
//              return true
//            } 
//          })
//        }
//      }
//    } else {
//      reportError("Container must be a RecodeType not " + container + " type", 
//            field.lexeme, 
//            field.pos)
//    }
//    false
//  }
//
//  def resolveDataType(unsolvedType: ast.UnresolvedType, 
//      id: ast.ID): Option[ast.Type] = {
//    val queue = new scala.collection.mutable.Queue[ScopeTreeNode]
//    queue.enqueue(ScopeTreeNode.Root)
//    
//    while (!queue.isEmpty) {
//      val node: ScopeTreeNode = queue.dequeue()
//      node.listofChild.foreach(tmp => queue.enqueue(tmp))
//      for(ids <- node.idDeclEntry){
//        if(ids._2.declAST.isInstanceOf[StructDecl]) {
//          val st = ids._2.declAST.asInstanceOf[StructDecl]
//          if(st.id.lexeme == unsolvedType.Name.lexeme) {
//            return Some(new ast.RecordType(st.id, st.fieldDecls))
//          }
//        }
//      }
//    }
//    reportError(id.lexeme + " cannot be found in identification", 
//            id.lexeme, 
//            id.pos)
//    None
//  }
//
//  def setValueOfDeclID(value: Option[Any], searchNode: ast.ID): Unit = {
//    var curNode: ScopeTreeNode = this
//    var searched: Boolean = false
//    while(!searched) {
//      for(ids <- curNode.idDeclEntry) {
//        if(ids._2.id.lexeme == searchNode.lexeme){
//          ids._2.setValue(value)
//          return
//        } 
//      }
//      if(curNode.parent.isEmpty) {
//        reportError("Identifier undeclared " + searchNode.lexeme,
//              searchNode.lexeme,
//              searchNode.pos)
//        //Error
//        return
//      } else {
//        curNode = curNode.parent.get
//      }
//    }
//  }
//
//  def getValueOfDeclID(searchNode: ast.ID): Any  = {
//    var curNode: ScopeTreeNode = this
//    var searched: Boolean = false
//    while(!searched) {
//      for(ids <- curNode.idDeclEntry) {
//        if(ids._2.id.lexeme == searchNode.lexeme){
//          return ids._2.getValue
//        } 
//      }
//      if(curNode.parent.isEmpty) {
//        reportError("Identifier undeclared " + searchNode.lexeme,
//              searchNode.lexeme,
//              searchNode.pos)
//        //Error
//        return
//      } else {
//        curNode = curNode.parent.get
//      }
//    }
//  }
//}
