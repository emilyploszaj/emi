package dev.emi.emi.search;

import com.google.common.collect.Lists;
import dev.emi.emi.api.search.EmiSearchManagerApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmiSearchManager implements EmiSearchManagerApi {
    private EmiSearch.CompiledQuery compiledQuery;
    private List<? extends EmiIngredient> stacks = EmiStackList.stacks;
    private volatile SearchWorker currentWorker;

    public CompletableFuture<List<? extends EmiIngredient>> search(String query) {
        synchronized (this) {
            SearchWorker worker = new SearchWorker(query, EmiScreenManager.getSearchSource());
            currentWorker = worker;

            EmiSearch.executor.execute(worker);
            return worker.getCompletionFuture();
        }
    }

    public List<? extends EmiIngredient> getStacks() {
        return this.stacks;
    }

    public EmiSearch.CompiledQuery getCompiledQuery() {
        return this.compiledQuery;
    }

    class SearchWorker implements Runnable {
        private final String query;
        private final List<? extends EmiIngredient> source;
        private final CompletableFuture<List<? extends EmiIngredient>> completion;

        SearchWorker(String query, List<? extends EmiIngredient> source) {
            this.query = query;
            this.source = source;
            this.completion = new CompletableFuture<>();
        }

        public CompletableFuture<List<? extends EmiIngredient>> getCompletionFuture() {
            return completion;
        }

        private void apply(List<? extends EmiIngredient> stacks) {
            synchronized (EmiSearchManager.this) {
                if(this == currentWorker) {
                    EmiSearchManager.this.stacks = stacks;
                    EmiSearchManager.this.currentWorker = null;
                }
            }
            completion.complete(stacks);
        }

        @Override
        public void run() {
            try {
                EmiSearch.CompiledQuery compiled = new EmiSearch.CompiledQuery(query);
                compiledQuery = compiled;
                if (compiled.isEmpty()) {
                    apply(source);
                    return;
                }
                List<EmiIngredient> stacks = Lists.newArrayList();
                int processed = 0;
                for (EmiIngredient stack : source) {
                    if (processed++ >= 1024) {
                        processed = 0;
                        if (this != currentWorker) {
                            apply(source);
                            return;
                        }
                    }
                    List<EmiStack> ess = stack.getEmiStacks();
                    // TODO properly support ingredients?
                    if (ess.size() == 1) {
                        EmiStack es = ess.get(0);
                        if (compiled.test(es)) {
                            stacks.add(stack);
                        }
                    }
                }
                apply(List.copyOf(stacks));
            } catch (Exception e) {
                EmiLog.error("Error when attempting to search:");
                e.printStackTrace();
                apply(source);
            }
        }
    }
}
