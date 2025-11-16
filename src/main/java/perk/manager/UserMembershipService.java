package perk.manager;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the operations for managing user memberships.
 *
 * This service handles the assignment, removal, and retrieval of MembershipType
 * entities associated with a specific User. It acts as an intermediary between
 * controllers and the UserMembershipRepository.
 */
@Service
public class UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;

    /**
     * Assigns the repository to be used by the system.
     *
     * @param userMembershipRepository the repo used for accessing membership type data.
     */
    public UserMembershipService(UserMembershipRepository userMembershipRepository) {
        this.userMembershipRepository = userMembershipRepository;
    }

    /**
     * Assigns a membership to the specified user and persists the relationship.
     *
     * @param user the user to whom the membership will be assigned.
     * @param membership the membership type to assign to the user.
     * @return the persisted UserMembership entity representing the assignment.
     */
    public UserMembership assignMembership(User user, MembershipType membership){
        UserMembership userMembership = new UserMembership(user,membership);
        return userMembershipRepository.save(userMembership);
    }

    /**
     * Removes the specified user membership from the system.
     *
     * @param userMembership the UserMembership entity to delete.
     */
    public void removeMembership(UserMembership userMembership){
        userMembershipRepository.delete(userMembership);
    }

    /**
     * Retrieves all membership types assigned to the specified user.
     *
     * @param user the user whose memberships should be retrieved.
     * @return a list of MembershipType entities associated with the user.
     */
    public List<MembershipType> getMembershipsForUser(User user){
        return user.getMemberships()
                .stream()
                .map(UserMembership::getMembershipType)
                .collect(Collectors.toList());
    }
}
