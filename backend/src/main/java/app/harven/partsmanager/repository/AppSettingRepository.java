package app.harven.partsmanager.repository;

import app.harven.partsmanager.domain.AppSetting;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingRepository extends ReactiveMongoRepository<AppSetting, String> {
}
