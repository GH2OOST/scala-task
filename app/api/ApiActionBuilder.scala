package api

import javax.inject.Inject

import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


trait ApiRequestHeader
  extends MessagesRequestHeader
    with PreferredMessagesProvider

class ApiRequest[A](request: Request[A], val messagesApi: MessagesApi)
  extends WrappedRequest(request)
    with ApiRequestHeader


trait RequestMarkerContext {

  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(
                                             implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress)
    }
  }

}

class ApiActionBuilder @Inject()(messagesApi: MessagesApi,
                                 playBodyParsers: PlayBodyParsers)(
                                  implicit val executionContext: ExecutionContext)
  extends ActionBuilder[ApiRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type RequestBlock[A] = ApiRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A],
                              block: RequestBlock[A]): Future[Result] = {
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request)
    logger.trace(s"invokeBlock: ")

    val future = block(new ApiRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case _ =>
          result
      }
    }
  }
}

case class ApiControllerComponents @Inject()(apiActionBuilder: ApiActionBuilder,
                                             apiResourceHandler: ApiResourceHandler,
                                             actionBuilder: DefaultActionBuilder,
                                             parsers: PlayBodyParsers,
                                             messagesApi: MessagesApi,
                                             langs: Langs,
                                             fileMimeTypes: FileMimeTypes,
                                             executionContext: scala.concurrent.ExecutionContext)
  extends ControllerComponents


class ApiBaseController @Inject()(components: ApiControllerComponents)
  extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = components

  def ApiAction: ApiActionBuilder = components.apiActionBuilder

  def apiResourceHandler: ApiResourceHandler = components.apiResourceHandler
}
