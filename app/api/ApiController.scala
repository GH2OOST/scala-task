package api

import java.util.UUID

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class PhonesForm(phoneNumber: String, name: String)

class ApiController @Inject()(cc: ApiControllerComponents)(
  implicit ec: ExecutionContext)
  extends ApiBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PhonesForm] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "phoneNumber" -> text,
        "name" -> text
      )(PhonesForm.apply)(PhonesForm.unapply)
    )
  }

  def index: Action[AnyContent] = ApiAction.async { implicit request =>
    logger.trace("index: ")
    apiResourceHandler.find.map { phones =>
      Ok(Json.toJson(phones))
    }
  }

  def create: Action[AnyContent] = ApiAction.async { implicit request =>
    logger.trace("create: ")
    processJsonPost()
  }

  def changeById(id: String): Action[AnyContent] = ApiAction.async {
    implicit request =>
      logger.trace(s"changeById: id = $id")
      apiResourceHandler.update(UUID.fromString(id)).map {
        case None => NoContent
        case Some(uuid) => Ok(Json.toJson(uuid))
      }
  }

  def deleteById(id: String): Action[AnyContent] = ApiAction.async {
    implicit request =>
      logger.trace(s"deleteById: id = $id")
      apiResourceHandler.delete(UUID.fromString(id)).map {
        case None => NoContent
        case Some(uuid) => Ok(Json.toJson(uuid))
      }
  }

  def searchByName: Action[AnyContent] = ApiAction.async {
    implicit request =>
      logger.trace(s"searchByName")
      val maybeSubstring = request.queryString.get("nameSubstring")
      maybeSubstring match {
        case None => Future.successful(BadRequest("Should contains nameSubstring"))
        case Some(values) => apiResourceHandler.searchByName(values.head).map {
          phones => Ok(Json.toJson(phones))
        }
      }
  }

  def searchByNumber: Action[AnyContent] = ApiAction.async {
    implicit request =>
      logger.trace(s"searchByNumber")
      val maybeSubstring = request.queryString.get("phoneSubstring")
      maybeSubstring match {
        case None => Future.successful(BadRequest("Should contains phoneSubstring"))
        case Some(values) => apiResourceHandler.searchByPhone(values.head).map {
          phones => Ok(Json.toJson(phones))
        }
      }
  }

  private def processJsonPost[A]()(
    implicit request: ApiRequest[A]): Future[Result] = {
    def failure(badForm: Form[PhonesForm]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PhonesForm) = {
      apiResourceHandler.create(input).map { id =>
        Created(id.toString)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
