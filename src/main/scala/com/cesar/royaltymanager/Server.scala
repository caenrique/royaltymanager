package com.cesar.royaltymanager

import cats.effect._
import cats.implicits._
import com.cesar.royaltymanager.config.{EpisodeList, RoyaltyManagerConfig}
import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.episodes.{
  Episode,
  EpisodeService,
  EpisodeValidationInterpreter
}
import com.cesar.royaltymanager.domain.payments.PaymentService
import com.cesar.royaltymanager.domain.studios.{Studio, StudioService, StudioValidationInterpreter}
import com.cesar.royaltymanager.infrastructure.endpoints.{
  PaymentEndpoints,
  ResetEndpoints,
  ViewingEndpoint
}
import com.cesar.royaltymanager.infrastructure.repository.inmemory.{
  EpisodeRepositoryInMemoryInterpreter,
  PaymentRepositoryInMemoryInterpreter,
  StudioRepositoryInMemoryInterpreter,
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

  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] =
    for {
      conf <- Resource.liftF(parser.decodePathF[F, RoyaltyManagerConfig]("royaltymanager"))
      episodes <- Resource.liftF(loadData[F, EpisodeList](conf.episodes))
      studios <- Resource.liftF(loadData[F, List[Studio]](conf.studios))
      episodeRepo = EpisodeRepositoryInMemoryInterpreter[F](
        new TrieMap[GUID, Episode] ++ (episodes.episodes.map(ep => ep.id -> ep)))
      studioRepo = StudioRepositoryInMemoryInterpreter[F](
        new TrieMap[GUID, Studio] ++ (studios.map(st => st.id -> st)))
      paymentRepo = PaymentRepositoryInMemoryInterpreter[F]
      episodeValidation = EpisodeValidationInterpreter(episodeRepo)
      episodeService = EpisodeService[F](episodeRepo, episodeValidation)
      studioValidation = StudioValidationInterpreter(studioRepo)
      studioService = StudioService[F](studioRepo, studioValidation)
      paymentService = PaymentService[F](paymentRepo, studioValidation)
      httpApp = Router(
        "/payments" -> PaymentEndpoints.endpoints[F](paymentService, studioService),
        "/viewing" -> ViewingEndpoint.endpoints[F](studioService, episodeService, paymentService),
        "/reset" -> ResetEndpoints.endpoints[F](paymentService)
      ).orNotFound
      server <- BlazeServerBuilder[F]
        .bindHttp(conf.server.port, conf.server.host)
        .withHttpApp(httpApp)
        .resource
    } yield server
}
