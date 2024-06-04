package ma.marjane.multi_file.entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CandidatRequest {
    private MultipartFile file;
    private String nom;
    private String prenom;
    private String niveauDesEtudes;
}
