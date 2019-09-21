package com.cesar.royaltymanager.domain.episodes

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.EpisodeNotFoundError

class EpisodeValidationInterpreter[F[_]: Applicative](repository: EpisodeRepositoryAlgebra[F])
    extends EpisodeValidationAlgebra[F] {

  def exist(episodeId: GUID): EitherT[F, EpisodeNotFoundError, Unit] =
    EitherT {
      repository.get(episodeId).value.map {
        case Some(episode) => Right(episode)
        case _             => Left(EpisodeNotFoundError(episodeId))
      }
    }
}

object EpisodeValidationInterpreter {
  def apply[F[_]: Applicative](
      repository: EpisodeRepositoryAlgebra[F]): EpisodeValidationInterpreter[F] =
    new EpisodeValidationInterpreter(repository)
}
