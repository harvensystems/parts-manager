package app.harven.partsmanager.service;

import app.harven.partsmanager.domain.Part;
import app.harven.partsmanager.domain.PartDictionaries;
import app.harven.partsmanager.domain.PartParams;
import app.harven.partsmanager.dto.CreateOrUpdatePartDto;
import app.harven.partsmanager.dto.DictionaryResponseDto;
import app.harven.partsmanager.dto.PartResponseDto;
import app.harven.partsmanager.mapper.DtoMapper;
import app.harven.partsmanager.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final DtoMapper dtoMapper;
    private final Boolean enabledAi;

    public Mono<Page<PartResponseDto>> findAll(String search, String type, String mounting, Pageable pageable) {
        Query countQuery = new Query();
        Query query = new Query();

        Criteria criteria = buildCriteria(search, type, mounting);
        if (criteria != null) {
            countQuery.addCriteria(criteria);
            query.addCriteria(criteria);
        }

        query.with(pageable);

        Mono<Long> totalCount = mongoTemplate.count(countQuery, Part.class);
        Mono<List<PartResponseDto>> dtoList = mongoTemplate.find(query, Part.class)
                .map(dtoMapper::toPartResponseDto)
                .collectList();

        return Mono.zip(totalCount, dtoList)
                .map(tuple -> new PageImpl<>(tuple.getT2(), pageable, tuple.getT1()));
    }

    public Flux<PartResponseDto> findAllList(String search, String type, String mounting, String sortBy) {
        Query query = new Query();
        Criteria criteria = buildCriteria(search, type, mounting);
        if (criteria != null) {
            query.addCriteria(criteria);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        if ("name".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else if ("quantity".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "quantity");
        }
        query.with(sort);
        query.limit(1000);

        return mongoTemplate.find(query, Part.class)
                .map(dtoMapper::toPartResponseDto);
    }

    private Criteria buildCriteria(String search, String type, String mounting) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (type != null && !type.trim().isEmpty()) {
            criteriaList.add(Criteria.where("type").is(type.trim()));
        }

        if (mounting != null && !mounting.trim().isEmpty()) {
            criteriaList.add(Criteria.where("mounting").is(mounting.trim()));
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim();
            Criteria textOrRegex = new Criteria().orOperator(
                    Criteria.where("name").regex(q, "i"),
                    Criteria.where("partNumber").regex(q, "i"),
                    Criteria.where("partCode").regex(q, "i"),
                    Criteria.where("location").regex(q, "i"),
                    Criteria.where("manufacturer").regex(q, "i"),
                    Criteria.where("type").regex(q, "i"),
                    Criteria.where("packageType").regex(q, "i"),
                    Criteria.where("description").regex(q, "i"),
                    Criteria.where("mounting").regex(q, "i")
            );
            criteriaList.add(textOrRegex);
        }

        if (criteriaList.isEmpty()) {
            return null;
        } else if (criteriaList.size() == 1) {
            return criteriaList.get(0);
        } else {
            return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }
    }

    public Mono<PartResponseDto> findById(String id) {
        return partRepository.findById(id)
                .map(dtoMapper::toPartResponseDto);
    }

    public Mono<PartResponseDto> saveOrUpdate(CreateOrUpdatePartDto dto) {
        if (dto.getPartNumber() != null && !dto.getPartNumber().trim().isEmpty()) {
            return partRepository.findByPartNumberIgnoreCase(dto.getPartNumber().trim())
                    .flatMap(part -> {
                        int currentQty = part.getQuantity() != null ? part.getQuantity() : 0;
                        int addedQty = dto.getQuantity() != null ? dto.getQuantity() : 1;
                        part.setQuantity(currentQty + addedQty);

                        if (dto.getName() != null) part.setName(dto.getName());
                        if (dto.getType() != null) part.setType(dto.getType());
                        if (dto.getManufacturer() != null) part.setManufacturer(dto.getManufacturer());
                        if (dto.getPartCode() != null && !dto.getPartCode().trim().isEmpty()) part.setPartCode(dto.getPartCode().trim());
                        if (dto.getLocation() != null && !dto.getLocation().trim().isEmpty()) part.setLocation(dto.getLocation().trim());
                        if (dto.getPackageType() != null) part.setPackageType(dto.getPackageType());
                        if (dto.getMounting() != null) part.setMounting(dto.getMounting());
                        if (dto.getDescription() != null) part.setDescription(dto.getDescription());

                        if (dto.getPhotoIds() != null && !dto.getPhotoIds().isEmpty()) {
                            List<String> combined = new ArrayList<>(part.getPhotoIds() != null ? part.getPhotoIds() : new ArrayList<>());
                            for (String pid : dto.getPhotoIds()) {
                                if (!combined.contains(pid)) {
                                    combined.add(pid);
                                }
                            }
                            part.setPhotoIds(combined);
                        }

                        if (dto.getMetadata() != null) {
                            Map<String, String> combinedMeta = new HashMap<>(part.getMetadata() != null ? part.getMetadata() : new HashMap<>());
                            combinedMeta.putAll(dto.getMetadata());
                            part.setMetadata(combinedMeta);
                        }

                        part.setUpdatedAt(Instant.now());
                        return partRepository.save(part);
                    })
                    .switchIfEmpty(Mono.defer(() -> createNewPart(dto)))
                    .map(dtoMapper::toPartResponseDto);
        }

        return createNewPart(dto)
                .map(dtoMapper::toPartResponseDto);
    }

    private Mono<Part> createNewPart(CreateOrUpdatePartDto dto) {
        Mono<String> partCodeMono;
        if (dto.getPartCode() != null && !dto.getPartCode().trim().isEmpty()) {
            partCodeMono = Mono.just(dto.getPartCode().trim());
        } else {
            partCodeMono = generateNextPartCode();
        }

        return partCodeMono.flatMap(code -> {
            Part part = Part.builder()
                    .name(dto.getName() != null ? dto.getName() : "Unknown component")
                    .type(dto.getType() != null ? dto.getType() : "Other")
                    .manufacturer(dto.getManufacturer())
                    .partNumber(dto.getPartNumber())
                    .partCode(code)
                    .location(dto.getLocation() != null ? dto.getLocation().trim() : null)
                    .packageType(dto.getPackageType())
                    .mounting(dto.getMounting() != null ? dto.getMounting() : "SMD")
                    .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                    .description(dto.getDescription())
                    .photoIds(dto.getPhotoIds() != null ? dto.getPhotoIds() : new ArrayList<>())
                    .metadata(dto.getMetadata() != null ? dto.getMetadata() : new HashMap<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            return partRepository.save(part);
        });
    }

    public Mono<String> generateNextPartCode() {
        return partRepository.count().map(count -> "#" + (count + 1));
    }

    public Mono<PartResponseDto> updatePart(String id, CreateOrUpdatePartDto dto) {
        return partRepository.findById(id).flatMap(part -> {
            part.setName(dto.getName());
            part.setType(dto.getType());
            part.setManufacturer(dto.getManufacturer());
            part.setPartNumber(dto.getPartNumber());
            if (dto.getPartCode() != null && !dto.getPartCode().trim().isEmpty()) {
                part.setPartCode(dto.getPartCode().trim());
            }
            part.setLocation(dto.getLocation() != null ? dto.getLocation().trim() : null);
            part.setPackageType(dto.getPackageType());
            part.setMounting(dto.getMounting());
            part.setQuantity(dto.getQuantity());
            part.setDescription(dto.getDescription());
            if (dto.getPhotoIds() != null) {
                part.setPhotoIds(dto.getPhotoIds());
            }
            if (dto.getMetadata() != null) {
                part.setMetadata(dto.getMetadata());
            }
            part.setUpdatedAt(Instant.now());
            return partRepository.save(part);
        }).map(dtoMapper::toPartResponseDto);
    }

    public Mono<PartResponseDto> adjustQuantity(String id, int delta) {
        return partRepository.findById(id).flatMap(part -> {
            int currentQty = part.getQuantity() != null ? part.getQuantity() : 0;
            int newQuantity = Math.max(0, currentQty + delta);
            part.setQuantity(newQuantity);
            part.setUpdatedAt(Instant.now());
            return partRepository.save(part);
        }).map(dtoMapper::toPartResponseDto);
    }

    public Mono<Void> deleteById(String id) {
        return partRepository.deleteById(id);
    }

    public Mono<DictionaryResponseDto> findAllDictionary() {
        return Mono.zip(
            partRepository.getAllParams().defaultIfEmpty(new PartParams(List.of())),
            partRepository.getPackagesAndManufacturers().defaultIfEmpty(new PartDictionaries(List.of(), List.of(), List.of())),
            generateNextPartCode()
        )
                .map(tuple -> {
                    List<String> staticComponents = List.of("Resistor","Capacitor","IC","Transistor","Diode","LED","Inductor","Connector","Sensor","Module","Other");
                    List<String> locations = tuple.getT2().getLocations() != null ? tuple.getT2().getLocations().stream().filter(Objects::nonNull).filter(s -> !s.isBlank()).toList() : List.of();
                    return new DictionaryResponseDto(
                            tuple.getT2().getManufacturers(),
                            tuple.getT2().getPackages(),
                            tuple.getT1().getParameters().stream().toList(),
                            staticComponents,
                            locations,
                            tuple.getT3(),
                            enabledAi);
                });
    }
}
