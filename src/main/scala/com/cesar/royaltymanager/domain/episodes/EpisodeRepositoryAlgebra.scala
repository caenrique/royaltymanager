package com.cesar.royaltymanager.domain.episodes

import cats.data.OptionT
import com.cesar.royaltymanager.domain.GUID.GUID

trait EpisodeRepositoryAlgebra[F[_]] {
  def get(episodeId: GUID): OptionT[F, Episode]
  def create(episode: Episode): F[Episode]
}
