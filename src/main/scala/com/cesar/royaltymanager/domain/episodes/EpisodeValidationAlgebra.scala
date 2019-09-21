package com.cesar.royaltymanager.domain.episodes

import cats.data.EitherT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.EpisodeNotFoundError

trait EpisodeValidationAlgebra[F[_]] {
  def exist(episodeId: GUID): EitherT[F, EpisodeNotFoundError, Unit]
}
