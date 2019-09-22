package com.cesar.royaltymanager.infrastructure.endpoints

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.cesar.royaltymanager.domain.ValidationError.StudioNotFoundError
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentService}
import com.cesar.royaltymanager.domain.studios.StudioService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class PaymentEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  implicit val paymentDecoder: EntityDecoder[F, Payment] = jsonOf[F, Payment]

  private def createPaymentEndpoin(paymentService: PaymentService[F],
                                   ownerService: StudioService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {

      case GET -> Root =>
        val action: F[List[Payment]] = for {
          paymentList <- paymentService.list()
        } yield paymentList

        action.flatMap { payments =>
          Ok(payments.asJson)
        }

      case GET -> Root / ownerId =>
        val payments: EitherT[F, StudioNotFoundError, Payment] = for {
          owner   <- ownerService.get(ownerId)
          payment <- paymentService.get(owner.id)
        } yield payment

        payments.value.flatMap {
          case Right(payment)                => Ok(payment.asJson)
          case Left(StudioNotFoundError(id)) => NotFound(s"GUID not found: $id")
        }
    }
}

object PaymentEndpoints {
  def endpoints[F[_]: Sync](paymentService: PaymentService[F],
                            ownerService: StudioService[F]): HttpRoutes[F] =
    new PaymentEndpoints[F].createPaymentEndpoin(paymentService, ownerService)
}
