package com.subproblem.fitnesstrackingapp.config;

import com.subproblem.fitnesstrackingapp.entity.*;
import com.subproblem.fitnesstrackingapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds 5 dummy users and demo data when profile "demo" is active.
 * Run with: -Dspring.profiles.active=demo (or add demo to application.yaml for local testing).
 * Skips seeding if any demo user already exists.
 */
@Profile("demo")
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PerformanceMetricsRepository performanceMetricsRepository;
    private final WorkoutRepository workoutRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail("alice@demo.com")) {
            return; // already seeded
        }

        // Create products first (shared by users)
        List<Product> products = createProducts();
        productRepository.saveAll(products);

        // 1. Alice Smith – USER, full profile + weight + workouts + products
        User alice = createUser("Alice", "Smith", "alice@demo.com", Role.USER,
                170.0, 65.0, LocalDate.of(1992, 5, 10));
        userRepository.save(alice);
        addWeightEntries(alice.getId(), List.of("2025-02-01", "2025-02-15", "2025-03-01"), List.of(68.0, 67.2, 66.5));
        addWorkout(alice, LocalDate.of(2025, 3, 1), "Running", 35.0, 280, 7);
        alice.setProducts(new ArrayList<>(List.of(products.get(0), products.get(1))));
        userRepository.save(alice);

        // 2. Bob Jones – USER, weight journey + workouts
        User bob = createUser("Bob", "Jones", "bob@demo.com", Role.USER,
                182.0, 80.0, LocalDate.of(1988, 11, 22));
        userRepository.save(bob);
        addWeightEntries(bob.getId(), List.of("2025-01-10", "2025-02-10", "2025-03-05"), List.of(85.0, 83.5, 82.0));
        addWorkout(bob, LocalDate.of(2025, 3, 2), "Cycling", 60.0, 520, 8);
        bob.setProducts(new ArrayList<>());
        userRepository.save(bob);

        // 3. Carol White – USER, minimal profile + one weight + one product
        User carol = createUser("Carol", "White", "carol@demo.com", Role.USER,
                165.0, 58.0, LocalDate.of(1995, 3, 8));
        userRepository.save(carol);
        addWeightEntries(carol.getId(), List.of("2025-03-01"), List.of(59.0));
        addWorkout(carol, LocalDate.of(2025, 3, 3), "Yoga", 45.0, 120, 4);
        carol.setProducts(new ArrayList<>(List.of(products.get(2))));
        userRepository.save(carol);

        // 4. Dave Brown – USER, profile only, no weight/workouts/products
        User dave = createUser("Dave", "Brown", "dave@demo.com", Role.USER,
                178.0, 75.0, LocalDate.of(1990, 7, 14));
        userRepository.save(dave);
        dave.setWorkouts(new ArrayList<>());
        dave.setProducts(new ArrayList<>());
        userRepository.save(dave);

        // 5. Eve Admin – ADMIN, for admin API testing
        User admin = createUser("Eve", "Admin", "admin@demo.com", Role.ADMIN,
                168.0, 62.0, LocalDate.of(1985, 1, 1));
        userRepository.save(admin);
        addWeightEntries(admin.getId(), List.of("2025-03-01"), List.of(63.0));
        addWorkout(admin, LocalDate.of(2025, 3, 5), "Strength", 50.0, 300, 6);
        admin.setProducts(new ArrayList<>());
        userRepository.save(admin);
    }

    private User createUser(String firstname, String lastname, String email, Role role,
                            Double heightCm, Double targetWeightKg, LocalDate dateOfBirth) {
        return User.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .dateOfBirth(dateOfBirth)
                .role(role)
                .heightCm(heightCm)
                .targetWeightKg(targetWeightKg)
                .workouts(new ArrayList<>())
                .build();
    }

    private List<Product> createProducts() {
        return List.of(
                Product.builder().name("Choco Protein Bar").description("High protein snack").calories(200).protein(15.0).carbs(22.0).fat(8.0).code(1001).image(new byte[0]).build(),
                Product.builder().name("Oatmeal Pack").description("Quick breakfast").calories(150).protein(5.0).carbs(27.0).fat(3.0).code(1002).image(new byte[0]).build(),
                Product.builder().name("Banana").description("Pre-workout fruit").calories(105).protein(1.3).carbs(27.0).fat(0.4).code(1003).image(new byte[0]).build()
        );
    }

    private void addWeightEntries(String userId, List<String> dates, List<Double> weightsKg) {
        for (int i = 0; i < dates.size(); i++) {
            WeightEntry e = WeightEntry.builder()
                    .userId(userId)
                    .date(LocalDate.parse(dates.get(i)))
                    .weightKg(weightsKg.get(i))
                    .note("Demo entry")
                    .build();
            weightEntryRepository.save(e);
        }
    }

    private void addWorkout(User user, LocalDate date, String exerciseType, double duration, int caloriesBurned, int intensity) {
        PerformanceMetrics pm = PerformanceMetrics.builder()
                .duration(duration)
                .caloriesBurned(caloriesBurned)
                .intensity(intensity)
                .build();
        performanceMetricsRepository.save(pm);
        Workout w = Workout.builder()
                .workoutDate(date)
                .exerciseType(exerciseType)
                .performanceMetrics(pm)
                .build();
        workoutRepository.save(w);
        user.getWorkouts().add(w);
        userRepository.save(user);
    }
}
