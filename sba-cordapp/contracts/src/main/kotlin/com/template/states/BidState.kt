package com.template.states

import com.template.contracts.AuctionContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.security.PublicKey

// *********
// * State *
// *********
@BelongsToContract(AuctionContract::class)
data class BidState(
        val bid: Int,
        val mailSequenceNumber: Int,
        val auction: UniqueIdentifier,
        //Only participant is the issuer.
        override val participants: List<Party> = listOf()) : ContractState{
}

