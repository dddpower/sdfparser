package graph
import scalax.collection.Graph
import scalax.collection.GraphPredef._, scalax.collection.GraphEdge._
import scalax.collection.io.dot._
import scalax.collection.edge.LDiEdge, scalax.collection.edge.Implicits._
object tester extends App {
  val g = Graph((2,2.2) ~> (3,3.3), (3,3.3) ~> (1,1.1))
  g mkString "-" // 1-2-3-2~3-3~1
  g.nodes mkString "-" // 1-2-3
  g.edges mkString "->"         // 2~3-3~1
  g.edges.iterator.foreach(f=>f._1)
}