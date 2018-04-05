package ast 

import common.ErrorReporter._

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition
import java.io._
import scala.io.Source


object ASTPrinter {

  //val trav = new ASTTraversal[Int]
  val MaxCol = 255 //maximum indentation level for AST dump
  var fout: PrintWriter = null; //output file descriptor

  def printAST(srcfile: String, outfile: String, node: Any) = {
    fout = new PrintWriter(new File(outfile))
    fout.write("// AST created from file " + srcfile)
    fout.write("\n\n") 
    IndentWriter.setOutput(fout)
    printvisit(node, 0)
    fout.close()
  }

  private object IndentWriter {
    private object AlignChar extends Enumeration {
      type AlignChar = Value //Type alias
      val ALIGN_Active   = Value("|")
      val ALIGN_Expiring = Value("`")
      val ALIGN_Inactive = Value(" ")
    }
    import AlignChar._

    private var ColumnStatus = Array.fill(MaxCol)(ALIGN_Inactive)
    private var fout: PrintWriter = null

    def setOutput(of: PrintWriter) = {
      fout = of
    }

    def activateColumn(col: Int) = {
      ColumnStatus(col) = ALIGN_Active
      col
    }

    def activateExpiredColumn(col: Int) = {
      ColumnStatus(col) = ALIGN_Expiring
      col
    }

    def expireColumn(col: Int) = {
      if (ColumnStatus(col) != ALIGN_Active) {
        reportWarning("Selected column not active!","",NoPosition) 
      }
      ColumnStatus(col) = ALIGN_Expiring
      col-1
    }  

    def writeIndent(LastCol: Int) = {
      for (col <- 1 to LastCol) {
        // first formatting character:
        fout.write(ColumnStatus(col).toString) 
        if (ColumnStatus(col) == ALIGN_Expiring)
          ColumnStatus(col) = ALIGN_Inactive
        // second formatting character:
        if (col < LastCol)
          fout.write(' ')
        else
          fout.write('-')
      }
    }
  }

  private def printPos(pos : Position) {
    fout.print("<")
    if(pos==NoPosition)
      fout.print("NoPosition")
    else
      fout.print("line:" + pos.line + ",col:" + pos.column)
    fout.print(">")
  }

  private def printList(l: List[Any], indent: Int, expires: Boolean = true) {
    l.zipWithIndex foreach { case(elmt, idx) =>
      if (expires && (idx == l.length-1))
        IndentWriter.expireColumn(indent)
      printvisit(elmt, indent)
    }
  }
  
  private def printVarDecl(v : Type) {
    var value : Type = v
    if(value.isInstanceOf[ConstantArrayType]) {//For Array
      var arrValue : ConstantArrayType = null
      var arrList : List[Expr] = List[Expr]()
      /**not necessary
       * 
       */
//      while (value.isInstanceOf[ConstantArrayType]) {//For Multiple Dimension
//        arrValue = value.asInstanceOf[ConstantArrayType]
//        arrList ::= arrValue.size
//        value = arrValue.ElementType.Value
//      }
      fout.print(value + " ")
      arrList = arrList.reverse
      arrList.foreach(s => 
        s match {
          case id: ID => {//eg)int[MAX] v;
            fout.print("[" + id.lexeme + "]")
          }
          case b: BinaryExpr => { //float[2*n] results;
            fout.print("[" + b + "]")//TBD
          }
          case i: IntLiteral => {//eg)int[10] v;
            fout.print("[" + i.value + "]")
          }
          case _ => { 
            fout.print("[" + s + "]")
          }
        })
        fout.print(" " + "array")
    } else if(value.isInstanceOf[RecordType]){
      val rType = value.asInstanceOf[RecordType]
      fout.print("referenced " + rType.id.lexeme + " RecordType")
    } else {
      fout.print(value)
    }

    //TBD
  }

  private def printvisit(node: Any, indent: Int): Unit = node match {
    case ProgramDecl(decls) =>
      fout.println("ProgramDecl")
      val myindent = IndentWriter.activateColumn(indent+1)
      printList(decls.decls, myindent)
    case FilterDecl(t, s, id, d, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("FilterDecl ")
      //printPos(id.pos)
      fout.print(" " + id.lexeme + " ")
      if(!s) fout.print("!")
      fout.print("statefull ")
      //write out filter signature:
      fout.print("'" + t.InT.toString() + "->" + t.OutT.toString() + " ")
      fout.print("(")
      if(d.parmvardecls.isEmpty)
        fout.print("void")
      else
        d.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != d.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      //ParmVarDecls:
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.parmvardecls, declbodyindent, false)
      //body:
      IndentWriter.expireColumn(declbodyindent)
      printvisit(b, declbodyindent)
    case FilterBodyDecl(decls) =>
      IndentWriter.writeIndent(indent)
      fout.println("FilterBodyDecl")
      val bodyindent = IndentWriter.activateColumn(indent+1)
      printList(decls.decls, bodyindent)
    case PreWorkFunctionDecl(iorates, body) =>
      IndentWriter.writeIndent(indent)
      fout.println("PreWorkFunctionDecl")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(iorates, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(body, nextindent)
    case WorkFunctionDecl(iorates, body) =>
      IndentWriter.writeIndent(indent)
      fout.println("WorkFunctionDecl")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(iorates, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(body, nextindent)
    case NoDecl() =>
      fout.println("NoDecl")
    case StreamConstructExpr(id, t, l, ps) =>
      IndentWriter.writeIndent(indent)
      fout.print("StreamConstructExpr " + id.lexeme + " '")
      fout.print("(")
      if(l.exprs.isEmpty){
        fout.print("void")//TBD.
      }
      l.exprs.zipWithIndex foreach { case(parm, idx) =>
        fout.print(parm)
        if(idx != l.exprs.length-1) fout.print(" ")
      }
      fout.println(")'")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(ps, nextindent)
    case ConstructAnonFilterExpr(id, t, sf, fb, ps) => //TBD
      IndentWriter.writeIndent(indent)
      fout.print("AnonymousFilterExpr ")
      if(!sf) fout.print("!")
      fout.print("statefull ")
      fout.print(id)
      if(t!=null)
        fout.print("'" + t.InT + "->" + t.OutT + "'")
      fout.println()
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printvisit(fb, declbodyindent)
      IndentWriter.expireColumn(declbodyindent)
      printvisit(ps, declbodyindent)
    case AnonymousPipelineExpr(t, sf, b, ps) =>
      IndentWriter.writeIndent(indent)
      fout.print("AnonymousPipelineExpr ")
      if(!sf) fout.print("!")
      fout.print("statefull ")
      if(t!=null)
        fout.print("'" + t.InT + "->" + t.OutT + "'")
      fout.println()
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printvisit(b, declbodyindent)
      IndentWriter.expireColumn(declbodyindent)
      printvisit(ps, declbodyindent)
    case AnonymousSplitJoinExpr(t, sf, b, ps) =>
      IndentWriter.writeIndent(indent)
      fout.print("AnonymousSplitJoinExpr ")
      if(!sf) fout.print("!")
      fout.print("statefull ")
      if(t!=null)
        fout.print("'" + t.InT + "->" + t.OutT + "'")
      fout.println()
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printvisit(b, declbodyindent)
      IndentWriter.expireColumn(declbodyindent)
      printvisit(ps, declbodyindent)
    case AnonymousFeedbackLoopExpr(t, sf, b, ps) =>
      IndentWriter.writeIndent(indent)
      fout.print("AnonymousFeedbackLoopExpr")
      if(!sf) fout.print("!")
      fout.print("statefull ")
      if(t!=null)
        fout.print("'" + t.InT + "->" + t.OutT + "'")
      fout.println()
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printvisit(b, declbodyindent)
      IndentWriter.expireColumn(declbodyindent)
      printvisit(ps, declbodyindent)
    case PortalSpecExpr(ps) =>
      IndentWriter.writeIndent(indent)
      fout.print("PortalSpecExpr to '(")
      ps.exprs.zipWithIndex foreach { case(portal, idx) =>
        fout.print(portal.asInstanceOf[ID].lexeme)
        if(idx != ps.exprs.length-1) fout.print(", ")
      }
      fout.println(")'")
    case CastExpr(cl, i) =>
      IndentWriter.writeIndent(indent)
      fout.println("CastExpr")
      val cindent = IndentWriter.activateColumn(indent+1)
      printList(cl.types, cindent, false)
      //printvisit(cl, cindent)
      IndentWriter.expireColumn(cindent)
      printvisit(i, cindent)
    case ImplicitTypeCastExpr(f, t, i) =>
      IndentWriter.writeIndent(indent)
      fout.println("ImplicitTypeCastExpr " + f + " to " + t)
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(i, nextindent)
    case p: ParenExpr =>
      IndentWriter.writeIndent(indent)
      fout.print("ParenExpr")
      
      if(p.evalType.isDefined)
        fout.print(", evalType = " + p.evalType)
      if(p.promoteToType.isDefined)
        fout.print(", promoteToType = " + p.promoteToType)
      fout.println
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(p.input, nextindent)
    case PeekExpr(v) =>
      IndentWriter.writeIndent(indent)
      fout.println("PeekExpr")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(v, nextindent)
    case RangeExpr(min, ave, max) =>
      IndentWriter.writeIndent(indent)
      fout.println("RangeExpr")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(min, nextindent)
      printvisit(ave, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(max, nextindent)
    case NoExpr() =>
      IndentWriter.writeIndent(indent)
      fout.println("NoExpr")
    case InitFunctionDecl(body) =>
      IndentWriter.writeIndent(indent)
      fout.println("InitFunctionDecl")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(body, nextindent)
    case PipelineDecl(t, s, id, d, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("PipelineDecl ")
      //printPos(id.pos)
      fout.print(" " + id.lexeme + " ")
      if(!s) fout.print("!")
      fout.print("statefull ")
      fout.print("'" + t.InT + "->" + t.OutT + " ")
      fout.print("(")
      //ParmVarDecls:
      if(d.parmvardecls.isEmpty)
        fout.print("void")
      else
        d.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != d.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.parmvardecls, declbodyindent, false)
      //body:
      IndentWriter.expireColumn(declbodyindent)
      printvisit(b, declbodyindent)
    case SplitJoinDecl(t, s, id, d, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("SplitJoinDecl " + id.lexeme + " ")
      if(!s) fout.print("!")
      fout.print("statefull ")
      fout.print("'" + t.InT + "->" + t.OutT + " ")
      fout.print("(")
      //ParmVarDecls:
      if(d.parmvardecls.isEmpty)
        fout.print("void")
      else
        d.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != d.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.parmvardecls, declbodyindent, false)
      //body:
      IndentWriter.expireColumn(declbodyindent)
      printvisit(b, declbodyindent)
    case str: StructDecl =>
      IndentWriter.writeIndent(indent)
      fout.print("StructDecl ")
      //printPos(str.id.pos)
      fout.println(" " + str.id.lexeme + " ")
      val declsindent = IndentWriter.activateColumn(indent+1)
      if(str.fielddecls.fielddecls.isEmpty)
        fout.print("void")
      else
        str.fielddecls.fielddecls.zipWithIndex foreach { case(fields, idx) =>
          if(idx == str.fielddecls.fielddecls.length-1) IndentWriter.expireColumn(declsindent)
          printvisit(fields, declsindent)
        }
      //TBD
//    case NativeDecl(id, nf) =>
//      IndentWriter.writeIndent(indent)
//      fout.print("NaiveDecl ")
//      printPos(id.pos)
//      fout.println(" " + id.lexeme + " ")
//      val declsindent = IndentWriter.activateColumn(indent+1)
//      if(nf.isEmpty)
//        fout.print("void")
//      else
//        nf.zipWithIndex foreach { case(funcs, idx) =>
//          if(idx == nf.length-1) IndentWriter.expireColumn(declsindent)
//          printvisit(funcs, declsindent)
//        }
    case BuiltInFunctionDecl(id, t, pa) =>
      IndentWriter.writeIndent(indent)
      fout.print("BuiltInFunctionDecl ")
      //printPos(id.pos)
      fout.print(" '" + t + " " + id.lexeme + " ")
      val declsindent = IndentWriter.activateColumn(indent+1)
      fout.print("(")
      if(pa.parmvardecls.isEmpty)
        fout.print("void")
      else
        pa.parmvardecls.zipWithIndex foreach { case(funcs, idx) =>
          if(idx == pa.parmvardecls.length-1) IndentWriter.expireColumn(declsindent)
          printvisit(funcs, declsindent)
        }
      fout.println(")'")
      //pa.parmvardeclsrmVarDecls:
      printList(pa.parmvardecls, declsindent)

    case BuiltInStreamDecl(id, pd) =>
      IndentWriter.writeIndent(indent)
      fout.print("BuiltInStreamDecl ")//
      //printPos(id.pos)
      fout.print(" '" + id.lexeme + " ")
      val declsindent = IndentWriter.activateColumn(indent+1)
      fout.print("(")
      if(pd.parmvardecls.isEmpty)
        fout.print("void")
      else
        pd.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != pd.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      //ParmVarDecls:
      printList(pd.parmvardecls, declsindent)
      //TBD
    case GlobalDecl(st, d) =>
      IndentWriter.writeIndent(indent)
      fout.println("GlobalDecl")
      val declstmtindent = IndentWriter.activateColumn(indent+1)
      printList(st.vardecls, declstmtindent, false)
      IndentWriter.expireColumn(declstmtindent)
      printvisit(d, declstmtindent)
      //TBD
    case FeedbackLoopDecl(t, s, id, d, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("FeedbackLoopDecl ")
      //printPos(id.pos)
      fout.print(" " + id.lexeme + " ")
      if(!s) fout.print("!")
      fout.print("statefull ")
      //write out filter signature:
      fout.print("'" + t.InT + "->" + t.OutT + " ")
      fout.print("(")
      if(d.parmvardecls.isEmpty)
        fout.print("void")
      else
        d.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != d.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      //ParmVarDecls:
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.parmvardecls, declbodyindent, false)
      //body:
      IndentWriter.expireColumn(declbodyindent)
      printvisit(b, declbodyindent)
    case HelperFunctionDecl(rt, id, p, io, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("HelperFunctionDecl ")
      fout.print(" '")
      /*if(!rt.isConst)*/ fout.print("!")
      fout.print("const ")
      printVarDecl(rt)
      fout.print("' ")
      //printPos(id.pos)
      fout.print(" '" + id.lexeme + " ")
      fout.print("(")
      if(p.parmvardecls.isEmpty)
        fout.print("void")
      else
        p.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != p.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      val nextindent = IndentWriter.activateColumn(indent+1)
      //ParmVarDecls:
      printList(p.parmvardecls, nextindent, false)
      printvisit(io, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(b, nextindent)
    case CallExpr(id, d) =>
      IndentWriter.writeIndent(indent)
      fout.print("CallExpr ")
//      ids.zipWithIndex foreach { case(parm, idx) =>
//        fout.print(parm.lexeme)
//        if(idx != ids.length-1) fout.print(".")
//      }
      fout.print(id)
      if(id.evalType.isDefined)
        fout.print(": evalType = " + id.evalType)
      else {
        fout.print(" " + id.symbol)
      }
      if(id.promoteToType.isDefined)
        fout.print(", promoteToType = " + id.promoteToType)
      fout.print("'")
      fout.print("(")
      //ParmVarDecls:
      if(d.exprs.isEmpty)
        fout.print("void")
      else
        d.exprs.zipWithIndex foreach { case(parm, idx) =>
          fout.print(parm)
          if(idx != d.exprs.length-1) fout.print(" ")
        }
      fout.println(")'")
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.exprs, declbodyindent)
    case popexpr: PopExpr =>
      IndentWriter.writeIndent(indent)
      fout.print("PopExpr")
      if(popexpr.evalType.isDefined)
        fout.print(": evalType = " + popexpr.evalType)
      if(popexpr.promoteToType.isDefined)
        fout.print(", promoteToType = " + popexpr.promoteToType)
      fout.println
    case StarExpr() =>
      IndentWriter.writeIndent(indent)
      fout.println("StarExpr [*]")
    case ArraySubscriptExpr(b, i) =>
      IndentWriter.writeIndent(indent)
      fout.println("ArraySubscriptExpr")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(b, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(i, nextindent)
      //TBD
    case FieldExpr(f, b) =>
      IndentWriter.writeIndent(indent)
      fout.println("FieldExpr")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(f, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(b, nextindent)
    case VarDeclList(d) =>
      if(!d.isEmpty) {
        for (parm <- d)
          printvisit(parm, indent)
      }
    case VarDecl(id, t, init) =>
      IndentWriter.writeIndent(indent)
      fout.print("VarDecl ")
      //printPos(id.pos) 
      fout.print(" " + id.lexeme + " '")
      /*if(!t.isConst)*/ fout.print("!")
        fout.print("const ")
      printVarDecl(t)
      fout.println("'")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(init, nextindent)
//    case FieldDeclList(d) =>
//      if(!d.isEmpty) {
//        for (parm <- d)
//          printvisit(parm, indent)
//      }
    case FieldDecl(id, t) =>
      IndentWriter.writeIndent(indent)
      fout.print("FieldDecl ")
      //printPos(id.pos) 
      fout.print(" " + id.lexeme + " '")
      /*if(!t.isConst)*/ fout.print("!")
      fout.print("const ")
      printVarDecl(t)
      fout.println("'")
    case InitArrayExpr(arr) =>
      IndentWriter.writeIndent(indent)
      fout.println("InitArrayExpr ")//For Array
      val nextindent = IndentWriter.activateColumn(indent+1)
      arr.exprs.zipWithIndex foreach { case(itm, idx) =>
        if(idx == arr.exprs.length-1) IndentWriter.expireColumn(nextindent)
        printvisit(itm, nextindent)
      }
      //TBD
    case DataRates(rates) =>
      printList(rates.decls, indent, false)
    case PushDecl(rate) =>
      IndentWriter.writeIndent(indent)
      fout.println("PushDecl")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(rate, nextindent)
    case PopDecl(rate) =>
      IndentWriter.writeIndent(indent)
      fout.println("PopDecl")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(rate, nextindent)
    case PeekDecl(rate) =>
      IndentWriter.writeIndent(indent)
      fout.println("PeekDecl")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(rate, nextindent)
    case ParmVarDecl(id, t) =>
      IndentWriter.writeIndent(indent)
      fout.print("ParmVarDecl " + id.lexeme + " '")
      printVarDecl(t)
      fout.println("'")
    case HandlerFunctionDecl(id, d, b) =>
      IndentWriter.writeIndent(indent)
      fout.print("HandlerFunctionDecl " + id.lexeme + " '")
      fout.print("(")
      if(d.parmvardecls.isEmpty)
        fout.print("void")
      else
        d.parmvardecls.zipWithIndex foreach { case(parm, idx) =>
          printVarDecl(parm.T)
          if(idx != d.parmvardecls.length-1) fout.print(" ")
        }
      fout.println(")'")
      //ParmVarDecls:
      val declbodyindent = IndentWriter.activateColumn(indent+1)
      printList(d.parmvardecls, declbodyindent, false)
      //body:
      IndentWriter.expireColumn(declbodyindent)
      printvisit(b, declbodyindent)
    case CompoundStmt(stmts) =>
      IndentWriter.writeIndent(indent)
      fout.println("CompoundStmt")
      printList(stmts, IndentWriter.activateColumn(indent+1))
    case PushStmt(v) =>
      IndentWriter.writeIndent(indent)
      fout.println("PushStmt")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(v, nextindent)
    case SplitStmt(s) =>
      IndentWriter.writeIndent(indent)
      fout.print("SplitStmt ")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(s, nextindent)
    case JoinStmt(s) =>
      IndentWriter.writeIndent(indent)
      fout.print("JoinStmt ")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(s, nextindent)
    case DeclStmt(l) =>
      if(!l.decls.isEmpty) {
        for (parm <- l.decls)
          printvisit(parm, indent)
      }
    case ForStmt(ini, cond, inc, body) =>
      IndentWriter.writeIndent(indent)
      fout.println("ForStmt")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(ini, nextindent)
      printvisit(cond, nextindent)
      printvisit(inc, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(body, nextindent)
    case IfStmt(cond, i, e) =>
      IndentWriter.writeIndent(indent)
      fout.println("IfStmt")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(cond, nextindent)
      printvisit(i, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(e, nextindent)
    case WhileStmt(c, b) =>
      IndentWriter.writeIndent(indent)
      fout.println("WhileStmt")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(c, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(b, nextindent)
    case ReturnStmt(e) =>
      IndentWriter.writeIndent(indent)
      fout.println("ReturnStmt")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(e, nextindent)
    case DoWhileStmt(b, c) =>
      IndentWriter.writeIndent(indent)
      fout.println("DoWhileStmt")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(b, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(c, nextindent)
    case BreakStmt() =>
      IndentWriter.writeIndent(indent)
      fout.println("BreakStmt")
    case ContinueStmt() =>
      IndentWriter.writeIndent(indent)
      fout.println("ContinueStmt")
    case CommaStmt() =>
      IndentWriter.writeIndent(indent)
      fout.println("CommaStmt")
    case EnqueueStmt(v) =>
      IndentWriter.writeIndent(indent)
      fout.println("EnqueueStmt")
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(v, nextindent)
    case MsgStmt(bid, id, p, min, max) =>
      IndentWriter.writeIndent(indent)
      fout.print("MsgStmt ")
      //printPos(bid.pos)
      fout.print(" " + bid.lexeme + " ")
      //printPos(id.pos)
      fout.print(" " + id.lexeme + " ")
      fout.print("(")
      if(p.exprs.isEmpty){
        fout.print("void")//TBD.
      }
      p.exprs.zipWithIndex foreach { case(parm, idx) =>
        fout.print(parm)
        if(idx != p.exprs.length-1) fout.print(", ")
      }
      fout.println(")'")
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(min, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(max, nextindent)
    case NoStmt() =>
      IndentWriter.writeIndent(indent)
      fout.println("NoStmt")
    case RRSpec(d) =>
      fout.print("'(")
      if(d.exprs.isEmpty)
        fout.print("void")
      else
        d.exprs.zipWithIndex foreach { case(parm, idx) =>
          fout.print(parm)
          if(idx != d.exprs.length-1) fout.print(" ")
        }
      fout.println(")'")
    case DuplicateSpec() =>
      fout.println("DuplicateSpec ")
    case id: ID =>
      IndentWriter.writeIndent(indent)
      fout.print("ID ");
      //printPos(id.pos);
      fout.print(" " + id.lexeme)
      if(id.evalType.isDefined)
        fout.print(", evalType = " + id.evalType)
      if(id.promoteToType.isDefined)
        fout.print(", promoteToType = " + id.promoteToType)
      fout.println
    case StringLiteral(p, l) =>
      IndentWriter.writeIndent(indent)
      fout.print("StringLiteral ");
      printPos(p);
      fout.println(" " + l)
    case intlit: IntLiteral =>
      IndentWriter.writeIndent(indent)
      fout.print("IntLiteral ");
      printPos(intlit.pos);
      fout.print(" " + intlit.value)
      if(intlit.evalType.isDefined)
        fout.print(", evalType = " + intlit.evalType)
      if(intlit.promoteToType.isDefined)
        fout.print(", promoteToType = " + intlit.promoteToType)
      fout.println
    case floatlit:FloatLiteral =>
      IndentWriter.writeIndent(indent)
      fout.print("FloatLiteral ");
      printPos(floatlit.pos);
      fout.print(" " + floatlit.value)
      if(floatlit.evalType.isDefined)
        fout.print(", evalType = " + floatlit.evalType)
      if(floatlit.promoteToType.isDefined)
        fout.print(", promoteToType = " + floatlit.promoteToType)
      fout.println
    case BooleanLiteral(p, l, v) =>
      IndentWriter.writeIndent(indent)
      fout.print("BooleanLiteral ");
      printPos(p);
      fout.println(" " + v)
    case ComplexLiteral(p, l, v) =>
      IndentWriter.writeIndent(indent)
      fout.print("ComplexLiteral ");
      printPos(p);
      fout.println(" " + v)
    case PiLiteral(p, l, v) =>
      IndentWriter.writeIndent(indent)
      fout.print("PiLiteral ");
      printPos(p);
      fout.println(" " + v)
    case TernaryExpr(opc, e1, e2, e3) =>
      IndentWriter.writeIndent(indent)
      fout.println("TernaryExpr '" + opc.toString + "'");
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(e1, nextindent)
      printvisit(e2, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(e3, nextindent)
    case bop:BinaryExpr =>
      IndentWriter.writeIndent(indent)
      fout.print("BinaryExpr '" + bop.opc.toString)
      if(bop.evalType.isDefined)
        fout.print(", evalType = " + bop.evalType)
      if(bop.promoteToType.isDefined)
        fout.print(", promoteToType = " + bop.promoteToType)
      fout.println("'");
      val nextindent = IndentWriter.activateColumn(indent+1)
      printvisit(bop.lhs, nextindent)
      IndentWriter.expireColumn(nextindent)
      printvisit(bop.rhs, nextindent)
    case uop:UnaryExpr =>
      IndentWriter.writeIndent(indent)
      fout.print("UnaryExpr '" + uop.opc.toString)
      if(uop.evalType.isDefined)
        fout.print(", evalType = " + uop.evalType)
      if(uop.promoteToType.isDefined)
        fout.print(", promoteToType = " + uop.promoteToType)
      fout.println("'");
      val nextindent = IndentWriter.activateExpiredColumn(indent+1)
      printvisit(uop.input, nextindent)
    case QualType(v, i, p) =>
      IndentWriter.writeIndent(indent)
      fout.print("QualType ")
      printPos(p)
      fout.print(" ")
      if(!i) fout.print("!")
      fout.print("const")
      fout.println(" " + v + " ")
      
    case t: Any =>
      //Boilerplate:
      IndentWriter.writeIndent(indent)
      fout.println("<unmatched: " + t)
      reportWarning("Printvisit unmatched node: " + t)
      //trav.visit(t, indent, printvisit)
  }
}
