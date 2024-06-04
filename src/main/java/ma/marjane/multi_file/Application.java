package ma.marjane.multi_file;

import jakarta.annotation.Resource;
import ma.marjane.multi_file.upload.IFileUploadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Resource
	private IFileUploadService fileUploadService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileUploadService.init();
	}
}
