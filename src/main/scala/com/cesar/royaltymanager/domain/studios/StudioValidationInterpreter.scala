package com.cesar.royaltymanager.domain.studios

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.StudioNotFoundError

class StudioValidationInterpreter[F[_]: Applicative](repository: StudioRepositoryAlgebra[F])
    extends StudioValidationAlgebra[F] {

  override def exists(ownerId: GUID): EitherT[F, StudioNotFoundError, Unit] =
    EitherT {
      repository.get(ownerId).value.map {
        case Some(_) => Right(())
        case _       => Left(StudioNotFoundError(ownerId))
      }
    }
}

object StudioValidationInterpreter {
  def apply[F[_]: Applicative](
      repository: StudioRepositoryAlgebra[F]): StudioValidationInterpreter[F] =
    new StudioValidationInterpreter(repository)
}
