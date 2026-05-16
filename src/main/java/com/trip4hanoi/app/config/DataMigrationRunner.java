package com.trip4hanoi.app.config;

import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataMigrationRunner implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking for data migration: update search_vector for existing places");
        
        List<Place> placesToUpdate = placeRepository.findAll().stream()
                .filter(p -> p.getSearchVector() == null)
                .toList();

        if (!placesToUpdate.isEmpty()) {
            log.info("Updating search_vector for {} places...", placesToUpdate.size());
            for (Place place : placesToUpdate) {
                place.setSearchVector(StringUtil.generateSearchVector(
                        place.getName(),
                        place.getAddress(),
                        place.getCategory() != null ? place.getCategory().getName() : ""
                ));
            }
            placeRepository.saveAll(placesToUpdate);
            log.info("Data migration completed successfully.");
        } else {
            log.info("No places need search_vector update.");
        }
    }
}
