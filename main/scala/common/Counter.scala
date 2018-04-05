package common

object Counter {
  private var number : Int = 0
  def reSet = {
    //reset number to 0
    number = 0
  }
  def getNumber = { //get and automatically increase number by 1
    number = number +1
    number
  }
}