package com.messdiener.cms.notifications.mail;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

public class MailService {

    public static void sendHtmlEmail(String to, String subject, String heading, String bodyText, String linkUrl) throws EmailException {
        // HTML-Template mit Platzhaltern für die Überschrift, den Text und den Link
        String htmlMessage = "<!DOCTYPE html>" +
                "<html lang=\"de\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>" + subject + "</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; }" +
                "        .email-container { background-color: #3A557C; color: white; }" +
                "        .header { text-align: center; padding: 20px; }" +
                "        .header img { width: 200px; }" +
                "        .content { padding: 20px; text-align: center; }" +
                "        .content h1 { font-size: 24px; }" +
                "        .content p { font-size: 16px; }" +
                "        .content a { color: #FF9900; text-decoration: none; }" +
                "        .footer { padding: 10px; text-align: center; background-color: #2C4A68; font-size: 12px; color: #CCCCCC; }" +
                "    </style>" +
                "</head>" +
                "<body style=\"background-color: #3A557C\">" +
                "    <div class=\"email-container\">" +
                "        <!-- Header Section -->" +
                "        <div class=\"header\">" +
                "            <img src=\"https://i.ibb.co/zT1pdSbr/Logo-Ohne-Bg.png\" alt=\"Logo\">" +
                "        </div>" +
                "        <!-- Content Section -->" +
                "        <div class=\"content\">" +
                "            <h1>" + heading + "</h1>" +  // Dynamische Überschrift
                "            <p>" + bodyText + "</p>" +  // Dynamischer Text
                "            <a href=\"" + linkUrl + "\">Link zum CMS</a>" + // Dynamischer Link
                "        </div><br><br>" +
                "        <!-- Footer Section -->" +
                "        <div class=\"footer\">" +
                "            <span class=\"text-sm\">Diese E-Mail wurde automatisiert generiert</span><br>" +
                "            <span>Bei Rückfragen wenden Sie sich bitte an <a href=\"mailto:info@messdiener.com\">info@messdiener.com</a></span>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";


        HtmlEmail email = getHtmlEmail(to, subject, htmlMessage);
        email.send();

        System.out.println("HTML-E-Mail erfolgreich gesendet!");
    }

    private static HtmlEmail getHtmlEmail(String to, String subject, String htmlMessage) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        email.setHostName("smtp.strato.de"); // SMTP-Server
        email.setSmtpPort(587); // SMTP-Port für STARTTLS
        email.setAuthentication("cms@messdiener.com", "3274eAf?zLz;Edf"); // Ersetze dies durch eine sichere Methode
        email.setStartTLSEnabled(true);
        email.setSSLOnConnect(false);
        email.setFrom("cms@messdiener.com");
        email.addTo(to);
        email.setSubject(subject);
        email.setHtmlMsg(htmlMessage);
        return email;
    }


}
