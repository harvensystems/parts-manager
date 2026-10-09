package app.harven.partsmanager.migration;

import reactor.core.publisher.Mono;

public interface MigrationTask {

    String getId();

    String getName();

    String getDescription();

    int getOrder();

    Mono<Void> execute();
}
