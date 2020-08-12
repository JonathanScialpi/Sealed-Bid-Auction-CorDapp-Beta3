package com.template.states

import com.template.contracts.AuctionContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.security.PublicKey

// *********
// * State *
// *********
@BelongsToContract(AuctionContract::class)
data class AuctionState(
        val winner: Party?,
        // have to make sure that the only party that can be added in each transaction is that of the
        // party who is running the BidNotification flow.
        val auctioneer : Party,
        val status: String,
        val numOfRequiredBids: Int,
        val acknowledgedBids : List<PublicKey> = listOf(),
        override val participants: List<Party> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState{

    fun setClosedStatus(): AuctionState{
        return copy(status = "Closed")
    }

    fun addAcknowledgedBids(bidders: List<PublicKey>): AuctionState{
        return copy(acknowledgedBids = bidders)
    }
}

