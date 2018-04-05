package sem
import scala.collection.mutable.Queue

trait Scope {
	var scopeName : Option[String] = None

	/**Where to look next for symbols; **/
	var enclosingScope: Option[Scope] = None
	
	/**for printing out symbol table**/
	var surroundingScope : Option[Scope] = None
	val surroundingScopeQueue = new Queue[Scope]
	/** Define a symbol in the current scope */
	def define(sym: MySymbol): Unit
	
	/** Look up name in this scope or in enclosing scope if not here */
	def resolve(name: String): Option[MySymbol]
	/** Look up name just in this scope*/
	def resolveCurrent(name: String): Option[MySymbol]
	//def resolveStream(name: String) : Option[MySymbol] 
}