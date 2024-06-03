package ma.marjane.multi_file.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Simpson Alfred
 */
@Data
@AllArgsConstructor
public class FileResponse {
    private String fileName;
    private String url;
}
