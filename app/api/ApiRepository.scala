package api

import java.util.UUID

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class CatalogueEntity(id: UUID, phoneNumber: String, name: String)

class ExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")


trait InMemoryPhoneRepository {
  def createOrUpdate(data: CatalogueEntity)(implicit mc: MarkerContext): Future[UUID]

  def getAll()(implicit mc: MarkerContext): Future[Iterable[CatalogueEntity]]

  def get(id: UUID)(implicit mc: MarkerContext): Future[Option[CatalogueEntity]]

  def delete(id: UUID)(implicit mc: MarkerContext): Future[Option[UUID]]
}

@Singleton
class InMemoryPhoneRepositoryImpl @Inject()()(implicit ec: ExecutionContext)
  extends InMemoryPhoneRepository {

  private val logger = Logger(this.getClass)

  private var data = Map[UUID, CatalogueEntity]()

  override def getAll()(
    implicit mc: MarkerContext): Future[Iterable[CatalogueEntity]] = {
    Future {
      logger.trace(s"getAll: ")
      data.values
    }
  }

  override def get(id: UUID)(
    implicit mc: MarkerContext): Future[Option[CatalogueEntity]] = {
    Future {
      logger.trace(s"get: id = $id")
      data.get(id)
    }
  }

  override def createOrUpdate(entity: CatalogueEntity)(
    implicit mc: MarkerContext): Future[UUID] =
    Future {
      logger.trace(s"created or updated: data = $entity")
      data += (entity.id -> entity)
      entity.id
    }

  override def delete(id: UUID)(
    implicit mc: MarkerContext): Future[Option[UUID]] = {
    Future {
      logger.trace(s"delete: data = $id")
      val uuid = Some(id).filter(data.contains)
      data -= id
      uuid
    }
  }
}
