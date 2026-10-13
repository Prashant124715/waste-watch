package com.wastewatch.config;

import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.*;
import com.wastewatch.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AreaRepository areaRepository,
                           BuildingRepository buildingRepository,
                           RoomRepository roomRepository,
                           InspectionRepository inspectionRepository,
                           ViolationRepository violationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.inspectionRepository = inspectionRepository;
        this.violationRepository = violationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data creation.");
            return;
        }

        log.info("Starting WasteWatch development database seeding...");

        // 1. Seed Users (Development only passwords)
        AppUser admin = new AppUser(
                "admin",
                "admin@wastewatch.org",
                passwordEncoder.encode("admin123"),
                "Administrator",
                UserRole.ADMIN
        );
        admin.setPhoneNumber("9999000001");
        userRepository.save(admin);

        AppUser volunteer = new AppUser(
                "volunteer",
                "volunteer@wastewatch.org",
                passwordEncoder.encode("volunteer123"),
                "Field Inspector Volunteer",
                UserRole.VOLUNTEER
        );
        volunteer.setPhoneNumber("9999000002");
        userRepository.save(volunteer);

        log.info("Seeded users: admin (admin123), volunteer (volunteer123)");

        // 2. Seed Areas
        Area northArea = new Area(
                "ZONE-NORTH-01",
                "North Ward - Sector 14",
                "Metro City",
                "400001",
                "Residential & Commercial sector with active green initiatives"
        );
        areaRepository.save(northArea);

        Area southArea = new Area(
                "ZONE-SOUTH-02",
                "South Ward - Sector 22",
                "Metro City",
                "400022",
                "High-density urban residential corridor"
        );
        areaRepository.save(southArea);

        log.info("Seeded 2 areas: ZONE-NORTH-01, ZONE-SOUTH-02");

        // 3. Seed Buildings
        Building greenHeights = new Building(
                northArea,
                "BLD-NW-101",
                "Green Valley Heights",
                "12 Forest Hill Road, Sector 14",
                new BigDecimal("19.07600000"),
                new BigDecimal("72.87770000"),
                BuildingType.RESIDENTIAL,
                5
        );
        buildingRepository.save(greenHeights);

        Building ecoTech = new Building(
                northArea,
                "BLD-NW-102",
                "Eco Tech Park",
                "45 Industrial Avenue, Sector 14",
                new BigDecimal("19.08000000"),
                new BigDecimal("72.88000000"),
                BuildingType.COMMERCIAL,
                8
        );
        buildingRepository.save(ecoTech);

        Building sunriseResidency = new Building(
                southArea,
                "BLD-SW-201",
                "Sunrise Residency",
                "89 Marine View Boulevard, Sector 22",
                new BigDecimal("18.99000000"),
                new BigDecimal("72.82000000"),
                BuildingType.RESIDENTIAL,
                10
        );
        buildingRepository.save(sunriseResidency);

        log.info("Seeded 3 buildings: BLD-NW-101, BLD-NW-102, BLD-SW-201");

        // 4. Seed Rooms
        Room room101 = new Room(
                greenHeights,
                1,
                "Flat 101",
                "QR-GVH-101",
                "John Doe",
                "9876543210"
        );
        roomRepository.save(room101);

        Room room102 = new Room(
                greenHeights,
                1,
                "Flat 102",
                "QR-GVH-102",
                "Jane Smith",
                "9876543211"
        );
        roomRepository.save(room102);

        Room room201 = new Room(
                greenHeights,
                2,
                "Flat 201",
                "QR-GVH-201",
                "Robert Brown",
                "9876543212"
        );
        roomRepository.save(room201);

        Room suite301 = new Room(
                ecoTech,
                3,
                "Suite 301",
                "QR-ETP-301",
                "Innovate Labs",
                "9876543213"
        );
        roomRepository.save(suite301);

        log.info("Seeded 4 rooms with QR tokens across buildings");

        // 5. Seed Sample Inspection & Violation
        Inspection inspection1 = new Inspection();
        inspection1.setRoom(room101);
        inspection1.setVolunteer(volunteer);
        inspection1.setInspectionDate(LocalDateTime.now().minusDays(1));
        inspection1.setCreatedAt(LocalDateTime.now().minusDays(1));
        inspection1.setWetWasteSeparated(true);
        inspection1.setDryWasteSeparated(true);
        inspection1.setSanitaryWasteSeparated(true);
        inspection1.setCorrectBinsUsed(true);
        inspection1.setBinsLabelled(true);
        inspection1.setNoMixedWaste(true);
        inspection1.setTotalScore(100);
        inspection1.setClassification(ComplianceClassification.EXCELLENT);
        inspection1.setStatus(InspectionStatus.COMPLETED);
        inspection1.setRemarks("Impeccable segregation and color coded bins present.");
        inspectionRepository.save(inspection1);

        Inspection inspection2 = new Inspection();
        inspection2.setRoom(room102);
        inspection2.setVolunteer(volunteer);
        inspection2.setInspectionDate(LocalDateTime.now().minusHours(4));
        inspection2.setCreatedAt(LocalDateTime.now().minusHours(4));
        inspection2.setWetWasteSeparated(false);
        inspection2.setDryWasteSeparated(false);
        inspection2.setSanitaryWasteSeparated(false);
        inspection2.setCorrectBinsUsed(true);
        inspection2.setBinsLabelled(false);
        inspection2.setNoMixedWaste(false);
        inspection2.setTotalScore(15);
        inspection2.setClassification(ComplianceClassification.POOR);
        inspection2.setStatus(InspectionStatus.FLAGGED);
        inspection2.setRemarks("Mixed unsegregated food waste and recyclables observed.");
        inspectionRepository.save(inspection2);

        Violation violation2 = new Violation();
        violation2.setInspection(inspection2);
        violation2.setRoom(room102);
        violation2.setSeverity(ViolationSeverity.HIGH);
        violation2.setStatus(ViolationStatus.OPEN);
        violation2.setDescription("Severe unsegregated mixed municipal waste breach.");
        violation2.setFineAmount(new BigDecimal("500.00"));
        violationRepository.save(violation2);

        log.info("Seeded sample inspections and violation record.");
        log.info("WasteWatch seed data initialization completed successfully.");
    }
}
