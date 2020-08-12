package com.template.contracts

import com.template.states.BidState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class BidContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.BidContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<BidContract.Commands>()
        when(command.value) {
            is BidContract.Commands.Issue -> requireThat {
                "No inputs should be consumed when issuing a bid." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing a bid." using (tx.outputs.size == 1)
                val bid = tx.outputsOfType<BidState>().single()
                "The participants list of a auction must have only one party." using (bid.participants.size == 1)
                "The auction's linearID must be included." using (bid.auction != null)
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Issue : TypeOnlyCommandData(), BidContract.Commands
    }
}