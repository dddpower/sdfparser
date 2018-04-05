package graph

import scalax.collection.Graph
import scalax.collection.GraphPredef._, scalax.collection.GraphEdge._
import scalax.collection.io.dot._
import scalax.collection.edge.LDiEdge, scalax.collection.edge.Implicits._
import implicits._
import java.io._
import ast._
import common.WritingTool._

object Counter {
  var number: Int = 0
  def reSet = {
    //reset number to 0
    number = 0
  }
  def getNumber = { //get and automatically increase number by 1
    number = number + 1
    number
  }
}

case class FilterNode (
    var decl: Option[FilterSpec],
    name: String,
    inT : String,
    outT : String,
    pushrate:Option[Int],
    poprate:Option[Int],
    peekrate: Option[Int]) {
  def toSDF(incoming : Set[String], outgoing : Set[String]) : String = {
    def fixID(node:ID) : String = node.toString() + "_" + name
    def trickVarDeclL(node: VarDeclList, extra: String) : String = {
      val str = node.vardecls.map(f=>f.id.toString()+"_"+extra).mkString(endStmt)
      if(str=="")
        str
      else str+ endStmt
    }
    
    if(decl.isEmpty)
      ""
    else {
      val fildecl = decl.get
      var closeflag = true
      actor + this.name + newLine + openBlock + 
      //state,init,input,firing,output
      //init : from initfunciton
      //input,firing,output : from workfunciton
      state + newLine + openBlock + {
      var str = fildecl.parmvardecls.extractSDF
      if(str!="")str +=endStmt
      str
      } +
      fildecl.body.decls.decls.map(f=> f match {
        case vdl:VarDeclList=>
          closeflag = false
          trickVarDeclL(vdl, fildecl.id.toString) + closeBlock
        case initf: InitFunctionDecl=>
          val cb = if(closeflag==true) closeBlock else ""
          cb + init + newLine +
          initf.Body.stmts.map(f=> f match {
            case id:ID => fixID(id)
            case _=> f.extractSDF
            }
          )
        case workf: WorkFunctionDecl=>
          def writeInOutput(sym: String, num: Int) : String = {
    	      def rep(acc: String, counter: Int) : String = {
    	        if(counter>=num) //exit condition
    	          acc + counter
    	        else //rep condition
    	          rep(acc + counter + ", " + sym, counter+1)
    	      }
    	      rep(sym,1)
    	    }
          val cb = if(closeflag==true)
            closeBlock
          else ""
            val push = this.pushrate.getOrElse(0)
            val pop = this.poprate.getOrElse(0)
          cb + {
            pop match {
              case 0 => newLine
              case _=>
                newLine + input + newLine +
                incoming.head + ": " + writeInOutput("x",pop) + endStmt
            }
          } + 
          firing + newLine + 
          workf.extractSDF + {
            push match {
              case 0 => ""
              case _=> newLine + output + newLine +
              outgoing.head + ": " + writeInOutput("y",pop) + endStmt
            }
          }
        case _=> f.toString() + "is an exceptional case" + newLine
      }).mkString(newLine) +
      closeBlock
    }
  }
}
object GraphSpace {
  val nullactorspec = ("","")
  
  abstract class StreamGraph {
    val root = DotRootGraph(directed = true,
      id = Some("g"))
    def edgeTransformer(innerEdge: Graph[FilterNode, LDiEdge]#EdgeT): 
    Option[(DotGraph, DotEdgeStmt)] = innerEdge.edge match {
      case LDiEdge(source, target, label) => label match {
        case label: String =>
        Some((root,
          DotEdgeStmt(source.toString, target.toString,
              if (label.nonEmpty) List(DotAttr("label", label.toString))
              else                Nil)))
      }
    }
  
    var body: Graph[FilterNode, LDiEdge]
    var headvar: Option[FilterNode] = None
    var tailvar: Option[FilterNode] = None
//    def genStr : String = {
//      body.edges.iterator.map(f=>
//        f._1.value.decl.iotype.OutT.toString + " " + 
//        f._1.value.decl.id + "->" + 
//        f._2.value.decl.id).mkString(";\n")
//    }
    def toDotFile(filename: String) = {
      val content = body.toDot(root, edgeTransformer)
      // FileWriter
      val file = new File(filename)
      val bw = new BufferedWriter(new java.io.FileWriter(file))
      bw.write(content)
      bw.close()
    }
  }

  
//  class Identity(in_out: String) extends Filter {
//    def apply = new Filter((in_out, in_out), "Identity")
//  }
//  class FileReader(in_out: String) extends Filter {
//    def apply = new Filter((in_out, in_out), "FileReader")
//  }
//  class FileWriter(in_out: String) extends Filter {
//    def apply = new Filter((in_out, in_out), "FileWriter")
//  }
  
  class PipeLine extends StreamGraph {
    override var body = Graph[FilterNode, LDiEdge]()
    
    def add(target: FilterNode): Unit = {
      body += target
      
      if (headvar.isDefined) { //head already exists => headvar String fixed & addEdge
        val newedge = 
          LDiEdge(tailvar.get, target)(tailvar.get.outT)
        body += newedge
      } 
      //add target from empty pipeline
      else headvar = Some(target)
  
      //in both(empty head, existing head) case, tailvar sets to target.tailvar
      tailvar = Some(target)
    }
    def add(target: StreamGraph): Unit = {
      body = body union target.body
  
      if (headvar.isDefined) { //head already exists => headvar String fixed & addEdge
        val newedge = 
          LDiEdge(tailvar.get, target.headvar.get)(tailvar.get.outT)
        body += newedge
      } //add streamgraph from empty pipeline
      else headvar = target.headvar
  
      //in both case, tailvar sets to target.tailvar
      tailvar = target.tailvar
    }
  }
  
  //head, and tail are fixed to split and join node 
  class SplitJoin(id:String) extends StreamGraph {
    def this() = this("AnonFilter"+Counter.getNumber)
    val split = FilterNode(None,id + "_split", "", "",None,None,None)
    val join = FilterNode(None,id + "_join", "", "",None,None,None)
    headvar = Some(split)
    tailvar = Some(join)
    override var body = Graph[FilterNode, LDiEdge](headvar.get, tailvar.get)
    
    def add(target: FilterNode) : Unit = {
      body += target
      body += LDiEdge(headvar.get, target)(headvar.get.outT)
      body += LDiEdge(target, tailvar.get)(target.outT)
    }
    
    def add(target: StreamGraph): Unit = {
      body = body union target.body
      body += LDiEdge(headvar.get, target.headvar.get)(headvar.get.outT)
      body += LDiEdge(target.tailvar.get, tailvar.get)(target.tailvar.get.outT)
    }
  }
}