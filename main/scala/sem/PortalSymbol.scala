package sem

class PortalSymbol(name : String, idd : String) 
  extends VariableSymbol(name, Some(SymbolTable._portal)) {
  val id = idd
}