package com.cesar.royaltymanager.infrastructure.repository.inmemory

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.studios.{Studio, StudioRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class StudioRepositoryInterpreter[F[_]: Applicative](private val cache: TrieMap[GUID, Studio])
    extends StudioRepositoryAlgebra[F] {

  override def get(id: GUID): OptionT[F, Studio] = OptionT.fromOption[F](cache.get(id))

  // Used for test. Functionality not exposed in the API
  override def create(studio: Studio): F[Studio] = {
    cache.put(studio.id, studio)
    studio.pure[F]
  }

}

object StudioRepositoryInterpreter {
  def apply[F[_]: Applicative](cache: TrieMap[GUID, Studio]): StudioRepositoryInterpreter[F] =
    new StudioRepositoryInterpreter(cache)
}
