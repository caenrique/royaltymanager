package com.cesar.royaltymanager.infrastructure.endpoints

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits._
import com.cesar.royaltymanager.domain.ValidationError.ViewingValidationError
import com.cesar.royaltymanager.domain.episodes.EpisodeService
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentService}
import com.cesar.royaltymanager.domain.studios.StudioService
import com.cesar.royaltymanager.domain.viewing.Viewing
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

class ViewingEndpoint[F[_]: Sync] extends Http4sDsl[F] {

  implicit val viewingRequestDecoder: EntityDecoder[F, Viewing] = jsonOf

  private def createViewingEndpoint(studioService: StudioService[F],
                                    episodeService: EpisodeService[F],
                                    paymentService: PaymentService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        val action = for {
          viewing <- EitherT.liftF(req.as[Viewing])
          episode <- episodeService.get(viewing.episode).leftMap(_ => ViewingValidationError)
          owner <- studioService.get(episode.rightsowner).leftMap(_ => ViewingValidationError)
          recorded <- EitherT.liftF[F, ViewingValidationError.type, Payment](
            paymentService.incViews(owner))
        } yield recorded

        action.value.flatMap {
          case Right(_)                     => Accepted()
          case Left(ViewingValidationError) => NotFound(s"Episode not found")
        }
    }
}

object ViewingEndpoint {
  def endpoints[F[_]: Sync](studioService: StudioService[F],
                            episodeService: EpisodeService[F],
                            paymentService: PaymentService[F]) =
    new ViewingEndpoint[F].createViewingEndpoint(studioService, episodeService, paymentService)
}
