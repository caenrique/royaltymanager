package com.cesar.royaltymanager

import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val srDec: Decoder[ServerConfig] = deriveDecoder
  implicit val rmDec: Decoder[RoyaltyManagerConfig] = deriveDecoder
}
