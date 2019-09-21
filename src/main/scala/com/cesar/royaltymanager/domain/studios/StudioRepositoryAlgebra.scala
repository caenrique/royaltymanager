package com.cesar.royaltymanager.domain.studios

import cats.data.OptionT
import com.cesar.royaltymanager.domain.GUID.GUID

trait StudioRepositoryAlgebra[F[_]] {
  def get(id: GUID): OptionT[F, Studio]
}
