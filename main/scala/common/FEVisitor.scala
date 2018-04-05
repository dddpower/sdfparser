package common
/**
 * Visitor interface for StreamIt front-end nodes.  This class
 * implements part of the "visitor" design pattern for StreamIt
 * front-end nodes.  The pattern basically exchanges type structures
 * for function calls, so a different function in the visitor is
 * called depending on the run-time type of the object being visited.
 * Calling visitor methods returns some value, the type of which
 * depends on the semantics of the visitor in question.  In general,
 * you will create a visitor object, and then pass it to the
 * <code>FENode.accept()</code> method of the object in question.
 *
 * @author  David Maze &lt;dmaze@cag.lcs.mit.edu&gt;
 * @version $Id: FEVisitor.java,v 1.19 2006-08-23 23:01:08 thies Exp $
 */


//    def Object visitExprArray(ExprArray exp);
//    def Object visitExprArrayInit(ExprArrayInit exp);
//    def Object visitExprBinary(ExprBinary exp);
//    def Object visitExprComplex(ExprComplex exp);
//    def Object visitExprComposite(ExprComposite exp);
//    def Object visitExprConstBoolean(ExprConstBoolean exp);
//    def Object visitExprConstChar(ExprConstChar exp);
//    def Object visitExprConstFloat(ExprConstFloat exp);
//    def Object visitExprConstInt(ExprConstInt exp);
//    def Object visitExprConstStr(ExprConstStr exp);
//    def Object visitExprDynamicToken(ExprDynamicToken exp);
//    def Object visitExprField(ExprField exp);
//    def Object visitExprFunCall(ExprFunCall exp);
//    def Object visitExprHelperCall(ExprHelperCall exp);
//	def Object visitExprIter(ExprIter exprIter);
//    def Object visitExprPeek(ExprPeek exp);
//    def Object visitExprPop(ExprPop exp);
//    def Object visitExprRange(ExprRange exp);
//    def Object visitExprTernary(ExprTernary exp);
//    def Object visitExprTypeCast(ExprTypeCast exp);
//    def Object visitExprUnary(ExprUnary exp);
//    def Object visitExprVar(ExprVar exp);
//    def Object visitFieldDecl(FieldDecl field);
//    def Object visitFunction(Function func);
//    def Object visitFuncWork(FuncWork func);
//    def Object visitProgram(Program prog);
//    def Object visitSCAnon(SCAnon creator);
//    def Object visitSCSimple(SCSimple creator);
//    def Object visitSJDuplicate(SJDuplicate sj);
//    def Object visitSJRoundRobin(SJRoundRobin sj);
//    def Object visitSJWeightedRR(SJWeightedRR sj);
//    def Object visitStmtAdd(StmtAdd stmt);
//    def Object visitStmtAssign(StmtAssign stmt);
//    def Object visitStmtBlock(StmtBlock stmt);
//    def Object visitStmtBody(StmtBody stmt);
//    def Object visitStmtBreak(StmtBreak stmt);
//    def Object visitStmtContinue(StmtContinue stmt);
//    def Object visitStmtDoWhile(StmtDoWhile stmt);
//    def Object visitStmtEnqueue(StmtEnqueue stmt);
//    def Object visitStmtEmpty(StmtEmpty stmt);
//    def Object visitStmtFor(StmtFor stmt);
//    def Object visitStmtExpr(StmtExpr stmt);
//    def Object visitStmtIfThen(StmtIfThen stmt);
//    def Object visitStmtJoin(StmtJoin stmt);
//    def Object visitStmtLoop(StmtLoop stmt);
//    def Object visitStmtPush(StmtPush stmt);
//    def Object visitStmtReturn(StmtReturn stmt);
//    def Object visitStmtSendMessage(StmtSendMessage stmt);
//    def Object visitStmtHelperCall(StmtHelperCall stmt);
//    def Object visitStmtSplit(StmtSplit stmt);
//    def Object visitStmtVarDecl(StmtVarDecl stmt);
//    def Object visitStmtWhile(StmtWhile stmt);
//    def Object visitStreamSpec(StreamSpec spec);
//    def Object visitStreamType(StreamType type);
//    def Object visitOther(FENode node);
