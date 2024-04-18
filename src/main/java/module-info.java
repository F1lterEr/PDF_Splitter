module com.example.pdfsplitter {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.pdfbox;
    requires java.mail;
    requires java.activation;


    opens com.example.pdfsplitter to javafx.fxml;
    exports com.example.pdfsplitter;
}