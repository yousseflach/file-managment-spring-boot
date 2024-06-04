package ma.marjane.multi_file.upload;

import ma.marjane.multi_file.entity.Candidat;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface IFileUploadService {
    public void init();

    public String save(MultipartFile file);
    Resource getFileByName(String fileName);
    public void deleteAll();
    Stream<Path>loadAllFiles();

    Candidat saveCandidat(Candidat candidat);


}
