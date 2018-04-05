package sem
import common.Counter
class LocalScope(parent : Option[Scope]) extends BaseScope(parent) {
  scopeName = Some("local"+ Counter.getNumber)
}