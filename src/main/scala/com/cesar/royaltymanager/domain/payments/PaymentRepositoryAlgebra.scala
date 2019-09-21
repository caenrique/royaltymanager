package com.cesar.royaltymanager.domain.payments

import cats.data.OptionT
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.studios.Studio

trait PaymentRepositoryAlgebra[F[_]] {
  def list(): F[List[Payment]]
  def get(id: GUID): OptionT[F, Payment]
  def incViews(owner: Studio): F[Payment]
  def reset(): F[Unit]
}
