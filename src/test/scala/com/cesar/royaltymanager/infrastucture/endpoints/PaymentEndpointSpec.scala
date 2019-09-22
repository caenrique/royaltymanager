package com.cesar.royaltymanager.infrastucture.endpoints

import cats.effect.IO
import com.cesar.royaltymanager.Arbitraries
import com.cesar.royaltymanager.domain.payments.{Payment, PaymentService}
import com.cesar.royaltymanager.domain.studios.{Studio, StudioService, StudioValidationInterpreter}
import com.cesar.royaltymanager.infrastructure.endpoints.PaymentEndpoints
import com.cesar.royaltymanager.infrastructure.repository.inmemory.{
  PaymentRepositoryInterpreter,
  StudioRepositoryInterpreter
}
import org.http4s.{EntityDecoder, EntityEncoder, HttpApp, Status, Uri}
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.server.Router
import org.scalatest.Matchers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import io.circe.generic.auto._

import scala.collection.concurrent.TrieMap

class PaymentEndpointSpec
    extends AnyFunSuite
    with Matchers
    with ScalaCheckPropertyChecks
    with Arbitraries
    with Http4sDsl[IO]
    with Http4sClientDsl[IO] {

  implicit val paymentListDec: EntityDecoder[IO, List[Payment]] = jsonOf
  implicit val paymentDec: EntityDecoder[IO, Payment]           = jsonOf

  def getTestResources()
    : (HttpApp[IO], PaymentRepositoryInterpreter[IO], StudioRepositoryInterpreter[IO]) = {
    val studioRepo     = StudioRepositoryInterpreter[IO](TrieMap.empty[String, Studio])
    val studioVal      = StudioValidationInterpreter[IO](studioRepo)
    val paymentRepo    = PaymentRepositoryInterpreter[IO]
    val paymentService = PaymentService[IO](paymentRepo, studioVal)
    val studioService  = StudioService[IO](studioRepo, studioVal)
    val endpoint       = PaymentEndpoints.endpoints[IO](paymentService, studioService)
    val routes         = Router(("/payments", endpoint)).orNotFound
    (routes, paymentRepo, studioRepo)
  }

  test("no payments") {
    val (routes, _, _) = getTestResources()
    (for {
      request  <- GET(Uri.uri("/payments"))
      response <- routes.run(request)
      payments <- response.as[List[Payment]]
    } yield {
      response.status shouldEqual Status.Ok
      (payments should have).size(0)
    }).unsafeRunSync()
  }

  test("payment list") {
    forAll { payment: Payment =>
      val (routes, paymentRepo, _) = getTestResources()
      paymentRepo.create(payment)
      (for {
        request  <- GET(Uri.uri("/payments"))
        response <- routes.run(request)
        payments <- response.as[List[Payment]]
      } yield {
        response.status shouldEqual Status.Ok
        (payments should have).size(1)
      }).unsafeRunSync()
    }
  }

  test("payment with id") {

    forAll { (studio: Studio, payment: Payment) =>
      val (routes, paymentRepo, studioRepo) = getTestResources()
      studioRepo.create(studio)
      paymentRepo.create(payment.copy(rightsownerId = studio.id))
      (for {
        request  <- GET(Uri.unsafeFromString(s"/payments/${studio.id}"))
        response <- routes.run(request)
        payments <- response.as[Payment]
      } yield {
        response.status shouldEqual Status.Ok
      }).unsafeRunSync()

    }

  }
}
