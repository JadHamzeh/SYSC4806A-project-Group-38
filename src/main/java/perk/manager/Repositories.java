package perk.manager;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUsername(String username);
    User findByid(Long id);
}

interface MembershipTypeRepository extends JpaRepository<MembershipType, Long> {
    List<MembershipType> findByUsername(String username);
    MembershipType findbyid(Long id);
}

interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    List<UserMembership> findByUsername(String username);
    UserMembership findByid(Long id);
}

interface PerkRepository extends JpaRepository<Perk, Long> {
    List<Perk> findByUsername(String username);
    Perk findbyid(Long id);
}