import os
import re
import sys, getopt
import fnmatch
import commands
import subprocess


scalaProgram = "scala sdfparser -dumpGrammarErrors"
#scalaProgram = "scala -J-Xss1024m sdfparser -dumpGrammarErrors"
scalaArgs = ""#dumpAST, dumpToknes will be coming...
errorLogFile = "ContextFreeGrammar.log"

os.system("rm *.log")
f = open(errorLogFile,'w')

def execCmd(cmd):
  p = subprocess.Popen(cmd, 
                        shell=True, 
                        stdout=subprocess.PIPE, 
                        stderr=subprocess.PIPE)
  out, err = p.communicate(None)
  return p.returncode, out, err
  
def parseArgs(argv):
  userId = ""
  try:
    #opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
    opts, args = getopt.getopt(argv,"hi:",["userId="])
  except getopt.GetoptError:
    print "AutoRunBenchmarks.py -i <userId of Benchmarks SVN>"
    sys.exit(2)
  for opt, arg in opts:
    if opt == "-h":
      print "AutoRunBenchmarks.py -i <userId of Benchmarks SVN>"
      sys.exit()
    elif opt == "-i":
      userId = str(arg)
  return userId

def downBenchmarks(userId):
  if not userId: 
    os.system("svn co svn+ssh://elc.cs.yonsei.ac.kr/var/repos/benchmarks" + 
              "/streamit ./benchmarks")
  else:
    os.system("svn co svn+ssh://" + userId + 
              "@elc.cs.yonsei.ac.kr/var/repos/benchmarks/streamit ./benchmarks")

def walkBenchList():
  for root, dirs, files in os.walk("benchmarks"):
    path = root
    for file in fnmatch.filter(files, "*.str"):
        varFile = path + "/" + file
        varCmd = "cd ./../; " + scalaProgram + " ./script/" + varFile + ";"
        #print varFile, varCmd
        print "Running... [ ", varCmd, " ]"
        ecode, out, emsg = execCmd(varCmd)
        if not ecode == 0:#Occur Error
          saveLog(varFile, varCmd, out, emsg)

def saveLog(varFile, varCmd, stdout, errmsg):
  valErrMsg = varFile + errmsg
  #print "ErrorMsg", varFile, errmsg
  f.write("ErrorMsg : \n")
  f.write(valErrMsg)
  f.write("\n\n")


def main(argv):
  os.system("cd ./../; make; ")

  os.system("rm -rf benchmarks")

  userId = parseArgs(argv)
  downBenchmarks(userId)

  walkBenchList()
  f.close()

  os.system("rm -rf benchmarks")

if __name__ == "__main__":
   main(sys.argv[1:])



