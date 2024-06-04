package ma.marjane.multi_file.upload;

import ma.marjane.multi_file.CandidatRepository;
import ma.marjane.multi_file.entity.Candidat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileUploadService implements IFileUploadService{

    @Autowired
    private CandidatRepository candidatRepository;

    private final Path rootDir = Paths.get("uploads");
    @Override
    public void init() {
        try {
            File tempDir = new File(rootDir.toUri());
            boolean dirExists = tempDir.exists();
            if(!dirExists){
                Files.createDirectory(rootDir);
            }
        }catch (IOException e) {
            throw new RuntimeException("Error creating working directory");
        }
    }

    @Override
    public String save(MultipartFile file) {
        try {
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension = "";

            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            String uniqueFilename = "cv_" + System.currentTimeMillis() + extension;

            // Save the file to the uploads folder
            Path filePath = this.rootDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            return filePath.toString(); // Return the path of the saved file
        } catch (IOException e) {
            throw new RuntimeException("Error uploading files", e);
        }
    }

    public Candidat saveCandidat(Candidat candidat) {
        return candidatRepository.save(candidat);
    }

    @Override
    public Resource getFileByName(String fileName) {
        try {
            Path filePath = rootDir.resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file");
            }
        } catch (MalformedURLException mal) {
            throw new RuntimeException("Error: " + mal.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootDir.toFile());
    }

    @Override
    public Stream<Path> loadAllFiles() {
        try {
            return Files.walk(this.rootDir, 1)
                    .filter(path -> !path.equals(this.rootDir))
                    .map(this.rootDir::relativize);
        } catch (IOException io) {
            throw new RuntimeException("Could not load files");

        }
    }
}
