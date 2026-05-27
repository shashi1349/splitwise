package com.shashi.splitwise.expense.domain;

/**
 * How the total amount of an expense is divided among participants.
 *
 * <ul>
 *   <li>{@link #EQUAL}    — divide evenly; pennies go to the participants
 *                           with the smallest userIds.</li>
 *   <li>{@link #EXACT}    — caller specifies each share in currency units;
 *                           the sum must equal the total exactly.</li>
 *   <li>{@link #PERCENT}  — caller specifies percentages summing to 100.00;
 *                           cents are computed by floor and the remainder
 *                           is allocated to the smallest userIds first.</li>
 * </ul>
 */
public enum SplitType {
    EQUAL,
    EXACT,
    PERCENT
}
