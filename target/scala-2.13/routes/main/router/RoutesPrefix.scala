// @GENERATOR:play-routes-compiler
// @SOURCE:C:/scala-project/play-scala-rest-api-example/conf/routes
// @DATE:Mon Jun 01 23:26:33 YEKT 2020


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
