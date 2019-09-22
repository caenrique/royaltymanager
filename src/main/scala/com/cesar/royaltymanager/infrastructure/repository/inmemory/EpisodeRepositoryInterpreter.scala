package com.cesar.royaltymanager.infrastructure.repository.inmemory

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.episodes.{Episode, EpisodeRepositoryAlgebra}

import scala.collection.concurrent.TrieMap

class EpisodeRepositoryInterpreter[F[_]: Applicative](private val cache: TrieMap[GUID, Episode])
    extends EpisodeRepositoryAlgebra[F] {
  override def get(episodeId: GUID): OptionT[F, Episode] = OptionT.fromOption(cache.get(episodeId))

  // Used for test. Functionality not exposed in the API
  override def create(episode: Episode): F[Episode] = {
    cache.put(episode.id, episode)
    episode.pure[F]
  }
}

object EpisodeRepositoryInterpreter {
  def apply[F[_]: Applicative](cache: TrieMap[GUID, Episode]): EpisodeRepositoryInterpreter[F] =
    new EpisodeRepositoryInterpreter(cache)
}
