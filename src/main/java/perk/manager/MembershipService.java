package perk.manager;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the operations related to membershipType entities.
 *
 * This service acts as an intermediary between controllers and the MembershipTypeRepository,
 * handling the retrieval and persistence of membership type data.
 */
@Service
public class MembershipService {

    private final MembershipTypeRepository membershipTypeRepository;

    /**
     * Assigns the repository to be used by the system.
     *
     * @param membershipTypeRepository the repo for adding membership type data to.
     */
    public MembershipService(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    /**
     * @return all the memberships from the membershipTypeRepository.
     */
    public List<MembershipType> getAllMemberships() {
        return membershipTypeRepository.findAll();
    }

    /**
     * Retrieve the membership type associated to a specific ID.
     *
     * @param id the unique ID of the MembershipType.
     * @return the membership type.
     */
    public Optional<MembershipType> findById(long id) {
        return membershipTypeRepository.findById(id);
    }

    /**
     * Saves a new membership type to the membershipTypeRepository.
     * @param name the name of the new membership.
     * @return the updated membershipTypeRepository.
     */
    public MembershipType save(String name) {
        return membershipTypeRepository.save(new MembershipType(name));
    }
}
