package sem

class StaticScope(parent : Option[Scope]) extends BaseScope(parent){
  scopeName = Some("static")
}