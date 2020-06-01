package api

import java.util.UUID

import javax.inject.Inject
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

case class PhoneDto(id: UUID, phoneNumber: String, name: String)

object PhoneDto {
  implicit val format: Format[PhoneDto] = Json.format
}


class ApiResourceHandler @Inject()(phoneRepository: InMemoryPhoneRepository)(implicit ec: ExecutionContext) {
  def create(postInput: PhonesForm)(
    implicit mc: MarkerContext): Future[UUID] = {
    val data = CatalogueEntity(UUID.randomUUID(), postInput.phoneNumber, postInput.name)
    phoneRepository.createOrUpdate(data)
  }

  def update(id: UUID)(
    implicit mc: MarkerContext): Future[Option[UUID]] = {
    val catalogueEntity = phoneRepository.get(id)
    catalogueEntity.map { maybePostData =>
      maybePostData.map { postData =>
        phoneRepository.createOrUpdate(postData).map(x => x)
      }
    }.flatMap {
      case None => Future.successful(None)
      case Some(f) => f.map(Some(_))
    }
  }

  def delete(id: UUID)(
    implicit mc: MarkerContext): Future[Option[UUID]] = {
    phoneRepository.delete(id)
  }

  def find(implicit mc: MarkerContext): Future[Iterable[PhoneDto]] = {
    phoneRepository.getAll().map { catalogueEntity =>
      catalogueEntity.map(createResource)
    }
  }

  def searchByName(nameSubstring: String)(implicit mc: MarkerContext): Future[Iterable[PhoneDto]] = {
    phoneRepository.getAll().map { entities =>
      entities.filter(e => e.name.contains(nameSubstring)).map(createResource)
    }
  }

  def searchByPhone(phoneSubstring: String)(implicit mc: MarkerContext): Future[Iterable[PhoneDto]] = {
    phoneRepository.getAll().map { entities =>
      entities.filter(e => e.phoneNumber.contains(phoneSubstring)).map(createResource)
    }
  }

  private def createResource(p: CatalogueEntity): PhoneDto = {
    PhoneDto(p.id, p.phoneNumber, p.name)
  }
}
