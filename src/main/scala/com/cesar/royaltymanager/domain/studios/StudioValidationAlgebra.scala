package com.cesar.royaltymanager.domain.studios

import cats.data.EitherT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.ValidationError.StudioNotFoundError

trait StudioValidationAlgebra[F[_]] {
  def exists(ownerId: GUID): EitherT[F, StudioNotFoundError, Unit]
}
