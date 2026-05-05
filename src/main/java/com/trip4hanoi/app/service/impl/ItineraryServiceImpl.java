package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.ItineraryDayRequest;
import com.trip4hanoi.app.dto.req.ItineraryPlaceRequest;
import com.trip4hanoi.app.dto.req.ItineraryRequest;
import com.trip4hanoi.app.dto.req.ItineraryUpdateFullRequest;
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

        if( itineraryRepository.existsByTitleIgnoreCase(request.getTitle())) {
            throw new AppException(ErrorCode.TITLE_EXIST);
        }

        String trimmedTitle = request.getTitle() != null ? request.getTitle().trim() : "My Itinerary";
        Itinerary itinerary = itineraryRepository.findByUserIdAndTitleIgnoreCase(userId, trimmedTitle)
                .orElseGet(() -> {
                    Itinerary newItinerary = itineraryMapper.toItinerary(request);
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

    @Transactional
    public ItineraryResponse addPlaceToItinerary(ItineraryPlaceRequest request) {

        Itinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        int day = request.getDayNumber();

        if (day < 1 || day > itinerary.getDays()) {
            throw new AppException(ErrorCode.DAYS_INVALID);
        }

        int newCost = (place.getPriceAvg() != null ? place.getPriceAvg() : 0)
                * (itinerary.getNumberOfPeople() != null ? itinerary.getNumberOfPeople() : 1);

        int currentTotal = itineraryPlaceRepository.sumEstimatedCostByItineraryId(itinerary.getId());

        if (currentTotal + newCost > itinerary.getBudget()) {
            throw new AppException(ErrorCode.BUDGET_EXCEEDED);
        }

        if (itineraryPlaceRepository.existsByItineraryIdAndPlaceId(
                request.getItineraryId(),
                request.getPlaceId())) {

            throw new AppException(ErrorCode.PLACE_ALREADY_EXISTS);
        }

        int count = itineraryPlaceRepository
                .countByItineraryIdAndDayNumber(itinerary.getId(), day);

        int orderIndex = request.getOrderIndex();

        if (orderIndex < 1 || orderIndex > count + 1) {
            throw new AppException(ErrorCode.INVALID_ORDER_INDEX);
        }

        itineraryPlaceRepository.shiftOrderIndex(
                itinerary.getId(),
                day,
                orderIndex
        );

        String session;
        if (orderIndex <= 2) session = "Morning";
        else if (orderIndex <= 4) session = "Noon";
        else if (orderIndex <= 6) session = "Afternoon";
        else session = "Evening";

        ItineraryPlace newPlace = ItineraryPlace.builder()
                .itinerary(itinerary)
                .place(place)
                .dayNumber(day)
                .orderIndex(orderIndex)
                .session(session)
                .estimatedCost(
                        (place.getPriceAvg() != null ? place.getPriceAvg() : 0)
                                * (itinerary.getNumberOfPeople() != null ? itinerary.getNumberOfPeople() : 1)
                )
                .build();

        itineraryPlaceRepository.save(newPlace);
        Itinerary updated = itineraryRepository.findByIdWithPlaces(itinerary.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }

    public int getRemainingBudget(Long itineraryId) {

        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));

        int totalCost = itineraryPlaceRepository.sumEstimatedCostByItineraryId(itineraryId);

        return itinerary.getBudget() - totalCost;
    }

    @Transactional
    public ItineraryResponse updatePlaceInItinerary(ItineraryPlaceRequest request) {

        ItineraryPlace existing = itineraryPlaceRepository.findById(request.getItineraryPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PLACE_NOT_FOUND));

        Itinerary itinerary = existing.getItinerary();

        Place place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

        int newDay = request.getDayNumber();

        if (newDay < 1 || newDay > itinerary.getDays()) {
            throw new AppException(ErrorCode.DAYS_INVALID);
        }

        int newOrderIndex = request.getOrderIndex();

        itineraryPlaceRepository.decreaseOrderIndex(
                itinerary.getId(),
                existing.getDayNumber(),
                existing.getOrderIndex()
        );

        int count = itineraryPlaceRepository
                .countByItineraryIdAndDayNumber(itinerary.getId(), newDay);

        if (newOrderIndex < 1 || newOrderIndex > count + 1) {
            throw new AppException(ErrorCode.INVALID_ORDER_INDEX);
        }

        itineraryPlaceRepository.shiftOrderIndex(
                itinerary.getId(),
                newDay,
                newOrderIndex
        );

        existing.setPlace(place);
        existing.setDayNumber(newDay);
        existing.setOrderIndex(newOrderIndex);

        String session;
        if (newOrderIndex <= 2) session = "Morning";
        else if (newOrderIndex <= 4) session = "Noon";
        else if (newOrderIndex <= 6) session = "Afternoon";
        else session = "Evening";

        existing.setSession(session);

        existing.setEstimatedCost(
                (place.getPriceAvg() != null ? place.getPriceAvg() : 0)
                        * (itinerary.getNumberOfPeople() != null ? itinerary.getNumberOfPeople() : 1)
        );

        itineraryPlaceRepository.save(existing);

        Itinerary updated = itineraryRepository.findByIdWithPlaces(itinerary.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }

    @Transactional
    public ItineraryResponse removePlaceFromItinerary(Long itineraryPlaceId) {

        ItineraryPlace existing = itineraryPlaceRepository.findById(itineraryPlaceId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PLACE_NOT_FOUND));

        Itinerary itinerary = existing.getItinerary();

        int day = existing.getDayNumber();
        int orderIndex = existing.getOrderIndex();


        itineraryPlaceRepository.delete(existing);

        itineraryPlaceRepository.decreaseOrderIndex(
                itinerary.getId(),
                day,
                orderIndex
        );

        Itinerary updated = itineraryRepository.findByIdWithPlaces(itinerary.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }

    @Override
    public void deleteItinerary(Long itineraryId) {
        itineraryRepository.deleteById(itineraryId);
    }

    @Override
    public List<ItineraryResponse> getUserItineraries(Long userId) {

        if( itineraryRepository.findByUserId(userId) == null ) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }

        return itineraryRepository.findByUserId(userId).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItineraryResponse updateItinerary(ItineraryRequest request,long id) {

        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));


        if (request.getTitle() != null) {
            itinerary.setTitle(request.getTitle());
        }
        if (request.getDays() != null) {
            itinerary.setDays(request.getDays());
        }
        if (request.getBudget() != null) {
            itinerary.setBudget(request.getBudget());
        }
        if (request.getNumberOfPeople() != null) {
            itinerary.setNumberOfPeople(request.getNumberOfPeople());
        }

        return itineraryMapper.toItineraryResponse(itineraryRepository.save(itinerary));
    }

    @Transactional
    public ItineraryResponse updateFull(ItineraryUpdateFullRequest request) {

        Itinerary itinerary = itineraryRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));


        itinerary.setTitle(request.getTitle());
        itinerary.setBudget(request.getBudget());
        itinerary.setDays(request.getDays());
        itinerary.setNumberOfPeople(request.getNumberOfPeople());

        itineraryRepository.save(itinerary);


        itineraryPlaceRepository.deleteByItineraryId(itinerary.getId());


        for (ItineraryDayRequest dayReq : request.getItineraryDays()) {

            int day = dayReq.getDayNumber();

            for (ItineraryPlaceRequest p : dayReq.getPlaces()) {

                Place place = placeRepository.findById(p.getPlaceId())
                        .orElseThrow(() -> new AppException(ErrorCode.PLACE_NOT_FOUND));

                String session;
                if (p.getOrderIndex() <= 2) session = "Morning";
                else if (p.getOrderIndex() <= 4) session = "Noon";
                else if (p.getOrderIndex() <= 6) session = "Afternoon";
                else session = "Evening";

                ItineraryPlace entity = ItineraryPlace.builder()
                        .itinerary(itinerary)
                        .place(place)
                        .dayNumber(day)
                        .orderIndex(p.getOrderIndex())
                        .session(session)
                        .estimatedCost(
                                (place.getPriceAvg() != null ? place.getPriceAvg() : 0)
                                        * (itinerary.getNumberOfPeople() != null ? itinerary.getNumberOfPeople() : 1)
                        )
                        .build();

                itineraryPlaceRepository.save(entity);
            }
        }

        Itinerary updated = itineraryRepository.findByIdWithPlaces(itinerary.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }

    @Override
    public ItineraryResponse getDetail(long id) {
        Itinerary itinerary = itineraryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        return itineraryMapper.toItineraryResponse(itinerary);
    }

    @Override
    @Transactional
    public ItineraryResponse reorderPlace(Long itineraryPlaceId, int newDay, int newOrderIndex) {

        ItineraryPlace item = itineraryPlaceRepository.findById(itineraryPlaceId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_PLACE_NOT_FOUND));

        Itinerary itinerary = item.getItinerary();

        int oldDay = item.getDayNumber();
        int oldIndex = item.getOrderIndex();


        itineraryPlaceRepository.decreaseOrderIndex(
                itinerary.getId(),
                oldDay,
                oldIndex
        );

        int count = itineraryPlaceRepository
                .countByItineraryIdAndDayNumber(itinerary.getId(), newDay);

        if (newOrderIndex < 1 || newOrderIndex > count + 1) {
            throw new AppException(ErrorCode.INVALID_ORDER_INDEX);
        }


        itineraryPlaceRepository.shiftOrderIndex(
                itinerary.getId(),
                newDay,
                newOrderIndex
        );


        item.setDayNumber(newDay);
        item.setOrderIndex(newOrderIndex);

        String session;
        if (newOrderIndex <= 2) session = "Morning";
        else if (newOrderIndex <= 4) session = "Noon";
        else if (newOrderIndex <= 6) session = "Afternoon";
        else session = "Evening";

        item.setSession(session);

        itineraryPlaceRepository.save(item);

        Itinerary updated = itineraryRepository.findByIdWithPlaces(itinerary.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }


    @Override
    @Transactional
    public ItineraryResponse cloneItinerary(Long itineraryId) {

        Itinerary old = itineraryRepository.findByIdWithPlaces(itineraryId);

        if (old == null) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }

        Itinerary clone = Itinerary.builder()
                .title(old.getTitle() + " (Copy)")
                .budget(old.getBudget())
                .days(old.getDays())
                .numberOfPeople(old.getNumberOfPeople())
                .user(old.getUser())
                .build();

        itineraryRepository.save(clone);

        List<ItineraryPlace> newPlaces = old.getItineraryPlaces().stream()
                .map(p -> ItineraryPlace.builder()
                        .itinerary(clone)
                        .place(p.getPlace())
                        .dayNumber(p.getDayNumber())
                        .orderIndex(p.getOrderIndex())
                        .session(p.getSession())
                        .estimatedCost(p.getEstimatedCost())
                        .build()
                ).toList();

        itineraryPlaceRepository.saveAll(newPlaces);

        Itinerary updated = itineraryRepository.findByIdWithPlaces(clone.getId());
        return itineraryMapper.toItineraryResponse(updated);
    }


}
