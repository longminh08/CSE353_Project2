import java.io.File;

public class DeleteFile {
    public static void main(String[] args) {
        // Specify the path to your file
        for (int i = 1; i< 6; i++){
        String filePath = "node"+ i +"output.txt";
        File file = new File(filePath);

        // Check if the file exists before attempting to delete
        if (file.exists()) {
            if (file.delete()) {
                System.out.println(filePath + " has been deleted successfully.");
            } else {
                System.out.println("Failed to delete " + filePath);
            }
        } else {
            System.out.println(filePath + " does not exist.");
        }
    }
}
}
