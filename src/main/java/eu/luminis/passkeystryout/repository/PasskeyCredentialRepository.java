package eu.luminis.passkeystryout.repository;

import eu.luminis.passkeystryout.entity.PasskeyCredential;
import eu.luminis.passkeystryout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    List<PasskeyCredential> findByUser(User user);
    void deleteByCredentialId(String credentialId);
}
