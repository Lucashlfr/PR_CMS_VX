package com.messdiener.cms.v3.security;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.other.CharacterConverter;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Random;

@Component
public class SecurityHelper {

    private static Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static String getUsername(){
        return getAuthentication().getName();
    }

    public static Person getPerson() throws SQLException {
        return Cache.getPersonService().getPersonByUsername(getUsername()).orElseThrow();
    }

    public static Person addPersonToSession(HttpSession httpSession) throws SQLException {
        if(httpSession.getAttribute("sessionUser") == null){
            httpSession.setAttribute("sessionUser", getPerson());
        }

        if(httpSession.getAttribute("serviceName") == null){
            httpSession.setAttribute("serviceName", Cache.APP_NAME);
        }

        return getPerson();
    }

    private static final String[] charCategories = new String[] {
            "abcdefghijklmnopqrstuvwxyz",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            "0123456789",
            "-_@&/!"
    };

    public static String generatePassword() {
        int length = 8;

        StringBuilder password = new StringBuilder(length);
        Random random = new Random(System.nanoTime());

        for (int i = 0; i < length; i++) {
            String charCategory = charCategories[random.nextInt(charCategories.length)];
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }

        return new String(password);
    }

    public static String generateUsername(String firstname, String lastname){
        String f = CharacterConverter.convert(firstname);
        String l = CharacterConverter.convert(lastname);
        return f + "." + l;
    }

}
