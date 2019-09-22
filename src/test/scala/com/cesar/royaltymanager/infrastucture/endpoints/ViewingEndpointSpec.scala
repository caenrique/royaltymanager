package com.cesar.royaltymanager.infrastucture.endpoints

import cats.effect.IO
import com.cesar.royaltymanager.Arbitraries
import com.cesar.royaltymanager.domain.episodes.{
  Episode,
  EpisodeService,
  EpisodeValidationInterpreter
}
import com.cesar.royaltymanager.domain.payments.PaymentService
import com.cesar.royaltymanager.domain.studios.{Studio, StudioService, StudioValidationInterpreter}
import com.cesar.royaltymanager.domain.viewing.Viewing
import com.cesar.royaltymanager.infrastructure.endpoints.ViewingEndpoint
import com.cesar.royaltymanager.infrastructure.repository.inmemory.{
  EpisodeRepositoryInterpreter,
  PaymentRepositoryInterpreter,
  StudioRepositoryInterpreter
}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, EntityEncoder, HttpApp, Status, Uri}
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import io.circe.generic.auto._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.collection.concurrent.TrieMap

class ViewingEndpointSpec
    extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with Arbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  implicit val viewingEnc: EntityEncoder[IO, Viewing] = jsonEncoderOf
  implicit val viewingDec: EntityDecoder[IO, Viewing] = jsonOf

  def getTestResources()
    : (HttpApp[IO], EpisodeRepositoryInterpreter[IO], StudioRepositoryInterpreter[IO]) = {
    val episodeRepo    = EpisodeRepositoryInterpreter[IO](TrieMap.empty[String, Episode])
    val episodeVal     = EpisodeValidationInterpreter[IO](episodeRepo)
    val episodeService = EpisodeService[IO](episodeRepo, episodeVal)
    val studioRepo     = StudioRepositoryInterpreter[IO](TrieMap.empty[String, Studio])
    val studioVal      = StudioValidationInterpreter[IO](studioRepo)
    val paymentRepo    = PaymentRepositoryInterpreter[IO]
    val paymentService = PaymentService[IO](paymentRepo, studioVal)
    val studioService  = StudioService[IO](studioRepo, studioVal)
    val endpoint       = ViewingEndpoint.endpoints[IO](studioService, episodeService, paymentService)
    val routes         = Router(("/viewing", endpoint)).orNotFound
    (routes, episodeRepo, studioRepo)
  }

  test("viewing non existent episode") {
    val (routes, _, _) = getTestResources()

    forAll { viewing: Viewing =>
      (for {
        request  <- POST(viewing, Uri.uri("/viewing"))
        response <- routes.run(request)
      } yield {
        response.status shouldEqual Status.NotFound
      }).unsafeRunSync()
    }
  }

  test("viewing existent episode with non existent rightsowner") {
    val (routes, episodeRepo, _) = getTestResources()

    forAll { viewing: Viewing =>
      episodeRepo.create(Episode(viewing.episode, "Something", "owner"))
      (for {
        request  <- POST(viewing, Uri.uri("/viewing"))
        response <- routes.run(request)
      } yield {
        response.status shouldEqual Status.NotFound
      }).unsafeRunSync()
    }
  }

  test("viewing existent episode") {
    val (routes, episodeRepo, studioRepo) = getTestResources()

    forAll { viewing: Viewing =>
      episodeRepo.create(Episode(viewing.episode, "Something", "owner"))
      studioRepo.create(Studio("owner", "studioName", 12f))
      (for {
        request  <- POST(viewing, Uri.uri("/viewing"))
        response <- routes.run(request)
      } yield {
        response.status shouldEqual Status.Accepted
      }).unsafeRunSync()
    }
  }
}
