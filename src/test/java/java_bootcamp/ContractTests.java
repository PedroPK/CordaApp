package test.java.java_bootcamp;

import main.java.java_bootcamp.TokenContract;
import main.java.java_bootcamp.TokenState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "", "GB"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "", "GB"));
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));
    private TokenState tokenState = new TokenState(alice.getParty(), bob.getParty(), 1);

    @Test
    public void tokenContractImplementsContract() {
        assert(new TokenContract() instanceof Contract);
    }

    @Test
    public void tokenContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(TokenContract.ID, tokenState);
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresOneOutputInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(TokenContract.ID, tokenState);
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Hasone output, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresOneCommandInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(TokenContract.ID, tokenState);
            // Hastwo commands, will fail.
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(TokenContract.ID, tokenState);
            // Hasone command, will verify.
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsOutputToBeATokenState() {
        transaction(ledgerServices, tx -> {
            // Haswrong output type, will fail.
            tx.output(TokenContract.ID, new DummyState());
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Hascorrect output type, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsOutputToHaveAPositiveAmount() {
        TokenState zeroTokenState = new TokenState(alice.getParty(), bob.getParty(), 0);
        TokenState negativeTokenState = new TokenState(alice.getParty(), bob.getParty(), -1);
        TokenState positiveTokenState = new TokenState(alice.getParty(), bob.getParty(), 2);

        transaction(ledgerServices, tx -> {
            // Haszero-amount TokenState, will fail.
            tx.output(TokenContract.ID, zeroTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Hasnegative-amount TokenState, will fail.
            tx.output(TokenContract.ID, negativeTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Haspositive-amount TokenState, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Also has positive-amount TokenState, will verify.
            tx.output(TokenContract.ID, positiveTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        transaction(ledgerServices, tx -> {
            // Haswrong command type, will fail.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Hascorrect command type, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        TokenState tokenStateWhereBobIsIssuer = new TokenState(bob.getParty(), alice.getParty(), 1);

        transaction(ledgerServices, tx -> {
            // Issueris not a required signer, will fail.
            tx.output(TokenContract.ID, tokenState);
            tx.command(bob.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issueris also not a required signer, will fail.
            tx.output(TokenContract.ID, tokenStateWhereBobIsIssuer);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issueris a required signer, will verify.
            tx.output(TokenContract.ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issueris also a required signer, will verify.
            tx.output(TokenContract.ID, tokenStateWhereBobIsIssuer);
            tx.command(bob.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }
}
