package common

import scala.collection.mutable
import scala.util.control.Breaks._
import scanner.SDFUtils
import scanner.SDFLexical
import ast._
import graph._

object SDFCmdLineParsers {
  private abstract class OptionInfo {
    def GetOption : Any
    def isRequested : Boolean = { return false }
  }
  private case class IntOptionInfo (arg : Int) extends OptionInfo {
    val value : Int = arg
    override def GetOption : Int = { return value }
    override def isRequested : Boolean = { return true } 
  }
  private case class StringOptionInfo (arg : String) extends OptionInfo {
    val value : String = arg;
    override def GetOption : String = { return value }
    override def isRequested : Boolean = { return true }
  }
  private case class NoOptionInfo (arg: Boolean = false) extends OptionInfo {
    override def GetOption : Any = {
      throw new Exception("Do not call me")//It may not nice way.
    }
    override def isRequested : Boolean = { return arg }
  }
  
  private val DumpTokens : Int = 0
  private val DumpAst : Int = 1
  private val DumpSDFGraph : Int = 2
  private val DumpGrammarErrors : Int = 3
  private val OptimizationLevel : Int = 4
  private val LastOption : Int = OptimizationLevel
  private var srcFilePath : String = ""
  
  private var OptionInfos = 
  Array.fill[OptionInfo]( LastOption + 1 )( new NoOptionInfo )

  //Set options with arguments
  private def setOptions(args: Array[String], 
                           idxArg : Int, 
                           idxTokenID : Int) {
    if(args.length > idxArg + 1) {
      if(!OptionInfos(idxTokenID).isRequested) {
        OptionInfos(idxTokenID) = new StringOptionInfo(args(idxArg+1))
      } else {
        SDFMessageUtils.printDupOptions(args(idxArg))
        System.exit(1)
      }
    } else {
      SDFMessageUtils.printInvalidOptionValue(args(idxArg))
      System.exit(1)
    }
  }

  //Set options with no arguments
  private def setNoOptions(args: Array[String], 
                           idxArg : Int,
                           idxTokenID : Int) {
    if(!OptionInfos(idxTokenID).isRequested) {
      OptionInfos(idxTokenID) = new NoOptionInfo(true)
    } else {
      SDFMessageUtils.printDupOptions(args(idxArg))
      System.exit(1)
    }
  }

  def isDumpAst : Boolean = {
    return OptionInfos(DumpAst).isRequested;
  }
  def isDumpTokens : Boolean = {
    return OptionInfos(DumpTokens).isRequested;
  }
  def isDumpSDFGraph : Boolean = {
    return OptionInfos(DumpSDFGraph).isRequested;
  }
  def isDumpGrammarErrors : Boolean = {
    return OptionInfos(DumpGrammarErrors).isRequested;
  }
  def isOptLevel : Boolean = {
    return OptionInfos(OptimizationLevel).isRequested;
  }
  def getTokenFile(sdfLexical : SDFLexical) {
    if(!OptionInfos(DumpTokens).isRequested) {
      //Error
      SDFMessageUtils.printNotReadyOption("dumpTokens")
      System.exit(1)
    }
    val sdfUtil = new SDFUtils
    sdfUtil.printoutAllTokens(sdfLexical, srcFilePath, 
                              OptionInfos(DumpTokens).GetOption.toString())
  }
  def getASTFile(node: Any) = {
    if(!OptionInfos(DumpAst).isRequested) {
      //Error
      SDFMessageUtils.printNotReadyOption("dumpAST")
      System.exit(1)
    }
    ASTPrinter.printAST(srcFilePath,
                        OptionInfos(DumpAst).GetOption.toString(),
                        node)
  }
  def getSDFGraph() = {
    if(!OptionInfos(DumpSDFGraph).isRequested) {
      //Error
      SDFMessageUtils.printNotReadyOption("dumpSDFGraph")
      System.exit(1)
    }
    //StreamGraph.printStreamGraph(OptionInfos(DumpSDFGraph).GetOption.toString())
  }
  def getSrcOfPath : String = {
    if ( srcFilePath.isEmpty() ){
      SDFMessageUtils.printNotHasbeenCalledCmdParse
      System.exit(1)
    }
    return srcFilePath
  }
  def getSrcFileName: String = {
    if ( srcFilePath.isEmpty() ){
      SDFMessageUtils.printNotHasbeenCalledCmdParse
      System.exit(1)
    }
    val pathcmps = srcFilePath.split('/')
    return pathcmps.last
  }
  def processCmdLine(args: Array[String]) {
    // n of index -> input file
    // i of index -> options [help or dumpTokens]
    // i+1 of index -> output file
    // ex ) sdfparser in.str -dumpTokens foo.txt
    //      sdfparser -dumpTokens foo.txt in.str
    // (n and i are closed but sequence is not bother)
    var idx : Int = 0
    var idxofSrcfile : Int = -1
    
    //Check no input parameter.
    if(args.length == 0) {
      SDFMessageUtils.printUsageInfo(args)
      System.exit(1)
    }

    for (i <- 0 until args.length if i == idx) {
      args(i) match {
        case "-dumpTokens" => {
          setOptions(args, idx, DumpTokens)
          idx += 1
        }
        case "-dumpAST" => {
          setOptions(args, idx, DumpAst)
          idx += 1
        }
        case "-dumpSDFGraph" => {
          setOptions(args, idx, DumpSDFGraph)
          idx += 1
        }
        case "-dumpGrammarErrors" => {
          setNoOptions(args, idx, DumpGrammarErrors)
        }
        case "-help" => {
          SDFMessageUtils.printUsageInfo(args)
          System.exit(1)
        }
        case _ => {
          if (idxofSrcfile == -1) {
            idxofSrcfile = idx
            srcFilePath = args(idxofSrcfile)
            //Check There is an src file.
            if (new java.io.File(args(idxofSrcfile)).exists() == false) {
              SDFMessageUtils.printCheckFile
              System.exit(1)
            }
          } else {
            //Unexpected input.
            SDFMessageUtils.printInvalidCmd(args(i))
            System.exit(1)
          }
        }
      }
      idx += 1
    }
    if (idxofSrcfile == -1) {
      //There is no src file.
      SDFMessageUtils.printNoSrcFile
      System.exit(1)
    }
  }
}
