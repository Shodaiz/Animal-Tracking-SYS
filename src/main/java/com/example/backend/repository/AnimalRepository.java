package com.example.backend.repository;

import com.example.backend.model.Animal;
import com.example.backend.model.Farm;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

    // ✅ Cherche via la relation rfidTag → rfidCode (plus findByRfidTag string)
    Optional<Animal> findByRfidTagRfidCode(String rfidCode);

    // Par ferme courante
    List<Animal> findByFarm(Farm farm);

    // Par propriétaire
    List<Animal> findByOwner(User owner);

    // Animaux actifs d'un propriétaire
    List<Animal> findByOwnerAndLifeStatus(User owner, String lifeStatus);

    // Animaux actifs dans une ferme
    List<Animal> findByFarmAndLifeStatus(Farm farm, String lifeStatus);

    // Pour l'Aïd el-Adha : animaux éligibles
    @Query("""
        SELECT a FROM Animal a
        WHERE a.owner.id = :ownerId
        AND a.lifeStatus = 'Active'
        AND a.healthStatus = 'Healthy'
        AND a.species IN ('Ovin', 'Bovin', 'Caprin')
        AND a.birthDate <= CURRENT_DATE - 365
    """)
    List<Animal> findAidEligibleAnimals(@Param("ownerId") Long ownerId);
}