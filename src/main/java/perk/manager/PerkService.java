package perk.manager;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that manages operations related to perks.
 *
 * This service provides methods for retrieving perks, filtering perks
 * by membership type or keyword, creating new perks, updating votes, and deleting perks.
 */
@Service
public class PerkService {
    private final PerkRepository perkRepository;
    private final MembershipTypeRepository membershipTypeRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new instance of the perk service.
     *
     * @param perkRepository repository used for perk storage and retrieval
     * @param membershipTypeRepository repository used to access membership types
     * @param userRepository repository used to access user data
     */
    public PerkService(PerkRepository perkRepository,
                       MembershipTypeRepository membershipTypeRepository,
                       UserRepository userRepository) {
        this.perkRepository = perkRepository;
        this.membershipTypeRepository = membershipTypeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all perks available in the system.
     *
     * @return a list of all perks
     */
    public List<Perk> getAllPerks() {
        return perkRepository.findAll();
    }

    /**
     * Searches for perks matching a specific membership type.
     *
     * @param membershipName the name of the membership type to filter by
     * @return a list of perks associated with the specified membership
     */
    public List<Perk> searchByMembership(String membershipName){
        return perkRepository.findByMembershipType_NameIgnoreCase(membershipName);
    }

    /**
     * Searches for perks using optional membership type and keyword filters.
     *
     * If both membershipType and keyword are provided, the results are filtered
     * by membership and then refined based on keyword matching in the title or description.
     *
     * @param membershipType the name of the membership type, may be empty
     * @param keyword a search term for title or description, may be empty
     * @return a list of perks matching the provided filters
     */
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

    /**
     * Creates a new perk using the provided details.
     *
     * The method resolves the membership type and user, creates a new perk object,
     * and saves it to the database.
     *
     * @param title the title of the perk
     * @param description a description of the perk
     * @param region the geographic region where the perk applies
     * @param membershipName the membership type required for the perk
     * @param userId the ID of the user who created the perk
     * @param expiryDate the expiration date of the perk
     * @return the created perk object
     */
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

    /**
     * Adds an upvote or downvote to a perk.
     *
     * If the perk exists, the vote count is adjusted by one
     * depending on whether upvote is true or false.
     *
     * @param perkId the ID of the perk to vote on
     * @param upvote true to upvote, false to downvote
     */
    public void vote(Long perkId, boolean upvote) {
        Optional<Perk> optionalPerk = perkRepository.findById(perkId);
        if (optionalPerk.isPresent()) {
            Perk perk = optionalPerk.get();
            perk.setVotes(perk.getVotes() + (upvote ? 1 : -1));
            perkRepository.save(perk);
        }
    }

    /**
     * Deletes a perk by its ID.
     *
     * @param perkId the ID of the perk to delete
     */
    public void deletePerk(Long perkId) {
        perkRepository.deleteById(perkId);
    }
}
