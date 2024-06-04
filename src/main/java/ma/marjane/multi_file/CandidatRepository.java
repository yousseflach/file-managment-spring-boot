package ma.marjane.multi_file;

import ma.marjane.multi_file.entity.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidatRepository extends JpaRepository<Candidat, Long> {

}
