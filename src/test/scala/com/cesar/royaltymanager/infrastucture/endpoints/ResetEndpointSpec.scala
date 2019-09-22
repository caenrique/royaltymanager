package com.cesar.royaltymanager.infrastucture.endpoints

import cats.effect.IO
import com.cesar.royaltymanager.Arbitraries
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentService}
import com.cesar.royaltymanager.domain.studios.{Studio, StudioService, StudioValidationInterpreter}
import com.cesar.royaltymanager.infrastructure.endpoints.{PaymentEndpoints, ResetEndpoints}
import com.cesar.royaltymanager.infrastructure.repository.inmemory.{
  PaymentRepositoryInterpreter,
  StudioRepositoryInterpreter
}
import org.http4s.circe.jsonOf
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{EntityDecoder, HttpApp, Status, Uri}
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import io.circe.generic.auto._

import scala.collection.concurrent.TrieMap

class ResetEndpointSpec
    extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with Arbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  implicit val paymentListDec: EntityDecoder[IO, List[Payment]] = jsonOf

  def getTestResources(): (HttpApp[IO], PaymentRepositoryInterpreter[IO]) = {
    val studioRepo      = StudioRepositoryInterpreter[IO](TrieMap.empty[String, Studio])
    val studioVal       = StudioValidationInterpreter[IO](studioRepo)
    val paymentRepo     = PaymentRepositoryInterpreter[IO]
    val paymentService  = PaymentService[IO](paymentRepo, studioVal)
    val studioService   = StudioService[IO](studioRepo, studioVal)
    val resetEndpoint   = ResetEndpoints.endpoints[IO](paymentService)
    val paymentEndpoint = PaymentEndpoints.endpoints[IO](paymentService, studioService)
    val routes          = Router(("/reset", resetEndpoint), ("/payments", paymentEndpoint)).orNotFound
    (routes, paymentRepo)
  }

  test("reset payments") {
    val (routes, paymentRepo) = getTestResources()

    forAll { payment: Payment =>
      paymentRepo.create(payment)

      (for {
        request  <- GET(Uri.uri("/payments"))
        response <- routes.run(request)
        payments <- response.as[List[Payment]]
      } yield {
        response.status shouldEqual Status.Ok
        (payments should have).size(1)
      }).unsafeRunSync()

      (for {
        request  <- POST(Uri.uri("/reset"))
        response <- routes.run(request)
      } yield {
        response.status shouldEqual Status.Accepted
      }).unsafeRunSync()

      (for {
        request  <- GET(Uri.uri("/payments"))
        response <- routes.run(request)
        payments <- response.as[List[Payment]]
      } yield {
        response.status shouldEqual Status.Ok
        (payments should have).size(0)
      })
    }
  }

}
