package com.messdiener.cms.v3.utils.html;

import com.messdiener.cms.v3.app.entities.user.config.UserConfigurations;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserHTMLClasses {

	private UserConfigurations userConfigurations;

	public boolean show(String name){
		return userConfigurations.getValue(name).equals("true");
	}

}
