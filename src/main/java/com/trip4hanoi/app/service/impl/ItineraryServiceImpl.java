package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.ItineraryMapper;
import com.trip4hanoi.app.mapper.ItineraryPlaceMapper;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryPlaceRepository itineraryPlaceRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final CategoryRepository categoryRepository;
    private final ItineraryMapper itineraryMapper;
    private final ItineraryPlaceMapper itineraryPlaceMapper;

    @Override
    @Transactional
    public ItineraryResponse createItinerary(ItineraryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String trimmedTitle = request.getTitle() != null ? request.getTitle().trim() : "My Itinerary";
        Itinerary itinerary = itineraryRepository.findByUserIdAndTitleIgnoreCase(userId, trimmedTitle)
                .orElseGet(() -> {
                    Itinerary newItinerary = itineraryMapper.toItineraryEntity(request);
                    newItinerary.setTitle(trimmedTitle);
                    newItinerary.setUser(user);
                    return newItinerary;
                });

        itinerary.setBudget(request.getBudget());
        itinerary.setDays(request.getDays());
        itinerary.setNumberOfPeople(request.getNumberOfPeople() != null ? request.getNumberOfPeople() : 1);

        if (itinerary.getId() != null) {
            itineraryPlaceRepository.deleteByItineraryId(itinerary.getId());
            if (itinerary.getItineraryPlaces() != null) {
                itinerary.getItineraryPlaces().clear();
            } else {
                itinerary.setItineraryPlaces(new java.util.ArrayList<>());
            }
        }

        List<String> preferredCategoryNames;
        if (request.getCategoryNames() != null && !request.getCategoryNames().isEmpty()) {
            preferredCategoryNames = request.getCategoryNames();
        } else {
            preferredCategoryNames = userPreferenceRepository.findByUserId(userId).stream()
                    .map(up -> up.getCategory().getName())
                    .collect(Collectors.toList());
        }

        int numDays = request.getDays() != null ? request.getDays() : 1;
        int numPeople = request.getNumberOfPeople() != null ? request.getNumberOfPeople() : 1;
        int totalBudget = request.getBudget() != null ? request.getBudget() : 1000000;
        
        double dailyBudget = (double) totalBudget / numDays;
        double budgetPerPersonPerDay = dailyBudget / numPeople;
        
        List<Place> allPossiblePlaces = placeRepository.findAll();
        List<ItineraryPlace> itineraryPlaces = new java.util.ArrayList<>();
        List<Place> usedPlaces = new java.util.ArrayList<>();
        java.util.Random random = new java.util.Random();

        String[] sessionNames = {"Morning", "Noon", "Afternoon", "Evening"};

        for (int day = 1; day <= numDays; day++) {
            int orderInDay = 1;
            for (String session : sessionNames) {
                double sessionBudgetRatio = session.equals("Evening") ? 0.35 : 0.216;
                double currentSessionBudget = dailyBudget * sessionBudgetRatio;
                double budgetPerPlaceTarget = currentSessionBudget / (numPeople * 3.0);

                // Ép buộc ít nhất 2 địa điểm mỗi buổi để đảm bảo có cả Ăn và Chơi
                int placesPerSession = (numPeople >= 3) ? 3 : 2;

                String lastCategoryName = "";
                java.util.Map<String, Integer> sessionCategoryCounts = new java.util.HashMap<>();

                for (int p = 0; p < placesPerSession; p++) {
                    final String finalLastCategory = lastCategoryName;
                    final int slotIndex = p;
                    final String currentSession = session;
                    
                    // Xác định target category cho từng slot
                    List<String> slotTargets = getStrictTargets(currentSession, slotIndex);
                    
                    // Filter
                    List<Place> candidates = allPossiblePlaces.stream()
                            .filter(pl -> !usedPlaces.contains(pl))
                            .filter(pl -> {
                                String cat = pl.getCategory().getName().toLowerCase();
                                boolean matchesTarget = slotTargets.stream().anyMatch(t -> cat.contains(t.toLowerCase()) || t.toLowerCase().contains(cat));
                                int maxPerSession = 1; // Mỗi loại chỉ xuất hiện 1 lần/buổi
                                return matchesTarget && sessionCategoryCounts.getOrDefault(pl.getCategory().getName(), 0) < maxPerSession;
                            })
                            .collect(Collectors.toList());

                    // Fallback nếu không có target (nhưng vẫn loại trừ category vừa đi)
                    if (candidates.isEmpty()) {
                        candidates = allPossiblePlaces.stream()
                                .filter(pl -> !usedPlaces.contains(pl))
                                .filter(pl -> !pl.getCategory().getName().equalsIgnoreCase(finalLastCategory))
                                .collect(Collectors.toList());
                    }

                    if (candidates.isEmpty()) break;

                    // Scoring
                    List<PlaceScore> scoredPlaces = candidates.stream()
                            .map(pl -> {
                                double prefMatch = preferredCategoryNames.stream().anyMatch(c -> c.equalsIgnoreCase(pl.getCategory().getName())) ? 1.0 : 0.0;
                                double ratingScore = (pl.getRatingAvg() != null ? pl.getRatingAvg() : 0.0) / 5.0;
                                int price = pl.getPriceAvg() != null ? pl.getPriceAvg() : 0;
                                double budgetFit = 1.0 - Math.min(1.0, Math.abs(price - budgetPerPlaceTarget) / (budgetPerPlaceTarget + 1));
                                
                                double totalScore = 0.4 * prefMatch + 0.3 * ratingScore + 0.3 * budgetFit;
                                return new PlaceScore(pl, totalScore);
                            })
                            .sorted(Comparator.comparingDouble(PlaceScore::getScore).reversed())
                            .limit(5)
                            .collect(Collectors.toList());

                    Place foundPlace = scoredPlaces.get(random.nextInt(scoredPlaces.size())).getPlace();
                    
                    usedPlaces.add(foundPlace);
                    String chosenCat = foundPlace.getCategory().getName();
                    lastCategoryName = chosenCat;
                    sessionCategoryCounts.put(chosenCat, sessionCategoryCounts.getOrDefault(chosenCat, 0) + 1);
                    
                    int totalPlaceCost = (foundPlace.getPriceAvg() != null ? foundPlace.getPriceAvg() : 0) * numPeople;

                    ItineraryPlace itineraryPlace = ItineraryPlace.builder()
                            .itinerary(itinerary)
                            .place(foundPlace)
                            .dayNumber(day)
                            .orderIndex(orderInDay++)
                            .session(session)
                            .estimatedCost(totalPlaceCost)
                            .build();
                    itineraryPlaces.add(itineraryPlace);
                }
            }
        }

        itinerary.setItineraryPlaces(itineraryPlaces);
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        return itineraryMapper.toItineraryResponse(savedItinerary);
    }

    private List<String> getStrictTargets(String session, int slotIndex) {
        switch (session) {
            case "Morning":
                return (slotIndex == 0) ? List.of("Food", "Breakfast", "Pho", "Banh mi") : List.of("Museum", "Temple", "Pagoda", "Attraction", "Sightseeing", "History");
            case "Noon":
                return (slotIndex == 0) ? List.of("Food", "Lunch", "Restaurant") : List.of("Cafe", "Coffee", "Tea");
            case "Afternoon":
                return (slotIndex == 0) ? List.of("Travel", "Attraction", "Sightseeing", "Park") : List.of("Workshop", "Art", "Culture", "Craft");
            case "Evening":
                return (slotIndex == 0) ? List.of("Food", "Dinner", "Street Food") : List.of("Cinema", "Bar", "Pub", "Entertainment", "Music", "Cafe");
            default:
                return List.of();
        }
    }


    private static class PlaceScore {
        private final Place place;
        private final double score;
        public PlaceScore(Place place, double score) { this.place = place; this.score = score; }
        public Place getPlace() { return place; }
        public double getScore() { return score; }
    }


    @Override
    @Transactional
    public ItineraryPlaceResponse addPlaceToItinerary(ItineraryPlaceRequest request) {
        Itinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        ItineraryPlace itineraryPlace = itineraryPlaceMapper.toItineraryPlaceEntity(request);
        itineraryPlace.setItinerary(itinerary);
        itineraryPlace.setPlace(place);

        return itineraryPlaceMapper.toItineraryPlaceResponse(itineraryPlaceRepository.save(itineraryPlace));
    }

    @Override
    public List<ItineraryResponse> getUserItineraries(Long userId) {
        return itineraryRepository.findByUserId(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
    }
}
