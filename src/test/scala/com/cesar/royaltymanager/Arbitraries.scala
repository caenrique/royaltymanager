package com.cesar.royaltymanager

import com.cesar.royaltymanager.domain.GUID.GUID
import com.cesar.royaltymanager.domain.episodes.Episode
import com.cesar.royaltymanager.domain.payments.Payment
import com.cesar.royaltymanager.domain.studios.Studio
import com.cesar.royaltymanager.domain.viewing.Viewing
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

trait Arbitraries {

  implicit val payment = Arbitrary[Payment] {
    for {
      owner   <- Gen.alphaNumStr
      name    <- Gen.alphaNumStr
      royalty <- arbitrary[Double]
      views   <- arbitrary[Int]
    } yield Payment(s"id$owner", name, royalty, views)
  }

  implicit val episode = Arbitrary[Episode] {
    for {
      id    <- Gen.alphaNumStr
      name  <- Gen.alphaNumStr
      owner <- Gen.alphaNumStr
    } yield Episode(id, name, owner)
  }

  implicit val studio = Arbitrary[Studio] {
    for {
      id      <- Gen.alphaNumStr
      name    <- Gen.alphaNumStr
      payment <- Gen.choose(1f, 20f)
    } yield Studio(s"id$id", name, payment)
  }

  implicit val viewing = Arbitrary[Viewing] {
    for {
      episde   <- Gen.alphaNumStr
      customer <- Gen.alphaNumStr
    } yield Viewing(episde, customer)
  }

}
