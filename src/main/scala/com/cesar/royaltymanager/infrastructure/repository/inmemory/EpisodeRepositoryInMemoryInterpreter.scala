package com.cesar.royaltymanager.infrastructure.repository.inmemory

import cats.Applicative
import cats.data.OptionT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.episodes.{Episode, EpisodeRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class EpisodeRepositoryInMemoryInterpreter[F[_]: Applicative](
    private val cache: TrieMap[GUID, Episode])
    extends EpisodeRepositoryAlgebra[F] {
  override def get(episodeId: GUID): OptionT[F, Episode] = OptionT.fromOption(cache.get(episodeId))
}

object EpisodeRepositoryInMemoryInterpreter {
  def apply[F[_]: Applicative](
      cache: TrieMap[GUID, Episode]): EpisodeRepositoryInMemoryInterpreter[F] =
    new EpisodeRepositoryInMemoryInterpreter(cache)
}
