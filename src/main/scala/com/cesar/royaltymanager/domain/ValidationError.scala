package com.cesar.royaltymanager.domain

import com.cesar.royaltymanager.domain.GUID.GUID

sealed trait ValidationError extends Product with Serializable

object ValidationError {
  case class StudioNotFoundError(id: GUID) extends ValidationError
  case class EpisodeNotFoundError(id: GUID) extends ValidationError
  case object ViewingValidationError extends ValidationError
}
