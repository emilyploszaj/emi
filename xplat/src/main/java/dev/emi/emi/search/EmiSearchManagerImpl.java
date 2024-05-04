package dev.emi.emi.search;

import com.google.common.collect.Lists;
import dev.emi.emi.api.search.EmiSearchManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiLog;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class EmiSearchManagerImpl implements EmiSearchManager {
    public SearchWorker search(String query, List<? extends EmiIngredient> ingredients) {
        synchronized (this) {
            SearchWorker worker = new SearchWorker(query, ingredients);

            EmiSearch.executor.execute(worker);
            return worker;
        }
    }

    public class SearchWorker implements Runnable, SearchFuture {
        private final String query;
        private EmiSearch.CompiledQuery compiledQuery;
        private final List<? extends EmiIngredient> source;
        private final CompletableFuture<List<? extends EmiIngredient>> completion;
        private boolean interrupted;

        SearchWorker(String query, List<? extends EmiIngredient> source) {
            this.query = query;
            this.source = source;
            this.completion = new CompletableFuture<>();
        }

        private void apply(List<? extends EmiIngredient> stacks) {
            completion.complete(stacks);
        }

        public EmiSearch.CompiledQuery getCompiledQuery() {
            return this.compiledQuery;
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
                        if (interrupted) {
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

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            this.interrupted = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return this.interrupted;
        }

        @Override
        public boolean isDone() {
            return this.completion.isDone();
        }

        @Override
        public List<? extends EmiIngredient> get() throws InterruptedException, ExecutionException {
            return this.completion.get();
        }

        @Override
        public List<? extends EmiIngredient> get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.completion.get(timeout, unit);
        }

        @Override
        public List<? extends EmiIngredient> getNow() {
            return this.completion.getNow(this.source);
        }

        @Override
        public SearchWorker whenCompleted(Consumer<List<? extends EmiIngredient>> consumer) {
            this.completion.whenComplete((c, ex) -> {
                if(c != null) {
                    consumer.accept(c);
                }
            });
            return this;
        }
    }
}
