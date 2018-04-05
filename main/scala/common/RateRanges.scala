package common

/**
 * class for a pair of ranges denoting push and pop rate ranges
 */
class RateRanges(_popRange: Range, _pushRange: Range) {
  def popRange = _popRange
  def pushRange = _pushRange
  
  def this() = this(new Range(),new Range())
  
  def isZero = 
    if (popRange.isZero && pushRange.isZero)
      true
    else false

    def add(rhs:RateRanges) : RateRanges = 
    {
        val result = new RateRanges(
                                popRange.add(rhs.popRange), 
                                pushRange.add(rhs.pushRange))
        result
    }

    def subtract(rhs : RateRanges) : RateRanges = 
    {
        val result = new RateRanges(
                                popRange.subtract(rhs.popRange), 
                                pushRange.subtract(rhs.pushRange))
        result
    }

    def join(rhs : RateRanges) : RateRanges =
    {
        val result = new RateRanges(
                                popRange.join(rhs.popRange), 
                                pushRange.join(rhs.pushRange))
        result
    }

    def meet(rhs : RateRanges) : RateRanges =
    {
        val result = new RateRanges(
                                popRange.meet(rhs.popRange), 
                                pushRange.meet(rhs.pushRange))
        return result
    }

    override def toString = {
        var result = ""
        result+="[pop:"+popRange.toString()+","
        result+="push:"+pushRange.toString()+"]"
        result
    }
}