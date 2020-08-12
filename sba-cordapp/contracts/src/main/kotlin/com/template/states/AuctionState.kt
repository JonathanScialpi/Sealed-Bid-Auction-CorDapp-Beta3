package com.template.states

import com.template.contracts.AuctionContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
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
        val status: String,
        val numOfRequiredBids: Int,
        val acknowledgedBids : List<PublicKey> = listOf(),
        override val participants: List<Party> = listOf()) : ContractState{

    fun setClosedStatus(): AuctionState{
        return copy(status = "Closed")
    }
}

