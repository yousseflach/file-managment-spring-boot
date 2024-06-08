package ma.marjane.multi_file.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@AllArgsConstructor
public class CandidatResponse {

    private String nom;
    private String prenom;
    private String niveauDesEtudes;
    private String cvPath;
}
