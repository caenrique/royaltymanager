package com.cesar.royaltymanager.infrastructure.repository.inmemory

import cats.Applicative
import cats.data.OptionT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.studios.{Studio, StudioRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class StudioRepositoryInMemoryInterpreter[F[_]: Applicative](
    private val cache: TrieMap[GUID, Studio])
    extends StudioRepositoryAlgebra[F] {

  override def get(id: GUID): OptionT[F, Studio] = OptionT.fromOption[F](cache.get(id))
}

object StudioRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative](
      cache: TrieMap[GUID, Studio]): StudioRepositoryInMemoryInterpreter[F] =
    new StudioRepositoryInMemoryInterpreter(cache)
}
