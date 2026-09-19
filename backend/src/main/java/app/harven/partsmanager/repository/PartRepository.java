package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.Part;
import app.harven.partsmanager.domain.PartDictionaries;
import app.harven.partsmanager.domain.PartParams;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PartRepository extends ReactiveMongoRepository<Part, String> {
    Flux<Part> findAllBy(TextCriteria criteria, Pageable pageable);
    Mono<Part> findByPartNumberIgnoreCase(String partNumber);

    @Aggregation(pipeline = {
            "{$project: { k: { $objectToArray: \"$metadata\" } } }",
            "{ $unwind: \"$k\" }",
            "{ $group: { _id: null, parameters: { $addToSet: \"$k.k\" } } }"})
    Mono<PartParams> getAllParams();

    @Aggregation(pipeline = {
            "{ $group: { " +
            "_id: null, " +
            "packages: { $addToSet: \"$packageType\" }, " +
            "manufacturers: { $addToSet: \"$manufacturer\" } } }"})
    Mono<PartDictionaries> getPackagesAndManufacturers();
}
