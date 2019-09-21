package com.cesar.royaltymanager.domain.episodes

import com.cesar.royaltymanager.domain.GUID.GUID

case class Episode(id: GUID, name: String, rightsowner: GUID)
