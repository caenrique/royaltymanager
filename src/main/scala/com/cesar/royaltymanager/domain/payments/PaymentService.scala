package com.cesar.royaltymanager.domain.payments

import cats.Monad
import cats.data.EitherT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.StudioNotFoundError
import com.cesar.royaltymanager.domain.studios.{Studio, StudioValidationAlgebra}

class PaymentService[F[_]](repository: PaymentRepositoryAlgebra[F],
                           validation: StudioValidationAlgebra[F]) {

  def list(): F[List[Payment]] = repository.list()

  def get(id: GUID)(implicit M: Monad[F]): EitherT[F, StudioNotFoundError, Payment] =
    for {
      _ <- validation.exists(id)
      payment <- repository.get(id).toRight(StudioNotFoundError(id))
    } yield payment

  def incViews(owner: Studio): F[Payment] = repository.incViews(owner)
  def reset(): F[Unit] = repository.reset()
}

object PaymentService {
  def apply[F[_]](repository: PaymentRepositoryAlgebra[F],
                  validation: StudioValidationAlgebra[F]): PaymentService[F] =
    new PaymentService(repository, validation)
}
