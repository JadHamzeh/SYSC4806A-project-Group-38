package perk.manager;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositories to hold data for user, membershipType, userMembership, and Perk.
 */

interface UserRepository extends JpaRepository<User, Long> {}

interface MembershipTypeRepository extends JpaRepository<MembershipType, Long> {}

interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {}

interface PerkRepository extends JpaRepository<Perk, Long> {
    List<Perk> findByMembershipType_NameIgnoreCase(String name);
    List<Perk> findAllByOrderByVotesDesc();
}