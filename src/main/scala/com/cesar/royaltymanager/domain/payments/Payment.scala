package com.cesar.royaltymanager.domain.payments

import com.cesar.royaltymanager.domain.GUID.GUID

case class Payment(rightsownerId: GUID, rightsowner: String, royalty: Double, viewings: Int)
