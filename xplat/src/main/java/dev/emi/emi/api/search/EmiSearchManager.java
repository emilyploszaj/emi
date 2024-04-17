package dev.emi.emi.api.search;

import dev.emi.emi.api.stack.EmiIngredient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A search manager controls searching for items using EMI infrastructure.
 */
public interface EmiSearchManager {
    /**
     * {@return the current list of stacks matching the last search query}
     */
    List<? extends EmiIngredient> getStacks();

    /**
     * Search for ingredients matching the given query string. The list returned by {@link EmiSearchManager#getStacks()}
     * will not update until after the returned future completes.
     * @param query the query string to use when searching
     * @return a future that completes with the updated list
     */
    CompletableFuture<List<? extends EmiIngredient>> search(String query);
}
