package main.java.java_bootcamp;

import net.corda.core.identity.Party;

import java.util.ArrayList;
import java.util.Collection;

/* Our state, defining a shared fact on the ledger.
 * See src/main/java/examples/ArtState.java for an example. */
public class TokenState {

    private Party aIssuer;
    private Party aOwner;
    private int aAmount;

    public TokenState(Party pIssuer, Party pOwner, int pAmount) {
        this.aIssuer = pIssuer;
        this.aOwner = pOwner;
        this.aAmount = pAmount;
    }

    public Party getIssuer() {
        return this.aIssuer;
    }

    public Party getOwner() {
        return this.aOwner;
    }

    public int getAmount() {
        return this.aAmount;
    }

    public Collection<Party> getParticipants() {
        Collection<Party> participants = new ArrayList<Party>();

        if ( this.aIssuer != null ) {
            participants.add(this.aIssuer);
        }

        if ( this.aOwner != null ) {
            participants.add(this.aOwner);
        }

        return participants;
    }

}