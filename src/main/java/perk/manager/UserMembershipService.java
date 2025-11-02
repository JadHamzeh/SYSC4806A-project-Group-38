package perk.manager;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserMembershipService {

    private final UserMembershipRepository userMembershipRepository;

    public UserMembershipService(UserMembershipRepository userMembershipRepository) {
        this.userMembershipRepository = userMembershipRepository;
    }

    public UserMembership assignMembership(User user, MembershipType membership){
        UserMembership userMembership = new UserMembership(user,membership);
        return userMembershipRepository.save(userMembership);
    }

    public void removeMembership(UserMembership userMembership){
        userMembershipRepository.delete(userMembership);
    }

    public List<MembershipType> getMembershipsForUser(User user){
        return user.getMemberships()
                .stream()
                .map(UserMembership::getMembershipType)
                .collect(Collectors.toList());
    }
}
