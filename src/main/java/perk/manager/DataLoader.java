package perk.manager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final MembershipTypeRepository membershipTypeRepository;
    private final UserService userService;
    private final PerkService perkService;

    public DataLoader(MembershipTypeRepository membershipTypeRepository,
                      UserService userService,
                      PerkService perkService) {
        this.membershipTypeRepository = membershipTypeRepository;
        this.userService = userService;
        this.perkService = perkService;
    }

    @Override
    public void run(String... args) {
        if (membershipTypeRepository.count() == 0) {
            membershipTypeRepository.save(new MembershipType("Air Miles"));
            membershipTypeRepository.save(new MembershipType("PC Optimum"));
            membershipTypeRepository.save(new MembershipType("CAA"));
            membershipTypeRepository.save(new MembershipType("Visa"));
            membershipTypeRepository.save(new MembershipType("Mastercard"));
            membershipTypeRepository.save(new MembershipType("American Express"));
            membershipTypeRepository.save(new MembershipType("Scene+"));
            membershipTypeRepository.save(new MembershipType("Aeroplan"));
            membershipTypeRepository.save(new MembershipType("Costco"));
            membershipTypeRepository.save(new MembershipType("Amazon Prime"));

            System.out.println("Pre-loaded membership types");
        }

        if (userService.findByUsername("demo").isEmpty()) {
            User demoUser = userService.registerUser("demo", "demo123");

            perkService.createPerk(
                    "10% off Movie Tickets",
                    "Get 10% discount on movie tickets at Cineplex theatres",
                    "Canada",
                    "Scene+",
                    demoUser.getId(),
                    LocalDate.now().plusMonths(3)
            );

            perkService.createPerk(
                    "Free Domestic Flight",
                    "Redeem 25,000 points for a free domestic flight within Canada",
                    "Canada",
                    "Aeroplan",
                    demoUser.getId(),
                    LocalDate.now().plusYears(1)
            );

            perkService.createPerk(
                    "20,000 Bonus Points",
                    "Earn 20,000 bonus points on first purchase",
                    "North America",
                    "PC Optimum",
                    demoUser.getId(),
                    LocalDate.now().plusMonths(2)
            );

            perkService.createPerk(
                    "Gas Discount",
                    "Save 5 cents per litre on gas at participating stations",
                    "Canada",
                    "CAA",
                    demoUser.getId(),
                    LocalDate.now().plusMonths(6)
            );

            // For debugging
            System.out.println("Created demo user (username: demo, password: demo123) with sample perks");
        }
    }
}