package ma.marjane.multi_file.upload;

import lombok.RequiredArgsConstructor;
import ma.marjane.multi_file.entity.Candidat;
import ma.marjane.multi_file.entity.CandidatRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;


import java.util.List;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Simpson Alfred
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileUploadController {
    private final IFileUploadService fileUploadService;

//    @RequestParam("file") MultipartFile file,
//    @RequestParam("nom") String nom,
//    @RequestParam("prenom") String prenom,
//    @RequestParam("niveauDesEtudes") String niveauDesEtudes

    @PostMapping("/upload-candidat")
    public ResponseEntity<FileResponseMessage> uploadCandidat(@ModelAttribute CandidatRequest candidatRequest) {
        String message = null;
        try {
            // Save the file and get its path
            String filePath = fileUploadService.save(candidatRequest.getFile());

            // Create a new Candidat object with the provided attributes and file path
            Candidat candidat = new Candidat();
            candidat.setNom(candidatRequest.getNom());
            candidat.setPrenom(candidatRequest.getPrenom());
            candidat.setNiveauDesEtudes(candidatRequest.getNiveauDesEtudes());
            candidat.setCvPath(filePath);

            // Save the Candidat object in the database
            fileUploadService.saveCandidat(candidat);

            message = "File uploaded successfully for candidat with ID: " + candidat.getId();
            return ResponseEntity.status(HttpStatus.OK).body(new FileResponseMessage(message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new FileResponseMessage(e.getMessage()));
        }
    }


    @PostMapping("/upload-candidats")
    public ResponseEntity<FileResponseMessage> uploadCandidats(@RequestParam("file") MultipartFile[] files,
                                                               @RequestBody List<Candidat> candidats) {
        String message = null;
        try {
            if (files.length != candidats.size()) {
                throw new IllegalArgumentException("Number of files does not match number of candidates");
            }

            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                Candidat candidat = candidats.get(i);

                // Save the file and get its path
                String filePath = fileUploadService.save(file);

                // Update the candidat's cvPath attribute with the file path
                candidat.setCvPath(filePath);

                // Save the candidat to the database
                // Assuming you have a service method to save candidat
                // fileUploadService.saveCandidat(candidat);
            }

            message = "Files uploaded successfully";
            return ResponseEntity.status(OK).body(new FileResponseMessage(message));
        } catch (Exception e) {
            return ResponseEntity.status(EXPECTATION_FAILED).body(new FileResponseMessage(e.getMessage()));
        }
    }



    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> getFileByName(@PathVariable String fileName) {
        Resource resource = fileUploadService.getFileByName(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filaName=\"" + resource.getFilename() + "\"").body(resource);
    }

    @GetMapping("/all-files")
    public ResponseEntity<List<FileResponse>> loadAllFiles() {
        List<FileResponse> files = fileUploadService.loadAllFiles()
                .map(path -> {
                    String fileName = path.getFileName().toString();
                    String url = MvcUriComponentsBuilder
                            .fromMethodName(FileUploadController.class,
                                    "getFileByName",
                                    path.getFileName().toString()).build().toString();
                    return new FileResponse(fileName, url);
                }).toList();
        return ResponseEntity.status(OK).body(files);
    }

}
