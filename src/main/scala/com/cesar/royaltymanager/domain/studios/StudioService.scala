package com.cesar.royaltymanager.domain.studios

import cats.Monad
import cats.data.EitherT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.StudioNotFoundError

class StudioService[F[_]](repository: StudioRepositoryAlgebra[F],
                          validation: StudioValidationAlgebra[F]) {

  def get(ownerId: GUID)(implicit M: Monad[F]): EitherT[F, StudioNotFoundError, Studio] =
    for {
      _ <- validation.exists(ownerId)
      owner <- repository.get(ownerId).toRight(StudioNotFoundError(ownerId))
    } yield owner

}

object StudioService {
  def apply[F[_]](repository: StudioRepositoryAlgebra[F],
                  validation: StudioValidationAlgebra[F]): StudioService[F] =
    new StudioService(repository, validation)
}
