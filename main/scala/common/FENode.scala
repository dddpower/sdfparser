//package common
//
//abstract class FENode(rRanges:RateRanges) {
//  /**
// * Any node in the tree created by the front-end's parser.  This is
// * the root of the front-end class tree.  Derived classes include
// * statement, expression, and stream object nodes.
// *
// * @author  David Maze &lt;dmaze@cag.lcs.mit.edu&gt;
// * @version $Id: FENode.java,v 1.2 2003-10-09 19:50:59 dmaze Exp $
// */
//  //private FEContext context;
//  
//  // Yousun Ko: a pair of ranges for numbers of push and pop operations
//  var _rateRanges = rRanges
//  
///**
//   * Create a new node with the specified context.
//   *
//   * @param context  file and line number for the node
//   */
// 
//  /**
//   * Returns the context associated with this node.
//   *
//   * @return context object with file and line number
//   */
////    public FEContext getContext()
////    {
////        return context;
////    }
//
//
//  // get and set RateRanges
//  def rateRanges = _rateRanges
//
//  def rateRanges_=(value: RateRanges) = _rateRanges = value
//
//  /**
//   * Calls an appropriate method in a visitor object with this as
//   * a parameter.
//   *
//   * @param v  visitor object
//   * @return   the value returned by the method corresponding to
//   *           this type in the visitor object
//   */
//  def accept(v : FEVisitor)
//}