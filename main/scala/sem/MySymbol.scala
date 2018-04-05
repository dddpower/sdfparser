package sem

// A generic programming language symbol
class MySymbol(argname: String, argtype: Option[MyType]){
  var name = argname  // All symbols at least have a name
  var tYpe = argtype
  var scope : Option[Scope] = None  // All symbols know what scope contains them
  
  def this(name : String) = {
    this(name, None)
  }
//  def this(name : Option[String], tYpe : Option[MyType]) = {
//    this(name)
//    this.tYpe = tYpe 
//  }
  def getName = name
  
  override def toString() = if(scope!= None)
      '<'+ scope.get.scopeName.get+"."+getName+":"+tYpe+'>'
    else 
      '<'+ "None"+"."+getName+":"+tYpe+'>'
  object MySymbol {
    def stripBrackets(s: String) = s.substring(0,s.length);
  }
}