package com.cesar.royaltymanager.config

import com.cesar.royaltymanager.domain.episodes.Episode
import com.cesar.royaltymanager.domain.studios.Studio

case class EpisodeList(episodes: List[Episode])
case class StudioList(studios: List[Studio])
