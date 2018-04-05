package common

import ast.AST

class NodeVisitor {
  
  //overide to use it properly
  protected def visit(node: Any, visitfn: Any=> AST) : AST = 
    node match {
    case ast:AST=>visitfn(node)
    case any:Any=>throw new MatchError(any)
  }
  
//  protected[scalariform] def visit[T](ast: Any, visitfn: (Any) => List[T]): List[T] = ast match {
//    case a: AstNode                => visitfn(a.immediateChildren)
//    case t: Token                  => List()
//    case Some(x)                   => visitfn(x)
//    case xs @ (_ :: _)             => xs flatMap { visitfn(_) }
//    case Left(x)                   => visitfn(x)
//    case Right(x)                  => visitfn(x)
//    case (l, r)                    => visitfn(l) ::: visitfn(r)
//    case (x, y, z)                 => visitfn(x) ::: visitfn(y) ::: visitfn(z)
//    case true | false | Nil | None => List()
//}
}