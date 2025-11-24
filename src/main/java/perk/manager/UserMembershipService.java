package perk.manager;

import jakarta.transaction.Transactional;
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
    private final UserRepository userRepository;

    /**
     * Assigns the repository to be used by the system.
     *
     * @param userMembershipRepository the repo used for accessing membership type data.
     */
    public UserMembershipService(UserMembershipRepository userMembershipRepository, UserRepository userRepository) {
        this.userMembershipRepository = userMembershipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Assigns a membership to the specified user and persists the relationship.
     *
     * @param user the user to whom the membership will be assigned.
     * @param membership the membership type to assign to the user.
     * @return the persisted UserMembership entity representing the assignment.
     */
    @Transactional
    public UserMembership assignMembership(User user, MembershipType membership){
        UserMembership userMembership = new UserMembership(user,membership);
        return userMembershipRepository.save(userMembership);
    }

    /**
     * Removes the specified user membership from the system.
     *
     * @param userMembership the UserMembership entity to delete.
     */
    @Transactional
    public void removeMembership(UserMembership userMembership){
        userMembershipRepository.delete(userMembership);
    }

    @Transactional
    public void removeMembershipByUserAndType(Long userId, Long membershipTypeId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null){
           List <UserMembership> toRemove = userMembershipRepository.findAll().stream()
                   .filter(um -> um.getUser().getId().equals(userId) &&
                           um.getMembershipType().getId().equals(membershipTypeId))
                   .collect(Collectors.toList());

           userMembershipRepository.deleteAll(toRemove);
        }
    }

    /**
     * Retrieves all membership types assigned to the specified user.
     *
     * @param user the user whose memberships should be retrieved.
     * @return a list of MembershipType entities associated with the user.
     */
    public List<MembershipType> getMembershipsForUser(User user){
        if (user == null){
            return List.of();
        }
        return user.getMemberships()
                .stream()
                .map(UserMembership::getMembershipType)
                .collect(Collectors.toList());
    }
}
