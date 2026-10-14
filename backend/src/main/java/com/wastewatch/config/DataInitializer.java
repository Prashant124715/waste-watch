package com.wastewatch.config;

import com.wastewatch.entity.*;
import com.wastewatch.entity.enums.*;
import com.wastewatch.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
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
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AreaRepository areaRepository,
                           BuildingRepository buildingRepository,
                           RoomRepository roomRepository,
                           InspectionRepository inspectionRepository,
                           ViolationRepository violationRepository,
                           NotificationRepository notificationRepository,
                           AuditLogRepository auditLogRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.inspectionRepository = inspectionRepository;
        this.violationRepository = violationRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data creation.");
            return;
        }

        log.info("Starting WasteWatch development database seeding...");

        // =====================================================================
        // 1. SEED USERS
        // =====================================================================
        AppUser admin = new AppUser(
                "admin",
                "admin@wastewatch.org",
                passwordEncoder.encode("admin123"),
                "Administrator",
                UserRole.ADMIN
        );
        admin.setPhoneNumber("9999000001");
        userRepository.save(admin);

        AppUser volunteer1 = new AppUser(
                "volunteer",
                "volunteer@wastewatch.org",
                passwordEncoder.encode("volunteer123"),
                "Field Inspector Volunteer",
                UserRole.VOLUNTEER
        );
        volunteer1.setPhoneNumber("9999000002");
        volunteer1.setStatus(UserStatus.ACTIVE);
        userRepository.save(volunteer1);

        AppUser volunteer2 = new AppUser(
                "volunteer_priya",
                "priya.sharma@wastewatch.org",
                passwordEncoder.encode("volunteer123"),
                "Priya Sharma",
                UserRole.VOLUNTEER
        );
        volunteer2.setPhoneNumber("9999000003");
        volunteer2.setStatus(UserStatus.ACTIVE);
        userRepository.save(volunteer2);

        AppUser volunteer3 = new AppUser(
                "volunteer_rohan",
                "rohan.mehta@wastewatch.org",
                passwordEncoder.encode("volunteer123"),
                "Rohan Mehta",
                UserRole.VOLUNTEER
        );
        volunteer3.setPhoneNumber("9999000004");
        volunteer3.setStatus(UserStatus.INACTIVE);
        userRepository.save(volunteer3);

        log.info("Seeded 4 users (1 Admin, 3 Volunteers: volunteer, volunteer_priya, volunteer_rohan)");

        // =====================================================================
        // 2. SEED AREAS
        // =====================================================================
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

        Area centralArea = new Area(
                "ZONE-CENTRAL-03",
                "Central Business District",
                "Metro City",
                "400051",
                "Commercial towers, corporate campuses, and transit hub"
        );
        areaRepository.save(centralArea);

        log.info("Seeded 3 geographic areas across Metro City");

        // =====================================================================
        // 3. SEED BUILDINGS
        // =====================================================================
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

        Building apexTower = new Building(
                centralArea,
                "BLD-CD-301",
                "Apex Metro Tower",
                "100 Financial Center Drive",
                new BigDecimal("19.01500000"),
                new BigDecimal("72.85500000"),
                BuildingType.COMMERCIAL,
                12
        );
        buildingRepository.save(apexTower);

        log.info("Seeded 4 multi-type buildings with coordinates");

        // =====================================================================
        // 4. SEED ROOMS
        // =====================================================================
        Room room101 = new Room(greenHeights, 1, "Flat 101", "QR-GVH-101", "John Doe", "9876543210");
        roomRepository.save(room101);

        Room room102 = new Room(greenHeights, 1, "Flat 102", "QR-GVH-102", "Jane Smith", "9876543211");
        room102.setPersistentNonCompliance(true); // Flagged for repeat non-compliance
        roomRepository.save(room102);

        Room room201 = new Room(greenHeights, 2, "Flat 201", "QR-GVH-201", "Robert Brown", "9876543212");
        roomRepository.save(room201);

        Room suite301 = new Room(ecoTech, 3, "Suite 301", "QR-ETP-301", "Innovate Labs", "9876543213");
        roomRepository.save(suite301);

        Room suite302 = new Room(ecoTech, 3, "Suite 302", "QR-ETP-302", "CloudNexus Systems", "9876543214");
        roomRepository.save(suite302);

        Room roomSR1 = new Room(sunriseResidency, 1, "Unit 1A", "QR-SR-1A", "Anand Verma", "9876543215");
        roomRepository.save(roomSR1);

        Room roomSR2 = new Room(sunriseResidency, 2, "Unit 2B", "QR-SR-2B", "Kavita Rao", "9876543216");
        roomRepository.save(roomSR2);

        Room roomApex1 = new Room(apexTower, 5, "Floor 5 - HQ", "QR-AMT-501", "Metro FinCorp", "9876543217");
        roomRepository.save(roomApex1);

        log.info("Seeded 8 rooms across residential and commercial buildings");

        // =====================================================================
        // 5. SEED INSPECTIONS (Realistic history spanning multiple days)
        // =====================================================================
        // Room 101: Perfect inspection
        Inspection insp1 = new Inspection();
        insp1.setRoom(room101);
        insp1.setVolunteer(volunteer1);
        insp1.setInspectionDate(LocalDateTime.now().minusDays(3));
        insp1.setCreatedAt(LocalDateTime.now().minusDays(3));
        insp1.setUpdatedAt(LocalDateTime.now().minusDays(3));
        insp1.setWetWasteSeparated(true);
        insp1.setDryWasteSeparated(true);
        insp1.setSanitaryWasteSeparated(true);
        insp1.setCorrectBinsUsed(true);
        insp1.setBinsLabelled(true);
        insp1.setNoMixedWaste(true);
        insp1.setTotalScore(100);
        insp1.setClassification(ComplianceClassification.EXCELLENT);
        insp1.setStatus(InspectionStatus.COMPLETED);
        insp1.setRemarks("Flawless waste segregation and proper color-coded bin labels observed.");
        inspectionRepository.save(insp1);

        // Room 201: Good inspection
        Inspection insp2 = new Inspection();
        insp2.setRoom(room201);
        insp2.setVolunteer(volunteer2);
        insp2.setInspectionDate(LocalDateTime.now().minusDays(2));
        insp2.setCreatedAt(LocalDateTime.now().minusDays(2));
        insp2.setUpdatedAt(LocalDateTime.now().minusDays(2));
        insp2.setWetWasteSeparated(true);
        insp2.setDryWasteSeparated(true);
        insp2.setSanitaryWasteSeparated(false);
        insp2.setCorrectBinsUsed(true);
        insp2.setBinsLabelled(true);
        insp2.setNoMixedWaste(false);
        insp2.setTotalScore(75);
        insp2.setClassification(ComplianceClassification.GOOD);
        insp2.setStatus(InspectionStatus.COMPLETED);
        insp2.setRemarks("Good compliance. Minor reject waste mingling noticed.");
        inspectionRepository.save(insp2);

        // Room 102: 3 Consecutive POOR inspections (triggers persistent non-compliance!)
        Inspection insp3a = new Inspection();
        insp3a.setRoom(room102);
        insp3a.setVolunteer(volunteer1);
        insp3a.setInspectionDate(LocalDateTime.now().minusDays(4));
        insp3a.setCreatedAt(LocalDateTime.now().minusDays(4));
        insp3a.setUpdatedAt(LocalDateTime.now().minusDays(4));
        insp3a.setWetWasteSeparated(false);
        insp3a.setDryWasteSeparated(false);
        insp3a.setSanitaryWasteSeparated(false);
        insp3a.setCorrectBinsUsed(true);
        insp3a.setBinsLabelled(false);
        insp3a.setNoMixedWaste(false);
        insp3a.setTotalScore(15);
        insp3a.setClassification(ComplianceClassification.POOR);
        insp3a.setStatus(InspectionStatus.FLAGGED);
        insp3a.setRemarks("Audit 1: Complete absence of wet/dry segregation.");
        inspectionRepository.save(insp3a);

        Inspection insp3b = new Inspection();
        insp3b.setRoom(room102);
        insp3b.setVolunteer(volunteer2);
        insp3b.setInspectionDate(LocalDateTime.now().minusDays(2));
        insp3b.setCreatedAt(LocalDateTime.now().minusDays(2));
        insp3b.setUpdatedAt(LocalDateTime.now().minusDays(2));
        insp3b.setWetWasteSeparated(false);
        insp3b.setDryWasteSeparated(false);
        insp3b.setSanitaryWasteSeparated(false);
        insp3b.setCorrectBinsUsed(true);
        insp3b.setBinsLabelled(false);
        insp3b.setNoMixedWaste(false);
        insp3b.setTotalScore(15);
        insp3b.setClassification(ComplianceClassification.POOR);
        insp3b.setStatus(InspectionStatus.FLAGGED);
        insp3b.setRemarks("Audit 2: Repeat violation. Refused segregation protocol.");
        inspectionRepository.save(insp3b);

        Inspection insp3c = new Inspection();
        insp3c.setRoom(room102);
        insp3c.setVolunteer(volunteer1);
        insp3c.setInspectionDate(LocalDateTime.now().minusHours(4));
        insp3c.setCreatedAt(LocalDateTime.now().minusHours(4));
        insp3c.setUpdatedAt(LocalDateTime.now().minusHours(4));
        insp3c.setWetWasteSeparated(false);
        insp3c.setDryWasteSeparated(false);
        insp3c.setSanitaryWasteSeparated(false);
        insp3c.setCorrectBinsUsed(true);
        insp3c.setBinsLabelled(false);
        insp3c.setNoMixedWaste(false);
        insp3c.setTotalScore(15);
        insp3c.setClassification(ComplianceClassification.POOR);
        insp3c.setStatus(InspectionStatus.FLAGGED);
        insp3c.setRemarks("Audit 3: 3rd consecutive poor audit. Escalated to administration.");
        inspectionRepository.save(insp3c);

        // Suite 301: Needs improvement
        Inspection insp4 = new Inspection();
        insp4.setRoom(suite301);
        insp4.setVolunteer(volunteer2);
        insp4.setInspectionDate(LocalDateTime.now().minusDays(1));
        insp4.setCreatedAt(LocalDateTime.now().minusDays(1));
        insp4.setUpdatedAt(LocalDateTime.now().minusDays(1));
        insp4.setWetWasteSeparated(true);
        insp4.setDryWasteSeparated(true);
        insp4.setSanitaryWasteSeparated(false);
        insp4.setCorrectBinsUsed(false);
        insp4.setBinsLabelled(false);
        insp4.setNoMixedWaste(false);
        insp4.setTotalScore(50);
        insp4.setClassification(ComplianceClassification.NEEDS_IMPROVEMENT);
        insp4.setStatus(InspectionStatus.COMPLETED);
        insp4.setRemarks("Cafeteria waste segregated, but workstation bins unlabelled.");
        inspectionRepository.save(insp4);

        // Room SR-1: Excellent inspection
        Inspection insp5 = new Inspection();
        insp5.setRoom(roomSR1);
        insp5.setVolunteer(volunteer1);
        insp5.setInspectionDate(LocalDateTime.now().minusDays(1));
        insp5.setCreatedAt(LocalDateTime.now().minusDays(1));
        insp5.setUpdatedAt(LocalDateTime.now().minusDays(1));
        insp5.setWetWasteSeparated(true);
        insp5.setDryWasteSeparated(true);
        insp5.setSanitaryWasteSeparated(true);
        insp5.setCorrectBinsUsed(true);
        insp5.setBinsLabelled(true);
        insp5.setNoMixedWaste(true);
        insp5.setTotalScore(100);
        insp5.setClassification(ComplianceClassification.EXCELLENT);
        insp5.setStatus(InspectionStatus.COMPLETED);
        insp5.setRemarks("Active composting and exemplary three-bin disposal.");
        inspectionRepository.save(insp5);

        log.info("Seeded 7 comprehensive inspection records");

        // =====================================================================
        // 6. SEED VIOLATIONS (Different statuses and severities)
        // =====================================================================
        // Violation for Room 102 (Recent: OPEN, CRITICAL)
        Violation viol1 = new Violation();
        viol1.setInspection(insp3c);
        viol1.setRoom(room102);
        viol1.setSeverity(ViolationSeverity.CRITICAL);
        viol1.setStatus(ViolationStatus.OPEN);
        viol1.setDescription("Severe unsegregated mixed municipal waste breach. 3rd consecutive failure.");
        viol1.setFineAmount(new BigDecimal("500.00"));
        violationRepository.save(viol1);

        // Historical violation for Room 102 (ACKNOWLEDGED)
        Violation viol2 = new Violation();
        viol2.setInspection(insp3b);
        viol2.setRoom(room102);
        viol2.setSeverity(ViolationSeverity.CRITICAL);
        viol2.setStatus(ViolationStatus.ACKNOWLEDGED);
        viol2.setDescription("Second failure — tenant acknowledged violation notice.");
        viol2.setFineAmount(new BigDecimal("500.00"));
        viol2.setResolutionNotes("Notice served to occupant. Re-inspection scheduled.");
        violationRepository.save(viol2);

        // Historical violation for Room 102 (RESOLVED)
        Violation viol3 = new Violation();
        viol3.setInspection(insp3a);
        viol3.setRoom(room102);
        viol3.setSeverity(ViolationSeverity.CRITICAL);
        viol3.setStatus(ViolationStatus.RESOLVED);
        viol3.setDescription("First failure — initial warning ticket.");
        viol3.setFineAmount(new BigDecimal("500.00"));
        viol3.setResolutionNotes("Occupant paid fine and attended waste management orientation.");
        viol3.setResolvedAt(LocalDateTime.now().minusDays(3));
        violationRepository.save(viol3);

        log.info("Seeded 3 violation tickets (OPEN, ACKNOWLEDGED, RESOLVED)");

        // =====================================================================
        // 7. SEED NOTIFICATIONS (In-App Governance Alerts)
        // =====================================================================
        Notification notif1 = new Notification(
                admin,
                "⚠️ Persistent Non-Compliance — Room Flat 102",
                "Room Flat 102 in Green Valley Heights (North Ward - Sector 14) has had 3 consecutive POOR inspections and is flagged as PERSISTENT_NON_COMPLIANCE.",
                NotificationType.PERSISTENT_NON_COMPLIANCE
        );
        notificationRepository.save(notif1);

        Notification notif2 = new Notification(
                admin,
                "⛔ New CRITICAL Violation — Green Valley Heights, Room Flat 102",
                "CRITICAL violation raised for Room Flat 102. Score: 15/100. Severe unsegregated mixed waste breach.",
                NotificationType.VIOLATION_SEVERITY_HIGH
        );
        notificationRepository.save(notif2);

        Notification notif3 = new Notification(
                admin,
                "Welcome to WasteWatch Enterprise",
                "Waste segregation compliance monitoring system initialized successfully for Metro City.",
                NotificationType.SYSTEM
        );
        notif3.setIsRead(true);
        notificationRepository.save(notif3);

        log.info("Seeded 3 governance notifications for administrators");

        // =====================================================================
        // 8. SEED AUDIT LOGS (Governance Trail)
        // =====================================================================
        auditLogRepository.save(new AuditLog(admin, "LOGIN", "AppUser", admin.getId().toString(), "127.0.0.1", "Admin user logged in"));
        auditLogRepository.save(new AuditLog(admin, "AREA_CREATED", "Area", northArea.getId().toString(), "127.0.0.1", "Area 'North Ward - Sector 14' created"));
        auditLogRepository.save(new AuditLog(admin, "BUILDING_CREATED", "Building", greenHeights.getId().toString(), "127.0.0.1", "Building 'Green Valley Heights' created"));
        auditLogRepository.save(new AuditLog(admin, "ROOM_CREATED", "Room", room101.getId().toString(), "127.0.0.1", "Room 'Flat 101' created"));
        auditLogRepository.save(new AuditLog(admin, "VOLUNTEER_STATUS_CHANGED", "AppUser", volunteer3.getId().toString(), "127.0.0.1", "Volunteer 'volunteer_rohan' status changed to INACTIVE"));
        auditLogRepository.save(new AuditLog(admin, "RESOLVE_VIOLATION", "Violation", viol3.getId().toString(), "127.0.0.1", "Violation #3 resolved with corrective action notes"));

        log.info("Seeded 6 governance audit trail logs");
        log.info("WasteWatch seed data initialization completed successfully.");
    }
}
