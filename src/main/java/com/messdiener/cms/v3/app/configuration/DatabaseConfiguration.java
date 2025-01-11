package com.messdiener.cms.v3.app.configuration;

import lombok.Getter;
import lombok.Setter;

public class DatabaseConfiguration {

    private DatabaseConfiguration(){}

    @Getter @Setter
    private static String host = "#";

    @Getter @Setter
    private static String port = "#";

    @Getter @Setter
    private static String database = "#";

    @Getter @Setter
    private static String user = "#";

    @Getter @Setter
    private static String password = "#";

}
