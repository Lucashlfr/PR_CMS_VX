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
);

CREATE TABLE module_workflow (
                                 workflowId CHAR(36) NOT NULL,
                                 workflowName VARCHAR(255),
                                 workflowDescription LONGTEXT,
                                 workflowCategory VARCHAR(255),
                                 workflowState VARCHAR(255),
                                 workflowEditor CHAR(36),
                                 workflowCreator CHAR(36),
                                 workflowManager CHAR(36),
                                 workflowTarget CHAR(36),
                                 attachments INT,
                                 notes INT,
                                 creationDate BIGINT,
                                 modificationDate BIGINT,
                                 endDate BIGINT,
                                 currentNumber INT,
                                 metaData LONGTEXT,
                                 PRIMARY KEY (workflowId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
