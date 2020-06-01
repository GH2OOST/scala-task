package api

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ApiRouter @Inject()(controller: ApiController) extends SimpleRouter {
  val prefix = "/api"

  override def routes: Routes = {
    case POST(p"/phones/createNewPhone") =>
      controller.create

    case GET(p"/phones") =>
      controller.index

    case POST(p"/phone/$id") =>
      controller.changeById(id)

    case DELETE(p"/phone/$id") =>
      controller.deleteById(id)

    case GET(p"/phones/searchByName") =>
      controller.searchByName

    case GET(p"/phones/searchByNumber") =>
      controller.searchByNumber
  }
}
