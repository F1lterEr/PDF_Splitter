package com.example.pdfsplitter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelloController {
    @FXML
    private TextField eMailText;

    @FXML
    private TextField passwordText;
    @FXML
    private Button sendButton;

    public void initialize() {
        sendButton.setOnAction(event -> {
            String email = eMailText.getText();
            String password = passwordText.getText();
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                try {
                    PDDocument document = PDDocument.load(selectedFile);
                    PDFTextStripper textStripper = new PDFTextStripper();
                    int pageNum = 0;
                    for (PDPage page : document.getPages()) {
                        pageNum++;
                        textStripper.setStartPage(pageNum);
                        String pageText = textStripper.getText(document);
                        // Поиск имени с помощью регулярки
                        String fioRegex = "([А-ЯЁ][а-яё]+\\s[А-ЯЁ][а-яё]+\\s[А-ЯЁ][а-яё]+)";
                        Pattern pattern = Pattern.compile(fioRegex);
                        Matcher matcher = pattern.matcher(pageText);
                        String fio = "";
                        if (matcher.find()) {
                            fio = matcher.group(1);
                        }
                        // Название файла
                        String pageTitle = fio.isEmpty() ? "Page " + pageNum : fio;
                        PDDocument newDocument = new PDDocument();
                        newDocument.addPage(page);
                        newDocument.save("C:\\Users\\mel1s\\Desktop\\Тест\\output\\" + pageTitle + ".pdf");
                        newDocument.close();
                    }
                    document.close();
                    ReadFile(email, password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void ReadFile(String emailauth, String passwordauth) {
        File outputFolder = new File("C:\\Users\\mel1s\\Desktop\\Тест\\output\\");
        File[] files = outputFolder.listFiles();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\mel1s\\Desktop\\Тест\\РЛ.txt"))) {
            String line;
            while (((line = br.readLine()) != null) | (files.length > 0)) {
                if (line == null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        System.out.println("ФИО не указано в файле. Перемещение в папку Не отправлено");
                        file.renameTo(new File("C:\\Users\\mel1s\\Desktop\\Тест\\Не отправлено\\" + fileName));
                    }
                    if (files.length > 0) {
                        break;
                    }
                }
                Pattern pattern = Pattern.compile("([А-ЯЁ][а-яё]+\s[А-ЯЁ][а-яё]+\s[А-ЯЁ][а-яё]+)\s-\s(.+)");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String fio = matcher.group(1);
                    String email = matcher.group(2);
                    boolean containsEmail = true;
                    for (File file : files) {
                        String fileName = file.getName();
                        String fileNameex = fileName.substring(0, fileName.indexOf(".pdf"));
                        if ((containsEmail) & (fio.equals(fileNameex))) {
                            System.out.println("Файл " + fileName + " содержит почту " + fio);
                            SendEmail(emailauth, passwordauth, email, "C:\\Users\\mel1s\\Desktop\\Тест\\output\\" + fileName);
                            file.renameTo(new File("C:\\Users\\mel1s\\Desktop\\Тест\\Отправлено\\" + fileName));
                            containsEmail = true;
                            File[] files1 = outputFolder.listFiles();
                            files = files1;
                            break;
                        }
                        else {
                            System.out.println("Файл " + fileName + " не отправлен");
                            file.renameTo(new File("C:\\Users\\mel1s\\Desktop\\Тест\\Не отправлено\\" + fileName));
                            File[] files1 = outputFolder.listFiles();
                            files = files1;
                            break;
                        }
                    }
                } else {
                    for (File file : files) {
                        String fileName = file.getName();
                        System.out.println("Файл " + fileName + " не отправлен");
                        file.renameTo(new File("C:\\Users\\mel1s\\Desktop\\Тест\\Не отправлено\\" + fileName));
                        File[] files1 = outputFolder.listFiles();
                        files = files1;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void SendEmail(String eMailAuth, String passwordAuth, String toEmail, String filePath) {
        final String username = eMailAuth;
        final String password = passwordAuth;
        String email = username;
        Pattern pattern = Pattern.compile("@([\\w-]+\\.)+[\\w-]+");
        Matcher matcher = pattern.matcher(email);
        String domain = "";
        if (matcher.find()) {
            domain = matcher.group().substring(1); // Извлекаем домен без символа @
        }
        // Создание сервера
        Properties properties = new Properties();
        if (domain.equalsIgnoreCase("gmail.com")) {
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
        } else if (domain.equalsIgnoreCase("yandex.ru")) {
            properties.put("mail.smtp.host", "smtp.yandex.ru");
            properties.put("mail.smtp.port", "587");
        } else if (domain.equalsIgnoreCase("mail.ru")) {
            properties.put("mail.smtp.host", "smtp.mail.ru");
            properties.put("mail.smtp.port", "587");
        }
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Создание сессии
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(username));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            message.setSubject("Рассчётный лист за этот месяц");

            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setText("Проверьте рассчётные листы.");

            Multipart multipart = new MimeMultipart();

            multipart.addBodyPart(messageBodyPart);

            // Прикрепление файла
            messageBodyPart = new MimeBodyPart();
            String filename = filePath;
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(new File(filename).getName());
            multipart.addBodyPart(messageBodyPart);

            // Завершение создания сообщения
            message.setContent(multipart);

            // Отправка
            Transport.send(message);

            System.out.println("Отправка сообщения успешна");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}