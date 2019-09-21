package com.cesar.royaltymanager.config

import cats.effect.{Resource, Sync}
import com.cesar.royaltymanager.domain.episodes.Episode
import com.cesar.royaltymanager.domain.studios.Studio
import io.circe.config.parser
import io.circe.generic.auto._

final case class ServerConfig(host: String, port: Int)
final case class RoyaltyManagerConfig(episodes: String, studios: String, server: ServerConfig)

object RoyaltyManagerConfig {
  def initializeData[F[_]: Sync](episodesPath: String,
                                 studiosPath: String): Resource[F, (List[Episode], List[Studio])] =
    for {
      episodes <- Resource.liftF(parser.decodePathF[F, List[Episode]](episodesPath))
      studios <- Resource.liftF(parser.decodePathF[F, List[Studio]](studiosPath))
    } yield (episodes, studios)
}
