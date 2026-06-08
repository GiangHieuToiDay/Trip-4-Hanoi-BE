package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.common.ItineraryStatus;
import com.trip4hanoi.app.dto.req.*;
import com.trip4hanoi.app.dto.res.ItineraryPlaceResponse;
import com.trip4hanoi.app.dto.res.ItineraryResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PlaceResponse;
import com.trip4hanoi.app.entity.*;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.ItineraryMapper;
import com.trip4hanoi.app.mapper.ItineraryPlaceMapper;
import com.trip4hanoi.app.repository.*;
import com.trip4hanoi.app.service.ItineraryService;
import com.trip4hanoi.app.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ITINERARY-SERVICE")
public class ItineraryServiceImpl implements ItineraryService {
    private final ItineraryRepository itineraryRepository;
    private final ItineraryPlaceRepository itineraryPlaceRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final CategoryRepository categoryRepository;
    private final RecommendationService recommendationService; // Kết nối với dịch vụ gợi ý thông minh
    private final ItineraryMapper itineraryMapper;
    private final ItineraryPlaceMapper itineraryPlaceMapper;
    private final EventRepository eventRepository;
    private final UserLocationHistoryRepository userLocationHistoryRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    @Transactional
    public ItineraryResponse createItinerary(ItineraryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String trimmedTitle = request.getTitle() != null ? request.getTitle().trim() : "My Itinerary";
        
        // Luôn tạo đối tượng mới để mỗi lần tạo là một lịch trình riêng biệt
        Itinerary itinerary = itineraryMapper.toItinerary(request);
        itinerary.setTitle(trimmedTitle);
        itinerary.setUser(user);

        // Nếu là ADMIN/STAFF tạo thì đánh dấu là Featured (Lịch trình mẫu)
        boolean isAdminOrStaff = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("STAFF"));
        if (isAdminOrStaff) {
            if (Boolean.TRUE.equals(request.getIsSample())) {
                itinerary.setIsSample(true);
                itinerary.setIsFeatured(true); // Mặc định sample cũng là featured để dễ promote
            }
        } else {
            // User thường không được tự set isSample
            itinerary.setIsSample(false);
        }

        itinerary.setDescription(request.getDescription());
        itinerary.setCoverImage(request.getCoverImage());

        // Handle Status
        if (isAdminOrStaff && Boolean.TRUE.equals(itinerary.getIsSample())) {
            // Admin tạo sample: Nếu có gửi status thì dùng, không thì mặc định DRAFT
            if (request.getStatus() != null) {
                try {
                    itinerary.setStatus(ItineraryStatus.valueOf(request.getStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    itinerary.setStatus(ItineraryStatus.DRAFT);
                }
            } else {
                itinerary.setStatus(ItineraryStatus.DRAFT);
            }
        } else {
            // User tạo hoặc Admin tạo itinerary thường: Mặc định PUBLISHED
            itinerary.setStatus(ItineraryStatus.PUBLISHED);
        }

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
        long totalBudget = request.getBudget() != null ? request.getBudget() : 1000000;
        
        double remainingTotalBudget = totalBudget;
        double dailyBudgetLimit = (double) totalBudget / numDays;

        //Chỉ lấy những địa điểm chưa bị xóa
        List<Place> allPossiblePlaces = placeRepository.findAllByDeletedFalse();

        // Gợi ý thông minh
        List<?> rawRecommendations = recommendationService.getPersonalizedRecommendations(100);
        List<PlaceResponse> recommendedPlaces = rawRecommendations.stream()
                .map(item -> objectMapper.convertValue(item, PlaceResponse.class))
                .collect(Collectors.toList());
        
        Set<Long> recommendedIds = recommendedPlaces.stream().map(PlaceResponse::getId).collect(Collectors.toSet());

        // Lấy vị trí gần nhất của user làm điểm khởi hành
        var history = userLocationHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Double userLat = history.isEmpty() ? null : history.get(0).getLatitude();
        Double userLon = history.isEmpty() ? null : history.get(0).getLongitude();

        List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
        List<Place> usedPlaces = new java.util.ArrayList<>();
        Random random = new Random();

        String[] sessionNames = {"Morning", "Noon", "Afternoon", "Evening"};
        
        // Theo dõi toàn bộ chuyến đi
        Map<String, Integer> tripCategoryCounts = new HashMap<>();
        Set<String> lastDayCategories = new HashSet<>();

        for (int day = 1; day <= numDays; day++) {
            int orderInDay = 1;
            Double lastLat = (day == 1) ? userLat : (itineraryPlaces.isEmpty() ? null : itineraryPlaces.get(itineraryPlaces.size()-1).getPlace().getLatitude());
            Double lastLon = (day == 1) ? userLon : (itineraryPlaces.isEmpty() ? null : itineraryPlaces.get(itineraryPlaces.size()-1).getPlace().getLongitude());
            
            Map<String, Integer> dailyCategoryCounts = new HashMap<>();
            Set<String> currentDayCategories = new HashSet<>();

            for (String session : sessionNames) {
                // Mỗi buổi tối đa 2 địa điểm để đảm bảo sức khỏe và ngân sách
                int placesPerSession = 2;

                for (int p = 0; p < placesPerSession; p++) {
                    final String currentSession = session;
                    final int slotIndex = p;
                    final Double currentLastLat = lastLat;
                    final Double currentLastLon = lastLon;
                    final Set<String> finalLastDayCategories = lastDayCategories;
                    final double currentRemainingBudget = remainingTotalBudget;
                    
                    // Xác định target category cho từng slot
                    List<String> slotTargets = getStrictTargets(currentSession, slotIndex);
                    
                    // Filter candidates with diversity and budget constraints
                    List<Place> candidates = allPossiblePlaces.stream()
                            .filter(pl -> !usedPlaces.contains(pl))
                            .filter(pl -> {
                                if (pl.getCategory() == null || pl.getCategory().getName() == null) return false;
                                String cat = pl.getCategory().getName();
                                
                                // --- Diversity Rules 2.0 (Trip-wide & Contextual) ---
                                
                                // 1. Dịch vụ đặc thù: Tối đa 1 lần/CHUYẾN ĐI (Spa, Cinema, Workshop)
                                if (List.of("Spa", "Cinema", "Workshop").stream().anyMatch(cat::contains) 
                                    && tripCategoryCounts.getOrDefault(cat, 0) >= 1) return false;

                                // 2. Điểm tham quan/tâm linh (Đền chùa, Di tích): 
                                // Tối đa 1 lần/NGÀY và KHÔNG đi 2 ngày liên tiếp để tránh nhàm chán
                                if (List.of("Đền chùa", "Di tích", "Lịch sử").stream().anyMatch(cat::contains)) {
                                    if (dailyCategoryCounts.getOrDefault(cat, 0) >= 1) return false;
                                    if (finalLastDayCategories.contains(cat)) return false; 
                                }

                                // 3. Cafe: Tối đa 1 lần/ngày
                                if (cat.contains("Cafe") && dailyCategoryCounts.getOrDefault(cat, 0) >= 1) return false;

                                // 4. Thiên nhiên (Hồ, Công viên): Tối đa 1 lần/ngày
                                if (List.of("Hồ", "Công viên").stream().anyMatch(cat::contains) 
                                    && dailyCategoryCounts.getOrDefault(cat, 0) >= 1) return false;

                                // 5. Một category không xuất hiện quá 2 lần/ngày (trừ Đồ ăn)
                                if (!cat.contains("Restaurant") && dailyCategoryCounts.getOrDefault(cat, 0) >= 2) return false;
                                
                                boolean matchesTarget = slotTargets.stream().anyMatch(t -> cat.toLowerCase().contains(t.toLowerCase()) || t.toLowerCase().contains(cat.toLowerCase()));
                                return matchesTarget;
                            })
                            .collect(Collectors.toList());

                    // Fallback nếu không có target
                    if (candidates.isEmpty()) {
                        candidates = allPossiblePlaces.stream()
                                .filter(pl -> !usedPlaces.contains(pl))
                                .filter(pl -> {
                                    if (pl.getCategory() == null || pl.getCategory().getName() == null) return false;
                                    String cat = pl.getCategory().getName();
                                    return dailyCategoryCounts.getOrDefault(cat, 0) < 1 && tripCategoryCounts.getOrDefault(cat, 0) < 3;
                                })
                                .limit(20)
                                .collect(Collectors.toList());
                    }

                    if (candidates.isEmpty()) break;

                    LocalDate travelDate = (request.getStartDate() != null) ? request.getStartDate().plusDays(day -1) : LocalDate.now().plusDays(day-1);
                    
                    // Scoring logic
                    List<PlaceScore> scoredPlaces = candidates.stream()
                            .map(pl -> {
                                String plCatName = (pl.getCategory() != null) ? pl.getCategory().getName() : "";
                                double prefMatch = preferredCategoryNames.stream().anyMatch(c -> c.equalsIgnoreCase(plCatName)) ? 1.0 : 0.0;
                                double ratingScore = (pl.getRatingAvg() != null ? pl.getRatingAvg() : 0.0) / 5.0;
                                
                                int price = pl.getPriceAvg() != null ? pl.getPriceAvg() : 0;
                                double costForGroup = (double) price * numPeople;
                                
                                // Budget FIT Score
                                double budgetFit;
                                if (costForGroup > currentRemainingBudget) {
                                    budgetFit = -3.0; // Penalty cực nặng
                                } else {
                                    double targetPrice = dailyBudgetLimit / 8.0; 
                                    budgetFit = 1.0 - Math.min(1.0, costForGroup / (targetPrice * 2 + 1));
                                }

                                double recBonus = recommendedIds.contains(pl.getId()) ? 1.0 : 0.0;

                                double distanceScore = 0.0;
                                if (currentLastLat != null && currentLastLon != null) {
                                    Double dist = calculateDistance(currentLastLat, currentLastLon, pl.getLatitude(), pl.getLongitude());
                                    if (dist != null) {
                                        distanceScore = 1.0 / (1.0 + dist/2.0);
                                    }
                                }

                                double totalScore = 0.2 * prefMatch + 0.1 * ratingScore + 0.5 * budgetFit + 0.3 * distanceScore + 0.1 * recBonus;

                                double eventBonus = 0.0;
                                Event foundEvent = null;
                                List<Event>  events = eventRepository.findByPlaceId(pl.getId());
                                for (Event ev : events) {
                                    if(!travelDate.isBefore(ev.getStartTime().toLocalDate()) && !travelDate.isAfter(ev.getEndTime().toLocalDate())) {
                                        eventBonus = 0.5;
                                        foundEvent = ev;
                                        break;
                                    }
                                }
                                return new PlaceScore(pl, totalScore + eventBonus, foundEvent);
                            })
                            .sorted(Comparator.comparingDouble(PlaceScore::getScore).reversed())
                            .limit(5)
                            .collect(Collectors.toList());

                    if (scoredPlaces.isEmpty()) break;

                    PlaceScore chosenWrapper = (scoredPlaces.get(0).getScore() > 0) ? scoredPlaces.get(0) : scoredPlaces.get(random.nextInt(scoredPlaces.size()));
                    Place foundPlace = chosenWrapper.getPlace();
                    Event activeEvent = chosenWrapper.getActiveEvent();

                    usedPlaces.add(foundPlace);
                    lastLat = foundPlace.getLatitude();
                    lastLon = foundPlace.getLongitude();

                    String chosenCat = foundPlace.getCategory().getName();
                    dailyCategoryCounts.put(chosenCat, dailyCategoryCounts.getOrDefault(chosenCat, 0) + 1);
                    tripCategoryCounts.put(chosenCat, tripCategoryCounts.getOrDefault(chosenCat, 0) + 1);
                    currentDayCategories.add(chosenCat);
                    
                    int totalPlaceCost = (foundPlace.getPriceAvg() != null ? foundPlace.getPriceAvg() : 0) * numPeople;
                    remainingTotalBudget -= totalPlaceCost;

                    ItineraryPlace itineraryPlace = ItineraryPlace.builder()
                            .itinerary(itinerary)
                            .place(foundPlace)
                            .event(activeEvent)
                            .dayNumber(day)
                            .orderIndex(orderInDay++)
                            .session(session)
                            .estimatedCost(totalPlaceCost)
                            .build();
                    itineraryPlaces.add(itineraryPlace);
                }
            }
            lastDayCategories = currentDayCategories;
        }

        itinerary.setItineraryPlaces(itineraryPlaces);
        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        return itineraryMapper.toItineraryResponse(savedItinerary);
    }

    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 100.0) / 100.0;
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
        private final Event activeEvent; //Trường này lưu event tìm được

        public PlaceScore(Place place, double score, Event activeEvent) { this.place = place; this.score = score;  this.activeEvent = activeEvent;}
        public Place getPlace() { return place; }
        public double getScore() { return score; }
        public Event getActiveEvent() { return activeEvent; }
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
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("MODERATE_CONTENT"));

        if (isAdmin) {
            // Admin chỉ được xóa lịch trình do hệ thống tạo (featured)
            if (!Boolean.TRUE.equals(itinerary.getIsFeatured())) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        } else {
            // User thường chỉ được xóa lịch trình của chính mình
            if (!itinerary.getUser().getId().equals(currentUserId)) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
        
        itineraryRepository.deleteById(itineraryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getFeaturedItineraries() {
        return itineraryRepository.findByIsFeaturedTrue().stream()
                .filter(i -> i.getStatus() == ItineraryStatus.PUBLISHED)
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
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
        if (request.getDescription() != null) {
            itinerary.setDescription(request.getDescription());
        }
        if (request.getCoverImage() != null) {
            itinerary.setCoverImage(request.getCoverImage());
        }

        // Cập nhật Status & isSample (Chỉ Admin/Staff mới có quyền)
        Long currentUserId = getCurrentUserId();
        User user = userRepository.findById(currentUserId).orElse(null);
        boolean isAdminOrStaff = user != null && user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("STAFF"));

        if (isAdminOrStaff) {
            if (request.getIsSample() != null) {
                itinerary.setIsSample(request.getIsSample());
                if (Boolean.TRUE.equals(request.getIsSample())) {
                    itinerary.setIsFeatured(true);
                }
            }
            if (request.getStatus() != null) {
                try {
                    itinerary.setStatus(ItineraryStatus.valueOf(request.getStatus().toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
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
    @Transactional
    public ItineraryResponse saveAIItinerary(SaveAIItineraryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Itinerary itinerary = Itinerary.builder()
                .title(request.getTitle())
                .user(user)
                .budget(0) // Will calculate below
                .days(1)
                .numberOfPeople(1)
                .build();

        itinerary = itineraryRepository.save(itinerary);

        List<ItineraryPlace> itineraryPlaces = new ArrayList<>();
        int totalCost = 0;
        int orderIndex = 1;

        if (request.getTimeline() != null) {
            for (var item : request.getTimeline()) {
                if (item.getPlaceId() == null) continue;

                Place place = placeRepository.findById(item.getPlaceId())
                        .orElse(null);

                if (place == null) continue;

                int cost = parseEstimatedCost(item.getEstimatedCost(), place.getPriceAvg());

                totalCost += cost;

                // Map time to session
                String session = "Morning";
                if (item.getTime() != null) {
                    String timeStr = item.getTime().toLowerCase();
                    if (timeStr.contains("11:") || timeStr.contains("12:") || timeStr.contains("13:")) session = "Noon";
                    else if (timeStr.contains("14:") || timeStr.contains("15:") || timeStr.contains("16:") || timeStr.contains("17:")) session = "Afternoon";
                    else if (timeStr.contains("18:") || timeStr.contains("19:") || timeStr.contains("20:") || timeStr.contains("21:") || timeStr.contains("22:")) session = "Evening";
                }

                ItineraryPlace ip = ItineraryPlace.builder()
                        .itinerary(itinerary)
                        .place(place)
                        .dayNumber(1)
                        .orderIndex(orderIndex++)
                        .session(session)
                        .estimatedCost(cost)
                        .build();

                itineraryPlaces.add(ip);
            }
        }

        itineraryPlaceRepository.saveAll(itineraryPlaces);
        itinerary.setBudget(totalCost);
        itinerary.setItineraryPlaces(itineraryPlaces);
        itineraryRepository.save(itinerary);

        return itineraryMapper.toItineraryResponse(itinerary);
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
    public ItineraryResponse cloneItinerary(Long itineraryId, Long userId) {

        Itinerary old = itineraryRepository.findByIdWithPlaces(itineraryId);

        if (old == null) {
            throw new AppException(ErrorCode.PLAN_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Itinerary clone = Itinerary.builder()
                .title(old.getTitle() + " (Copy)")
                .description(old.getDescription())
                .coverImage(old.getCoverImage())
                .budget(old.getBudget())
                .days(old.getDays())
                .numberOfPeople(old.getNumberOfPeople())
                .user(user)
                .isSample(false) // Clone của user không bao giờ là sample
                .isFeatured(false)
                .status(ItineraryStatus.PUBLISHED)
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ItineraryResponse> getAllItinerariesAdmin(int page, int size, String keyword, Boolean isSample, String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        ItineraryStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ItineraryStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        // Sử dụng hàm search tập trung để xử lý tất cả filter cùng lúc (keyword, isSample, status)
        Page<Itinerary> itineraryPage = itineraryRepository.searchItinerariesAdmin(
                (keyword != null && !keyword.isBlank()) ? keyword : null, 
                isSample, 
                statusEnum, 
                pageable);

        List<ItineraryResponse> content = itineraryPage.getContent().stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());

        return PageResponse.<ItineraryResponse>builder()
                .pageNumber(page)
                .pageSize(size)
                .totalElements(itineraryPage.getTotalElements())
                .totalPages(itineraryPage.getTotalPages())
                .data(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getSampleItineraries() {
        return itineraryRepository.findByIsSampleTrueAndStatus(ItineraryStatus.PUBLISHED).stream()
                .map(itineraryMapper::toItineraryResponse)
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object idClaim = jwt.getClaims().get("id");
            if (idClaim instanceof Number n) {
                return n.longValue();
            }
        }
        return 0L;
    }

    /**
     * Helper to parse estimated cost from AI (which might contain "VND", dots, etc.)
     */
    private int parseEstimatedCost(String costStr, Integer defaultCost) {
        if (costStr == null || costStr.isBlank()) {
            return defaultCost != null ? defaultCost : 0;
        }
        try {
            // Remove non-digit characters (dots, commas, currency symbols)
            String digitsOnly = costStr.replaceAll("[^\\d]", "");
            if (digitsOnly.isEmpty()) return defaultCost != null ? defaultCost : 0;
            return Integer.parseInt(digitsOnly);
        } catch (Exception e) {
            log.warn(">>> Could not parse estimated cost: {}. Using default: {}", costStr, defaultCost);
            return defaultCost != null ? defaultCost : 0;
        }
    }


}
