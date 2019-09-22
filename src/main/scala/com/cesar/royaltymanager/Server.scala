package com.cesar.royaltymanager

import cats.effect._
import cats.implicits._
import com.cesar.royaltymanager.config.{EpisodeList, RoyaltyManagerConfig, StudioList}
import com.cesar.royaltymanager.domain.episodes.{EpisodeService, EpisodeValidationInterpreter}
import com.cesar.royaltymanager.domain.payments.PaymentService
import com.cesar.royaltymanager.domain.studios.{StudioService, StudioValidationInterpreter}
import com.cesar.royaltymanager.infrastructure.endpoints.{
  PaymentEndpoints,
  ResetEndpoints,
  ViewingEndpoint
}
import com.cesar.royaltymanager.infrastructure.repository.inmemory.{
  EpisodeRepositoryInterpreter,
  PaymentRepositoryInterpreter,
  StudioRepositoryInterpreter,
  _
}
import io.circe.config.parser
import io.circe.generic.auto._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}

import scala.collection.concurrent.TrieMap

object Server extends IOApp {
  def run(args: List[String]): IO[ExitCode] = createServer.use(_ => IO.never).as(ExitCode.Success)

  def listToMap[A, B](list: List[(A, B)]): TrieMap[A, B] = new TrieMap[A, B] ++ list

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      conf <- Resource.liftF(parser.decodePathF[F, RoyaltyManagerConfig]("royaltymanager"))
      epl  <- Resource.liftF(loadData[F, EpisodeList](conf.episodes))
      stl  <- Resource.liftF(loadData[F, StudioList](conf.studios))
      episodeRepo       = EpisodeRepositoryInterpreter[F](listToMap(epl.episodes.map(ep => ep.id -> ep)))
      studioRepo        = StudioRepositoryInterpreter[F](listToMap(stl.studios.map(st => st.id -> st)))
      paymentRepo       = PaymentRepositoryInterpreter[F]
      episodeValidation = EpisodeValidationInterpreter(episodeRepo)
      episodeService    = EpisodeService[F](episodeRepo, episodeValidation)
      studioValidation  = StudioValidationInterpreter(studioRepo)
      studioService     = StudioService[F](studioRepo, studioValidation)
      paymentService    = PaymentService[F](paymentRepo, studioValidation)
      httpApp = Router(
        "/payments" -> PaymentEndpoints.endpoints[F](paymentService, studioService),
        "/viewing"  -> ViewingEndpoint.endpoints[F](studioService, episodeService, paymentService),
        "/reset"    -> ResetEndpoints.endpoints[F](paymentService)
      ).orNotFound
      server <- BlazeServerBuilder[F]
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server
}
