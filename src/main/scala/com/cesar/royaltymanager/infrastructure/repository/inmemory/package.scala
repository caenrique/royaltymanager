package com.cesar.royaltymanager.infrastructure.repository

import cats.ApplicativeError
import cats.instances.either._
import cats.syntax.bifunctor._
import cats.syntax.either._
import io.circe.Decoder
import io.circe.parser._

import scala.io.Source

package object inmemory {

  def loadData[F[_], A: Decoder](path: String)(implicit ev: ApplicativeError[F, Throwable]): F[A] =
    decode[A](Source.fromResource(path).mkString).leftWiden[Throwable].raiseOrPure[F]
}
