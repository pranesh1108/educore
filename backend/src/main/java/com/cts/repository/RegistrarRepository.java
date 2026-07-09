package com.cts.repository;

import com.cts.entity.Registrar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegistrarRepository extends JpaRepository<Registrar, Long> {

    boolean existsByUserUserId(Long userId);

    Optional<Registrar> findByUserUserId(Long userId);

    Optional<Registrar> findByUserEmail(String email);
}
