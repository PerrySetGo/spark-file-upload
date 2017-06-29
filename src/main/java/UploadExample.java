import spark.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class UploadExample {

    public static void main(String[] args) {
        enableDebugScreen();

        File uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist

        staticFiles.externalLocation("upload");

        get("/", (req, res) ->
        //could be replaced by template
                  "<form method='post' enctype='multipart/form-data'>" // note the enctype
                + "    <input type='file' name='uploaded_file' accept='image/*'>" // make sure to call getPart using the same "name" in the post
                + "    <button>Upload picture</button>"
                + "</form>"
        );

        post("/", (req, res) -> {
            String originalFileName;
            Path newPath;
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) { // getPart needs to use same "name" as input field in form

              originalFileName = getFileName(req.raw().getPart("uploaded_file"));
              Path tempFile = Files.createTempFile(uploadDir.toPath(),originalFileName, "");
              System.out.println(tempFile.toString());
              newPath = Paths.get(uploadDir.toString(), originalFileName); //make a new path object to pass

              Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
              Files.move(tempFile, newPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "<h1>You uploaded this image:<h1><img src='" + newPath.getFileName() + "'>"; //or send to template
        });

    }

    private static String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
