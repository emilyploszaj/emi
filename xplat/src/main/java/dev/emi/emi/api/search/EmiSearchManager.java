package dev.emi.emi.api.search;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.registry.EmiStackList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A search manager controls searching for stacks using EMI infrastructure.
 */
public interface EmiSearchManager {
    /**
     * Search for ingredients matching the given query string.
     *
     * @param query the query string to use when searching
     * @param ingredients the list of ingredients to search in
     * @return a future that completes with the updated list
     */
    SearchFuture search(String query, List<? extends EmiIngredient> ingredients);

    /**
     * Search for ingredients matching the given query string in the list of all known ingredients.
     * @param query the query string to use when searching
     * @return a future that completes with the updated list
     */
    default SearchFuture search(String query) {
        return search(query, EmiStackList.stacks);
    }

    interface SearchFuture extends Future<List<? extends EmiIngredient>> {
        /**
         * {@return the search results if the search has completed, or the original input list}
         */
        List<? extends EmiIngredient> getNow();

        SearchFuture whenCompleted(Consumer<List<? extends EmiIngredient>> consumer);

        static SearchFuture completedFuture(List<? extends EmiIngredient> list) {
            return new SearchFuture() {
                @Override
                public List<? extends EmiIngredient> getNow() {
                    return list;
                }

                @Override
                public SearchFuture whenCompleted(Consumer<List<? extends EmiIngredient>> consumer) {
                    consumer.accept(list);
                    return this;
                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public List<? extends EmiIngredient> get() {
                    return list;
                }

                @Override
                public List<? extends EmiIngredient> get(long timeout, @NotNull TimeUnit unit) {
                    return list;
                }
            };
        }
    }
}
