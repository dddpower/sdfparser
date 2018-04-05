package common



class Range(_lb:Int, _ub:Int) {
  val INFTY = Int.MaxValue
  def lb = _lb
  def ub = _ub
  
  def this() = this(0,0)
  def isZero:Boolean = {
    if(ub==0 && lb == 0) true
    else false
  }
  
  //return the number of elements in the interval
  def size = {
    if(lb==INFTY || ub==INFTY) INFTY
    else if(ub==0&&lb==0) 0
    else ub-lb+1
  }
  def add(rhs: Range) : Range = {
    var newLb = 0
    var newUb = 0
    
    if(this.lb==INFTY || rhs.lb==INFTY)
      newLb = INFTY
    else {
      newLb = lb+rhs.lb
      if(newLb < 0 ||
         newLb < lb ||
         newLb < rhs.lb) {
         System.err.println("Error: Range addition exceeds limitation, "+Integer.MAX_VALUE)
         System.exit(1)
      }
    }
    if(ub==INFTY || rhs.ub==INFTY)
      newUb=INFTY
    else {
      newUb=ub+rhs.ub
      if (newUb < 0 ||    
          newUb < this.ub ||
          newUb < rhs.ub ) {
        System.err.println("Error: Range addition exceeds limitation, "+Integer.MAX_VALUE)
        System.exit(1)
      }
    }
    val result = new Range(newLb,newUb)
    result
  }
  
  def subtract(rhs : Range) : Range = {

        val rhsLb = rhs.lb
        val rhsUb = rhs.ub

        var newLb = 0
        if (this.lb == INFTY){
            newLb = INFTY
        }else if (rhsUb == INFTY) {
            newLb = 0
        }else if (rhsUb != INFTY) {
            newLb = this.lb-rhsUb 
            if (newLb<0) {
                newLb=0
            }
        }

        var newUb = 0
        if (this.ub == INFTY){
            newUb = INFTY
        }else if (rhsLb == INFTY) {
            newUb = 0
        }else if (rhsLb != INFTY) {
            newUb = this.ub-rhsLb
            if (newUb<0) {
                newUb=0
            }
        }

        val result = new Range(newLb, newUb)
        result
    }

    def join(rhs:Range) : Range = {
        var newLb = 0
        var newUb = 0

        val rhsLb = rhs.lb
        val rhsUb = rhs.ub

        if (this.lb == INFTY && rhsLb == INFTY){
            newLb = INFTY
        }else if (this.lb == INFTY && rhsLb != INFTY){
            newLb = rhsLb
        }else if (this.lb != INFTY && rhsLb == INFTY){
            newLb = this.lb
        }else if (this.lb != INFTY && rhsLb != INFTY){
            newLb = Math.min(this.lb, rhsLb)
        }

        if (this.ub == INFTY || rhsUb == INFTY){
            newUb = INFTY
        }else if (this.ub != INFTY && rhsUb != INFTY){
            newUb = Math.max(this.ub, rhsUb)
        }

        val result = new Range(newLb, newUb)
        return result
    }
     def meet(rhs : Range) : Range = {
        var newLb = 0
        var newUb = 0

        val rhsLb = rhs.lb
        val rhsUb = rhs.ub

        if (this.lb == INFTY || rhsLb == INFTY){
            newLb = INFTY
        }else if (this.lb != INFTY && rhsLb != INFTY){
            newLb = Math.max(this.lb, rhsLb)
        }

        if (this.ub == INFTY && rhsUb == INFTY){
            newUb = INFTY
        }else if (this.ub == INFTY && rhsUb != INFTY){
            newUb = rhsUb
        }else if (this.ub != INFTY && rhsUb == INFTY){
            newUb = this.ub
        }else if (this.ub != INFTY && rhsUb != INFTY){
            newUb = Math.min(this.ub, rhsUb)
        }

        val result = new Range(newLb, newUb)
        return result
    }

    override def toString = {
        var result = "["
        if (lb == INFTY){
            result+="INF"
        }else{
            result+=lb
        }
        
        result+=","
        if (ub == INFTY){
            result+="INF"
        }else{
            result+=ub
        }
        result+="]"
        result
    }
}