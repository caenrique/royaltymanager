package com.cesar.royaltymanager.infrastructure.repository.inmemory

import cats.Applicative
import cats.data.OptionT
import cats.implicits._
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentRepositoryAlgebra}
import com.cesar.royaltymanager.domain.studios.Studio

import scala.collection.concurrent.TrieMap

class PaymentRepositoryInterpreter[F[_]: Applicative] extends PaymentRepositoryAlgebra[F] {
  private var cache = new TrieMap[GUID, Payment]

  override def list(): F[List[Payment]] =
    cache.values.toList.pure[F]

  override def get(id: GUID): OptionT[F, Payment] = OptionT.fromOption(cache.get(id))

  override def incViews(owner: Studio): F[Payment] = {
    val updated = cache.get(owner.id) match {
      case Some(p) => p.copy(viewings = p.viewings + 1, royalty = p.royalty + owner.payment)
      case None    => Payment(owner.id, owner.name, owner.payment, 1)
    }
    cache += (owner.id -> updated)
    updated.pure[F]
  }

  override def reset(): F[Unit] = {
    cache = new TrieMap[GUID, Payment]
    ().pure[F]
  }

  // Used for test. Functionality not exposed in the API
  override def create(payment: Payment): F[Payment] = {
    cache.put(payment.rightsownerId, payment)
    payment.pure[F]
  }
}

object PaymentRepositoryInterpreter {
  def apply[F[_]: Applicative]: PaymentRepositoryInterpreter[F] =
    new PaymentRepositoryInterpreter()
}
