package io.github.kaltrinabajramii.urbantransitbackend.dataseeder;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.*;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.UserRole;
import io.github.kaltrinabajramii.urbantransitbackend.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final TicketPricingRepository ticketPricingRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      RouteRepository routeRepository,
                      TicketPricingRepository ticketPricingRepository,
                      TicketRepository ticketRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.ticketPricingRepository = ticketPricingRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        seedUsers();
        seedPricing();
        seedRoutes();
        seedSampleTickets();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            List<User> users = List.of(
                    // Admin users
                    createUser("admin@urbantransit.com", "System Administrator", "admin123", UserRole.ADMIN),
                    createUser("manager@urbantransit.com", "Transit Manager", "manager123", UserRole.ADMIN),

                    // Regular users
                    createUser("demo@user.com", "Demo User", "demo123", UserRole.USER),
                    createUser("john.doe@email.com", "John Doe", "password123", UserRole.USER),
                    createUser("jane.smith@email.com", "Jane Smith", "password123", UserRole.USER),
                    createUser("alice.johnson@email.com", "Alice Johnson", "password123", UserRole.USER),
                    createUser("bob.wilson@email.com", "Bob Wilson", "password123", UserRole.USER),
                    createUser("carol.brown@email.com", "Carol Brown", "password123", UserRole.USER),
                    createUser("david.jones@email.com", "David Jones", "password123", UserRole.USER),
                    createUser("emma.davis@email.com", "Emma Davis", "password123", UserRole.USER),
                    createUser("frank.miller@email.com", "Frank Miller", "password123", UserRole.USER),
                    createUser("grace.wilson@email.com", "Grace Wilson", "password123", UserRole.USER),
                    createUser("henry.taylor@email.com", "Henry Taylor", "password123", UserRole.USER),
                    createUser("ivy.anderson@email.com", "Ivy Anderson", "password123", UserRole.USER),
                    createUser("jack.thomas@email.com", "Jack Thomas", "password123", UserRole.USER),
                    createUser("kate.jackson@email.com", "Kate Jackson", "password123", UserRole.USER),
                    createUser("leo.white@email.com", "Leo White", "password123", UserRole.USER),
                    createUser("mia.harris@email.com", "Mia Harris", "password123", UserRole.USER),
                    createUser("noah.martin@email.com", "Noah Martin", "password123", UserRole.USER),
                    createUser("olivia.garcia@email.com", "Olivia Garcia", "password123", UserRole.USER),
                    createUser("peter.rodriguez@email.com", "Peter Rodriguez", "password123", UserRole.USER),
                    createUser("quinn.lopez@email.com", "Quinn Lopez", "password123", UserRole.USER),
                    createUser("ruby.gonzalez@email.com", "Ruby Gonzalez", "password123", UserRole.USER),
                    createUser("sam.williams@email.com", "Sam Williams", "password123", UserRole.USER)
            );
            userRepository.saveAll(users);
        }
    }

    private void seedPricing() {
        if (ticketPricingRepository.count() == 0) {
            List<TicketPricing> pricing = List.of(
                    createPricing(TicketType.RIDE, new BigDecimal("2.50"), "Single ride ticket - Valid for 24 hours"),
                    createPricing(TicketType.MONTHLY, new BigDecimal("75.00"), "30-day unlimited pass - Best value for regular commuters"),
                    createPricing(TicketType.YEARLY, new BigDecimal("800.00"), "365-day unlimited pass - Maximum savings for daily users")
            );
            ticketPricingRepository.saveAll(pricing);
        }
    }

    private void seedRoutes() {
        if (routeRepository.count() == 0) {
            List<Route> routes = List.of(
                    // Bus Routes
                    createRoute("Route 101", "City Center to Airport Express", TransportType.BUS,
                            Arrays.asList("City Center", "Mall Plaza", "University Campus", "Business District", "Tech Park", "Airport Terminal"),
                            LocalTime.of(5, 0), LocalTime.of(23, 30)),

                    createRoute("Route 202", "East-West Connector", TransportType.BUS,
                            Arrays.asList("East Side", "Riverside Park", "Central Plaza", "Shopping District", "West End", "Industrial Zone"),
                            LocalTime.of(5, 30), LocalTime.of(22, 30)),

                    createRoute("Route 303", "North Residential Loop", TransportType.BUS,
                            Arrays.asList("North Station", "Maple Heights", "Pine Valley", "Oak Gardens", "Cedar Park", "North Station"),
                            LocalTime.of(6, 0), LocalTime.of(22, 0)),

                    createRoute("Route 404", "South Coast Line", TransportType.BUS,
                            Arrays.asList("City Center", "Harbor View", "Beach Front", "Marina District", "Coastal Park", "Lighthouse Point"),
                            LocalTime.of(6, 30), LocalTime.of(21, 30)),

                    // Metro Lines
                    createRoute("Metro Line A", "North-South Main Line", TransportType.METRO,
                            Arrays.asList("North Terminal", "University Station", "Central Plaza", "Government District", "Financial Center", "South Terminal"),
                            LocalTime.of(4, 30), LocalTime.of(0, 30)),

                    createRoute("Metro Line B", "East-West Express", TransportType.METRO,
                            Arrays.asList("East Gateway", "Sports Complex", "Downtown Core", "Cultural District", "Medical Center", "West Gateway"),
                            LocalTime.of(5, 0), LocalTime.of(1, 0)),

                    createRoute("Metro Line C", "Airport Connection", TransportType.METRO,
                            Arrays.asList("Central Station", "Convention Center", "Hotel District", "Cargo Terminal", "Airport Central", "International Terminal"),
                            LocalTime.of(5, 30), LocalTime.of(23, 45)),

                    // Tram Lines
                    createRoute("Tram Line 1", "Historic District Loop", TransportType.TRAM,
                            Arrays.asList("Historic Center", "Museum Quarter", "Art District", "Old Town Square", "Cathedral", "Heritage Park"),
                            LocalTime.of(7, 0), LocalTime.of(22, 0)),

                    createRoute("Tram Line 2", "Waterfront Scenic Route", TransportType.TRAM,
                            Arrays.asList("Pier 1", "Aquarium", "Boardwalk", "Yacht Club", "Seaside Resort", "Fishing Village"),
                            LocalTime.of(8, 0), LocalTime.of(20, 0)),

                    createRoute("Tram Line 3", "University Campus Circle", TransportType.TRAM,
                            Arrays.asList("Main Gate", "Library", "Student Center", "Science Building", "Athletics Complex", "Dormitories"),
                            LocalTime.of(6, 30), LocalTime.of(23, 0)),

                    // Additional Bus Routes
                    createRoute("Route 505", "Shopping District Shuttle", TransportType.BUS,
                            Arrays.asList("Metro Mall", "Fashion Center", "Electronics Plaza", "Food Court", "Department Store", "Outlet Center"),
                            LocalTime.of(9, 0), LocalTime.of(21, 0)),

                    createRoute("Route 606", "Medical District Express", TransportType.BUS,
                            Arrays.asList("General Hospital", "Medical School", "Research Center", "Rehabilitation Clinic", "Pharmacy Hub", "Emergency Center"),
                            LocalTime.of(5, 0), LocalTime.of(23, 0)),

                    createRoute("Route 707", "Night Owl Service", TransportType.BUS,
                            Arrays.asList("Downtown", "Entertainment District", "Restaurant Row", "Hotel Zone", "Nightlife Area", "Taxi Stand"),
                            LocalTime.of(22, 0), LocalTime.of(6, 0))
            );
            routeRepository.saveAll(routes);
        }
    }

    private void seedSampleTickets() {
        if (ticketRepository.count() == 0) {
            // Get some users for creating tickets
            User demoUser = userRepository.findByEmail("demo@user.com").orElse(null);
            User johnUser = userRepository.findByEmail("john.doe@email.com").orElse(null);
            User janeUser = userRepository.findByEmail("jane.smith@email.com").orElse(null);
            User aliceUser = userRepository.findByEmail("alice.johnson@email.com").orElse(null);
            User bobUser = userRepository.findByEmail("bob.wilson@email.com").orElse(null);
            User carolUser = userRepository.findByEmail("carol.brown@email.com").orElse(null);
            User davidUser = userRepository.findByEmail("david.jones@email.com").orElse(null);
            User emmaUser = userRepository.findByEmail("emma.davis@email.com").orElse(null);

            if (demoUser != null && johnUser != null && janeUser != null) {
                List<Ticket> tickets = List.of(
                        // Active tickets
                        createTicket(demoUser, TicketType.MONTHLY, new BigDecimal("75.00"), TicketStatus.ACTIVE, LocalDateTime.now().minusDays(5)),
                        createTicket(johnUser, TicketType.YEARLY, new BigDecimal("800.00"), TicketStatus.ACTIVE, LocalDateTime.now().minusDays(30)),
                        createTicket(janeUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.ACTIVE, LocalDateTime.now().minusHours(2)),
                        createTicket(aliceUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.ACTIVE, LocalDateTime.now().minusHours(1)),
                        createTicket(bobUser, TicketType.MONTHLY, new BigDecimal("75.00"), TicketStatus.ACTIVE, LocalDateTime.now().minusDays(10)),

                        // Used tickets
                        createTicket(demoUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.USED, LocalDateTime.now().minusDays(3)),
                        createTicket(johnUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.USED, LocalDateTime.now().minusDays(7)),
                        createTicket(janeUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.USED, LocalDateTime.now().minusDays(1)),
                        createTicket(carolUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.USED, LocalDateTime.now().minusDays(2)),

                        // Expired tickets
                        createTicket(davidUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.EXPIRED, LocalDateTime.now().minusDays(5)),
                        createTicket(emmaUser, TicketType.MONTHLY, new BigDecimal("75.00"), TicketStatus.EXPIRED, LocalDateTime.now().minusDays(45)),

                        // More recent active tickets
                        createTicket(carolUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.ACTIVE, LocalDateTime.now().minusMinutes(30)),
                        createTicket(davidUser, TicketType.MONTHLY, new BigDecimal("75.00"), TicketStatus.ACTIVE, LocalDateTime.now().minusDays(2)),
                        createTicket(emmaUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.ACTIVE, LocalDateTime.now().minusHours(5)),

                        // Additional variety
                        createTicket(aliceUser, TicketType.MONTHLY, new BigDecimal("75.00"), TicketStatus.ACTIVE, LocalDateTime.now().minusDays(15)),
                        createTicket(bobUser, TicketType.RIDE, new BigDecimal("2.50"), TicketStatus.USED, LocalDateTime.now().minusDays(4))
                );
                ticketRepository.saveAll(tickets);
            }
        }
    }

    // Helper methods
    private User createUser(String email, String fullName, String password, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private TicketPricing createPricing(TicketType ticketType, BigDecimal price, String description) {
        TicketPricing pricing = new TicketPricing();
        pricing.setTicketType(ticketType);
        pricing.setPrice(price);
        pricing.setDescription(description);
        pricing.setActive(true);
        return pricing;
    }

    private Route createRoute(String name, String description, TransportType type,
                              List<String> stops, LocalTime startTime, LocalTime endTime) {
        Route route = new Route();
        route.setRouteName(name);
        route.setDescription(description);
        route.setTransportType(type);
        route.setStops(stops);
        route.setOperatingStartTime(startTime);
        route.setOperatingEndTime(endTime);
        route.setActive(true);
        return route;
    }

    private Ticket createTicket(User user, TicketType ticketType, BigDecimal price,
                                TicketStatus status, LocalDateTime purchaseDate) {
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setTicketType(ticketType);
        ticket.setPrice(price);
        ticket.setStatus(status);
        ticket.setTicketNumber(generateTicketNumber(ticketType));
        ticket.setPurchaseDate(purchaseDate);
        ticket.setValidFrom(purchaseDate);

        // Set validity and used date based on ticket type and status
        switch (ticketType) {
            case RIDE -> {
                ticket.setValidUntil(purchaseDate.plusHours(24));
                if (status == TicketStatus.USED) {
                    ticket.setUsedDate(purchaseDate.plusMinutes((long)(Math.random() * 1440))); // Used within 24 hours
                }
            }
            case MONTHLY -> {
                ticket.setValidUntil(purchaseDate.plusDays(30));
                // Monthly tickets don't get "used", they just expire
            }
            case YEARLY -> {
                ticket.setValidUntil(purchaseDate.plusDays(365));
                // Yearly tickets don't get "used", they just expire
            }
        }

        // Adjust status based on current time for realistic data
        if (ticket.getValidUntil().isBefore(LocalDateTime.now()) && status == TicketStatus.ACTIVE) {
            ticket.setStatus(TicketStatus.EXPIRED);
        }

        return ticket;
    }

    private String generateTicketNumber(TicketType ticketType) {
        String prefix = switch (ticketType) {
            case RIDE -> "RD";
            case MONTHLY -> "MO";
            case YEARLY -> "YR";
        };
        return prefix + "-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}
