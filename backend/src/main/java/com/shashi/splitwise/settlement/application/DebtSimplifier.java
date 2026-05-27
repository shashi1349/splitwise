package com.shashi.splitwise.settlement.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Greedy debt minimisation using two max-heaps.
 *
 * <p>Given a map of {@code userId -> netCents} (positive = creditor,
 * negative = debtor, total = 0), produces a list of transfers that
 * settles every balance with at most {@code N-1} transactions, where
 * {@code N} is the number of users with non-zero balances.</p>
 *
 * <h3>Why this is correct</h3>
 * <ul>
 *   <li>Each iteration matches the largest creditor with the largest
 *       debtor, transfers {@code min(c, d)}, and pushes back any leftover.
 *       Whichever party fully settles is removed permanently.</li>
 *   <li>So every iteration reduces the total non-zero count by at least
 *       one. After at most {@code N-1} iterations only one party can
 *       remain, and since the global sum is zero, that party also has
 *       zero balance.</li>
 *   <li>This is the minimum-transfer solution under the constraint that
 *       every transfer is between exactly two parties (the general
 *       minimum-transactions problem is NP-hard, but this greedy is
 *       optimal in the unrestricted-recipient model used by Splitwise).</li>
 * </ul>
 *
 * <h3>Determinism</h3>
 * <p>Heap ties on amount are broken by {@code userId} so two clients
 * computing the same input produce byte-identical output.</p>
 */
public final class DebtSimplifier {

    /** A single transfer suggestion. */
    public record Transfer(long fromUserId, long toUserId, long amountCents) {}

    private record Entry(long userId, long amount) {}

    private DebtSimplifier() { }

    public static List<Transfer> simplify(Map<Long, Long> netByUserId) {
        Comparator<Entry> byAmountDesc = Comparator
            .comparingLong(Entry::amount).reversed()
            .thenComparingLong(Entry::userId);

        PriorityQueue<Entry> creditors = new PriorityQueue<>(byAmountDesc);
        PriorityQueue<Entry> debtors = new PriorityQueue<>(byAmountDesc);

        for (var e : netByUserId.entrySet()) {
            long net = e.getValue() == null ? 0L : e.getValue();
            if (net > 0) creditors.add(new Entry(e.getKey(), net));
            else if (net < 0) debtors.add(new Entry(e.getKey(), -net));
        }

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Entry c = creditors.poll();
            Entry d = debtors.poll();
            long amount = Math.min(c.amount(), d.amount());
            transfers.add(new Transfer(d.userId(), c.userId(), amount));
            if (c.amount() > amount) {
                creditors.add(new Entry(c.userId(), c.amount() - amount));
            }
            if (d.amount() > amount) {
                debtors.add(new Entry(d.userId(), d.amount() - amount));
            }
        }
        return transfers;
    }
}
