package perk.manager;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipService {

    private final MembershipTypeRepository membershipTypeRepository;

    public MembershipService(MembershipTypeRepository membershipTypeRepository) {
        this.membershipTypeRepository = membershipTypeRepository;
    }

    public List<MembershipType> getAllMemberships() {
        return membershipTypeRepository.findAll();
    }

    public Optional<MembershipType> findById(long id) {
        return membershipTypeRepository.findById(id);
    }

    public MembershipType save(String name) {
        return membershipTypeRepository.save(new MembershipType(name));
    }
}
