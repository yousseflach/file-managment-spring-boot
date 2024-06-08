package ma.marjane.multi_file.upload;

import lombok.RequiredArgsConstructor;
import ma.marjane.multi_file.CandidatRepository;
import ma.marjane.multi_file.entity.Candidat;
import ma.marjane.multi_file.entity.CandidatRequest;
import ma.marjane.multi_file.entity.CandidatResponse;
import ma.marjane.multi_file.entity.ErrorMessage;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Simpson Alfred
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileUploadController {
    private final IFileUploadService fileUploadService;
    private final CandidatRepository candidatRepository;

//    @RequestParam("file") MultipartFile file,
//    @RequestParam("nom") String nom,
//    @RequestParam("prenom") String prenom,
//    @RequestParam("niveauDesEtudes") String niveauDesEtudes

    @PostMapping("/upload-candidat")
    public ResponseEntity<FileResponseMessage> uploadCandidat(@ModelAttribute CandidatRequest candidatRequest) {
        String message = null;
        try {
            // Save the file and get its filename
            String filename = fileUploadService.save(candidatRequest.getFile());

            // Create a new Candidat object with the provided attributes and filename
            Candidat candidat = new Candidat();
            candidat.setNom(candidatRequest.getNom());
            candidat.setPrenom(candidatRequest.getPrenom());
            candidat.setNiveauDesEtudes(candidatRequest.getNiveauDesEtudes());
            candidat.setCvPath(filename); // Save only the filename

            // Save the Candidat object in the database
            fileUploadService.saveCandidat(candidat);

            message = "File uploaded successfully for candidat with ID: " + candidat.getId();
            return ResponseEntity.status(HttpStatus.OK).body(new FileResponseMessage(message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new FileResponseMessage(e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getCandidatById(@PathVariable Long id) {
        try {
            // Fetch the candidate from the database
            Candidat candidat = candidatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));

            // Create a response entity with the candidate details
            return ResponseEntity.ok().body(new CandidatResponse(candidat.getNom(), candidat.getPrenom(), candidat.getNiveauDesEtudes(), candidat.getCvPath()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
        }
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        try {
            // Fetch the candidate from the database
            Candidat candidat = candidatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));

            // Load the CV file as a resource
            Resource cvResource = fileUploadService.getFileByName(Paths.get(candidat.getCvPath()).getFileName().toString());

            // Create a response entity with the CV file
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cvResource.getFilename() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(cvResource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all-candidat")
    public ResponseEntity<?> getAllCandidat() {
        try {
            // Fetch the candidates from the database
            List<Candidat> candidats = candidatRepository.findAll();

            // Map each Candidat to a CandidatResponse
            List<CandidatResponse> candidatResponses = candidats.stream()
                    .map(candidat -> new CandidatResponse(candidat.getNom(), candidat.getPrenom(), candidat.getNiveauDesEtudes(), candidat.getCvPath()))
                    .collect(Collectors.toList());

            // Create a response entity with the candidate details
            return ResponseEntity.ok().body(candidatResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(e.getMessage()));
        }
    }

    @DeleteMapping("/candidat/{id}")
    public ResponseEntity<?> deleteCandidatById(@PathVariable("id") long id) {
        try {
            // Fetch the candidate from the database
            Candidat candidat = candidatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));
            String fileName = Paths.get(candidat.getCvPath()).getFileName().toString();
            fileUploadService.deleteFileByName(fileName);
            candidatRepository.deleteById(candidat.getId());

            return ResponseEntity.status(HttpStatus.OK).body("Candidat deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }


    @DeleteMapping("/candidats")
    public ResponseEntity<?> deleteAllCandidat() {
        try {
            // Fetch the candidates from the database
            List<Candidat> candidats = candidatRepository.findAll();

            candidats.forEach(candidat -> {
                        fileUploadService.deleteFileByName(candidat.getCvPath().toString());
                        candidatRepository.deleteById(candidat.getId());
                    });

//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return ResponseEntity.status(HttpStatus.OK).body("All Candidats deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }

    }

    @PutMapping("/candidat/{id}")
    public ResponseEntity<?> updateCandidat(@PathVariable("id") long id, @ModelAttribute CandidatRequest candidatRequest) {
        try {
            // Fetch the candidate from the database
            Candidat existingCandidat = candidatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidat not found"));

            // Update the existing candidate's details with the new data
            existingCandidat.setNom(candidatRequest.getNom());
            existingCandidat.setPrenom(candidatRequest.getPrenom());
            existingCandidat.setNiveauDesEtudes(candidatRequest.getNiveauDesEtudes());

            // If a new CV file is uploaded, delete the old file and save the new one
            if (candidatRequest.getFile() != null && !candidatRequest.getFile().isEmpty()) {
                fileUploadService.deleteFileByName(existingCandidat.getCvPath().toString());
                String newFilePath = fileUploadService.save(candidatRequest.getFile());
                existingCandidat.setCvPath(newFilePath);
            }

            // Save the updated candidate in the database
            fileUploadService.saveCandidat(existingCandidat);

            return ResponseEntity.ok("Candidat updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }



//    @PostMapping("/upload-candidats")
//    public ResponseEntity<FileResponseMessage> uploadCandidats(@RequestParam("file") MultipartFile[] files,
//                                                               @RequestBody List<Candidat> candidats) {
//        String message = null;
//        try {
//            if (files.length != candidats.size()) {
//                throw new IllegalArgumentException("Number of files does not match number of candidates");
//            }
//
//            for (int i = 0; i < files.length; i++) {
//                MultipartFile file = files[i];
//                Candidat candidat = candidats.get(i);
//
//                // Save the file and get its path
//                String filePath = fileUploadService.save(file);
//
//                // Update the candidat's cvPath attribute with the file path
//                candidat.setCvPath(filePath);
//
//                // Save the candidat to the database
//                // Assuming you have a service method to save candidat
//                // fileUploadService.saveCandidat(candidat);
//            }
//
//            message = "Files uploaded successfully";
//            return ResponseEntity.status(OK).body(new FileResponseMessage(message));
//        } catch (Exception e) {
//            return ResponseEntity.status(EXPECTATION_FAILED).body(new FileResponseMessage(e.getMessage()));
//        }
//    }



//    @GetMapping("/file/{fileName}")
//    public ResponseEntity<Resource> getFileByName(@PathVariable String fileName) {
//        Resource resource = fileUploadService.getFileByName(fileName);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filaName=\"" + resource.getFilename() + "\"").body(resource);
//    }

//    @GetMapping("/all-files")
//    public ResponseEntity<List<FileResponse>> loadAllFiles() {
//        List<FileResponse> files = fileUploadService.loadAllFiles()
//                .map(path -> {
//                    String fileName = path.getFileName().toString();
//                    String url = MvcUriComponentsBuilder
//                            .fromMethodName(FileUploadController.class,
//                                    "getFileByName",
//                                    path.getFileName().toString()).build().toString();
//                    return new FileResponse(fileName, url);
//                }).toList();
//        return ResponseEntity.status(OK).body(files);
//    }

}
