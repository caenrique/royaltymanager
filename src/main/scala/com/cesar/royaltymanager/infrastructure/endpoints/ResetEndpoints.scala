package com.cesar.royaltymanager.infrastructure.endpoints

import cats.effect.Sync
import cats.implicits._
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentService}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class ResetEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  implicit val paymentDecoder: EntityDecoder[F, Payment] = jsonOf[F, Payment]

  private def createResetEndpoint(paymentService: PaymentService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {

      case POST -> Root =>
        for {
          _ <- paymentService.reset()
          result <- Accepted()
        } yield result
    }
}

object ResetEndpoints {
  def endpoints[F[_]: Sync](paymentService: PaymentService[F]): HttpRoutes[F] =
    new ResetEndpoints[F].createResetEndpoint(paymentService)
}
