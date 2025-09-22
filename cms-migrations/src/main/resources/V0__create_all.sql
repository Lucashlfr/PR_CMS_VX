-- =====================================================================
--  Base settings (optional, safely removable)
-- =====================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
--  AUTH  ---------------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS persistent_logins
(
    series    VARCHAR(64) NOT NULL,
    username  VARCHAR(64) NOT NULL,
    token     VARCHAR(64) NOT NULL,
    last_used DATETIME(6) NOT NULL,
    PRIMARY KEY (series)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  AUDIT ---------------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_audit
(
    `log`         CHAR(36)     NOT NULL,
    `type`        VARCHAR(255) NOT NULL,
    `category`    VARCHAR(255) NOT NULL,
    `connectId`   VARCHAR(255) NOT NULL,
    `userId`      CHAR(36)     NOT NULL,
    `timestamp`   BIGINT,
    `title`       LONGTEXT,
    `description` LONGTEXT,
    `mic`         VARCHAR(3)   NOT NULL,
    `file`        TINYINT(1),
    PRIMARY KEY (`log`),
    KEY idx_audit_connectId (`connectId`),
    KEY idx_audit_userId (`userId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  DOCUMENTS / STORAGE -------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_articles
(
    tag          INT          NOT NULL AUTO_INCREMENT,
    id           CHAR(36)     NOT NULL,
    creator      CHAR(36)     NOT NULL,
    lastUpdate   BIGINT       NOT NULL,
    target       VARCHAR(36)  NOT NULL,
    articleState VARCHAR(255) NOT NULL,
    articleType  VARCHAR(255) NOT NULL,
    title        LONGTEXT,
    description  LONGTEXT,
    imgUrl       LONGTEXT,
    html         LONGTEXT,
    form         LONGTEXT,
    PRIMARY KEY (tag),
    KEY idx_articles_id (id),
    KEY idx_articles_creator (creator)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_document
(
    documentId  CHAR(36) NOT NULL,
    owner       CHAR(36) NOT NULL,
    target      CHAR(36) NOT NULL,
    lastUpdate  BIGINT   NOT NULL,
    title       LONGTEXT,
    content     LONGTEXT,
    permissions TEXT,
    PRIMARY KEY (documentId),
    KEY idx_document_owner (owner),
    KEY idx_document_target (target)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_storage
(
    tag        INT         NOT NULL AUTO_INCREMENT,
    documentId CHAR(36)    NOT NULL,
    owner      CHAR(36)    NOT NULL,
    target     CHAR(36)    NOT NULL,
    lastUpdate BIGINT      NOT NULL,
    title      LONGTEXT,
    date       BIGINT      NOT NULL,
    meta       DOUBLE      NOT NULL,
    `type`     VARCHAR(30) NOT NULL,
    `path`     LONGTEXT,
    PRIMARY KEY (tag),
    KEY idx_storage_document (documentId),
    KEY idx_storage_owner (owner),
    KEY idx_storage_target (target)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  EVENTS / PLANER -----------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_events
(
    eventId             CHAR(36)   NOT NULL,
    tenant              VARCHAR(4) NOT NULL,
    number              INT,
    title               LONGTEXT,
    description         LONGTEXT,
    eventType           VARCHAR(50),
    eventState          VARCHAR(50),
    startDate           BIGINT,
    endDate             BIGINT,
    deadline            BIGINT,
    creationDate        BIGINT,
    resubmission        BIGINT,
    lastUpdate          BIGINT,
    schedule            LONGTEXT,
    registrationRelease LONGTEXT,
    targetGroup         LONGTEXT,
    location            LONGTEXT,
    imgUrl              LONGTEXT,
    riskIndex           INT,
    currentEditor       CHAR(36),
    createdBy           CHAR(36),
    updatedBy           CHAR(36),
    principal           CHAR(36),
    manager             CHAR(36),
    expenditure         DOUBLE,
    revenue             DOUBLE,
    pressRelease        LONGTEXT,
    preventionConcept   LONGTEXT,
    notes               LONGTEXT,
    application         LONGTEXT,
    PRIMARY KEY (eventId),
    KEY idx_events_tenant (tenant),
    KEY idx_events_state (eventState),
    KEY idx_events_dates (startDate, endDate)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_event_message
(
    messageId   CHAR(36) NOT NULL,
    eventId     CHAR(36) NOT NULL,
    `number`    INT,
    title       LONGTEXT,
    description LONGTEXT,
    `date`      BIGINT,
    `type`      VARCHAR(30),
    userId      CHAR(36),
    PRIMARY KEY (messageId),
    KEY idx_evtmsg_event (eventId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_events_results
(
    resultId CHAR(36) NOT NULL,
    eventId  CHAR(36) NOT NULL,
    userId   CHAR(36),
    `date`   BIGINT,
    json     LONGTEXT,
    PRIMARY KEY (resultId),
    KEY idx_evtres_event (eventId),
    KEY idx_evtres_user (userId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_planer_tasks
(
    taskId          CHAR(36)     NOT NULL,
    planerId        CHAR(36)     NOT NULL,
    id              INT,
    taskName        LONGTEXT,
    taskDescription LONGTEXT,
    taskState       VARCHAR(255) NOT NULL,
    created         BIGINT,
    updated         BIGINT,
    lable           VARCHAR(255),
    PRIMARY KEY (taskId),
    KEY idx_planer_tasks_planer (planerId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_planer_map
(
    planerId CHAR(36) NOT NULL,
    userId   CHAR(36) NOT NULL,
    `date`   BIGINT,
    PRIMARY KEY (planerId, userId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_planer_principal
(
    planerId CHAR(36) NOT NULL,
    userId   CHAR(36) NOT NULL,
    PRIMARY KEY (planerId, userId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS prevention_forms
(
    id                        CHAR(36) NOT NULL,
    structural_concerns       LONGTEXT,
    toilet_signage            LONGTEXT,
    room_visibility           LONGTEXT,
    welcome_round             LONGTEXT,
    photo_policy              LONGTEXT,
    complaint_channels        LONGTEXT,
    one_on_one_situations     LONGTEXT,
    hierarchical_dependencies LONGTEXT,
    communication_channels    LONGTEXT,
    decision_transparency     LONGTEXT,
    created_at                BIGINT,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  LITURGY -------------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_liturgie
(
    liturgieId   CHAR(36)    NOT NULL,
    `number`     INT         NOT NULL,
    tenant       VARCHAR(4)  NOT NULL,
    liturgieType VARCHAR(50) NOT NULL,
    `date`       BIGINT      NOT NULL,
    `local`      TINYINT(1)  NOT NULL,
    PRIMARY KEY (liturgieId),
    KEY idx_liturgie_date (`date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_liturgie_map
(
    liturgieId CHAR(36)     NOT NULL,
    personId   CHAR(36)     NOT NULL,
    `state`    VARCHAR(255) NOT NULL,
    PRIMARY KEY (liturgieId, personId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_liturgie_request
(
    requestId CHAR(36)   NOT NULL,
    tenant    VARCHAR(4) NOT NULL,
    creatorId CHAR(36)   NOT NULL,
    `number`  INT        NOT NULL,
    `name`    TEXT       NOT NULL,
    startDate BIGINT     NOT NULL,
    endDate   BIGINT     NOT NULL,
    deadline  BIGINT     NOT NULL,
    `active`  TINYINT(1) NOT NULL,
    PRIMARY KEY (requestId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_liturgie_request_map
(
    requestId CHAR(36) NOT NULL,
    personId  CHAR(36) NOT NULL,
    `date`    BIGINT   NOT NULL,
    PRIMARY KEY (requestId, personId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  PERSON --------------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_person
(
    person_id      CHAR(36)   NOT NULL,
    tenant         VARCHAR(4) NOT NULL,
    `type`         VARCHAR(255),
    person_rank    VARCHAR(255),
    principal      CHAR(36)   NOT NULL,
    fRank          INT        NOT NULL DEFAULT 0,
    salutation     VARCHAR(255),
    firstname      VARCHAR(255),
    lastname       VARCHAR(255),
    gender         VARCHAR(255),
    birthdate      BIGINT,
    street         VARCHAR(255),
    houseNumber    VARCHAR(255),
    postalCode     VARCHAR(255),
    city           VARCHAR(255),
    email          VARCHAR(255),
    phone          VARCHAR(255),
    mobile         VARCHAR(255),
    accessionDate  BIGINT,
    exitDate       BIGINT,
    activityNote   LONGTEXT,
    notes          LONGTEXT,
    `active`       TINYINT(1) NOT NULL,
    canLogin       TINYINT(1) NOT NULL,
    username       VARCHAR(255),
    `password`     VARCHAR(255),
    iban           VARCHAR(255),
    bic            VARCHAR(255),
    bank           VARCHAR(255),
    accountHolder  VARCHAR(255),
    privacy_policy LONGTEXT,
    signature      LONGBLOB,
    ob1            TINYINT(1),
    ob2            TINYINT(1),
    ob3            TINYINT(1),
    ob4            TINYINT(1),
    preventionDate BIGINT,
    customPassword TINYINT(1) NOT NULL,
    lastUpdate     BIGINT,
    PRIMARY KEY (person_id),
    KEY idx_person_principal (principal),
    KEY idx_person_login (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_person_connection
(
    connection_id CHAR(36)    NOT NULL,
    host          CHAR(36)    NOT NULL,
    sub           CHAR(36)    NOT NULL,
    `type`        VARCHAR(64) NOT NULL,
    PRIMARY KEY (connection_id),
    KEY idx_person_conn_host (host),
    KEY idx_person_conn_sub (sub)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_person_emergency_contact
(
    contact_id CHAR(36)   NOT NULL,
    person_id  CHAR(36)   NOT NULL,
    `type`     VARCHAR(255),
    firstname  VARCHAR(255),
    lastname   VARCHAR(255),
    phone      VARCHAR(255),
    mail       VARCHAR(255),
    `active`   TINYINT(1) NOT NULL,
    PRIMARY KEY (contact_id),
    KEY idx_ec_person (person_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_person_flag
(
    flagId         CHAR(36) NOT NULL,
    person_id      CHAR(36) NOT NULL,
    `number`       INT      NOT NULL AUTO_INCREMENT,
    flagType       VARCHAR(255),
    flagDetails    LONGTEXT,
    additionalInfo LONGTEXT,
    flagDate       BIGINT,
    complained     TINYINT(1),
    PRIMARY KEY (flagId),
    UNIQUE KEY uk_person_flag_number (`number`),
    KEY idx_flag_person (person_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  WORKFLOW ------------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_workflow
(
    workflowId          CHAR(36) NOT NULL,
    workflowName        VARCHAR(255),
    workflowDescription LONGTEXT,
    workflowCategory    VARCHAR(255),
    workflowState       VARCHAR(255),
    workflowEditor      CHAR(36),
    workflowCreator     CHAR(36),
    workflowManager     CHAR(36),
    workflowTarget      CHAR(36),
    attachments         INT,
    notes               INT,
    creationDate        BIGINT,
    modificationDate    BIGINT,
    endDate             BIGINT,
    currentNumber       INT,
    metaData            LONGTEXT,
    PRIMARY KEY (workflowId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS module_workflow_modules
(
    moduleId      VARCHAR(255) primary key,
    workflowId    VARCHAR(255),
    ownerId       VARCHAR(255),
    workflowState VARCHAR(255),
    endDate       long,
    number        int,
    uniqueName    VARCHAR(255),
    metaData      LONGTEXT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  TASKS (global) ------------------------------------------------------
-- =====================================================================
CREATE TABLE IF NOT EXISTS module_tasks
(
    `number`     INT      NOT NULL,
    taskId       CHAR(36) NOT NULL,
    `link`       CHAR(36) NOT NULL,
    creator      CHAR(36) NOT NULL,
    editor       CHAR(36) NOT NULL,
    lastUpdate   BIGINT,
    creationDate BIGINT,
    endDate      BIGINT,
    title        LONGTEXT,
    description  LONGTEXT,
    taskState    VARCHAR(50),
    priority     VARCHAR(10),
    PRIMARY KEY (`number`),
    UNIQUE KEY uk_tasks_taskId (taskId),
    KEY idx_tasks_link (link)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
