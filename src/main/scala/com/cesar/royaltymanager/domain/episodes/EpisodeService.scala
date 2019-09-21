package com.cesar.royaltymanager.domain.episodes

import cats.Monad
import cats.data.EitherT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.EpisodeNotFoundError

class EpisodeService[F[_]](repository: EpisodeRepositoryAlgebra[F],
                           validation: EpisodeValidationAlgebra[F]) {

  def get(episodeId: GUID)(implicit M: Monad[F]): EitherT[F, EpisodeNotFoundError, Episode] =
    for {
      _ <- validation.exist(episodeId)
      episode <- repository.get(episodeId).toRight(EpisodeNotFoundError(episodeId))
    } yield episode
}

object EpisodeService {
  def apply[F[_]](repository: EpisodeRepositoryAlgebra[F],
                  validation: EpisodeValidationAlgebra[F]): EpisodeService[F] =
    new EpisodeService(repository, validation)
}
