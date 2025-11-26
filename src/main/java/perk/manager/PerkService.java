package perk.manager;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PerkService {
    private final PerkRepository perkRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final UserRepository userRepository;

    public PerkService(PerkRepository perkRepository,
                       MembershipTypeRepository membershipTypeRepository,
                       UserRepository userRepository) {
        this.perkRepository = perkRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.userRepository = userRepository;
    }

    public List<Perk> getAllPerks() {
        return perkRepository.findAll();
    }

    public List<Perk> searchByMembership(String membershipName){
        return perkRepository.findByMembershipType_NameIgnoreCase(membershipName);
    }

    public List<Perk> searchPerks(String membershipType, String keyword) {
        if (membershipType != null && !membershipType.isEmpty() && keyword != null && !keyword.isEmpty()) {
            List<Perk> byMembership = perkRepository.findByMembershipType_NameIgnoreCase(membershipType);
            String k = keyword.toLowerCase();
            return byMembership.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(k) || p.getDescription().toLowerCase().contains(k))
                    .collect(Collectors.toList());
        } else if (membershipType != null && !membershipType.isEmpty()) {
            return perkRepository.findByMembershipType_NameIgnoreCase(membershipType);
        } else if (keyword != null && !keyword.isEmpty()) {
            return perkRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        } else {
            return perkRepository.findAll();
        }
    }

    public Perk createPerk(String title, String description, String region, String membershipName, Long userId, LocalDate expiryDate){
        MembershipType membership = membershipTypeRepository.findAll().stream()
                .filter(m -> m.getName().equalsIgnoreCase(membershipName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Membership Type not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Perk perk = new Perk(title, description, region, expiryDate, membership, user);
        return perkRepository.save(perk);
    }

    public void vote(Long perkId, boolean upvote) {
        Optional<Perk> optionalPerk = perkRepository.findById(perkId);
        if (optionalPerk.isPresent()) {
            Perk perk = optionalPerk.get();
            perk.setVotes(perk.getVotes() + (upvote ? 1 : -1));
            perkRepository.save(perk);
        }
    }

    public void deletePerk(Long perkId) {
        perkRepository.deleteById(perkId);
    }
}
