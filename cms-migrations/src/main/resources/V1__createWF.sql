-- =====================================================================
-- Workflow Kern
-- =====================================================================

CREATE TABLE IF NOT EXISTS wf_workflows (
                                            workflow_id       VARCHAR(36)  NOT NULL,
                                            process_key       VARCHAR(255) NOT NULL,
                                            process_version   INT          NOT NULL,
                                            title             VARCHAR(255) NOT NULL,
                                            description       LONGTEXT     NULL,
                                            state             VARCHAR(64)  NOT NULL,
                                            assignee_id       VARCHAR(36)  NULL,
                                            applicant_id      VARCHAR(36)  NULL,
                                            manager_id        VARCHAR(36)  NULL,
                                            target_element    VARCHAR(36)  NULL,
                                            priority          INT          NOT NULL,
                                            tags_json         LONGTEXT     NULL,
                                            attachments       INT          NOT NULL,
                                            notes             INT          NOT NULL,
                                            creation_date     BIGINT       NULL,
                                            modification_date BIGINT       NULL,
                                            end_date          BIGINT       NULL,
                                            current_number    INT          NOT NULL,
                                            metadata          LONGTEXT     NULL,
                                            PRIMARY KEY (workflow_id),
                                            KEY idx_wf_process (process_key, process_version),
                                            KEY idx_wf_state (state),
                                            KEY idx_wf_assignee (assignee_id),
                                            KEY idx_wf_applicant (applicant_id),
                                            KEY idx_wf_manager (manager_id),
                                            KEY idx_wf_target (target_element),
                                            KEY idx_wf_creation (creation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Konfigurationen
-- =====================================================================

CREATE TABLE IF NOT EXISTS wf_process_definitions (
                                                      process_definition_id VARCHAR(36)  NOT NULL,
                                                      process_key           VARCHAR(255) NOT NULL,
                                                      name                  VARCHAR(255) NOT NULL,
                                                      version               INT          NOT NULL,
                                                      states_json           LONGTEXT     NULL,
                                                      transitions_json      LONGTEXT     NULL,
                                                      sla_policies_json     LONGTEXT     NULL,
                                                      PRIMARY KEY (process_definition_id),
                                                      UNIQUE KEY uq_proc_key_version (process_key, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_form_definitions (
                                                   form_definition_id   VARCHAR(36)  NOT NULL,
                                                   form_key             VARCHAR(255) NOT NULL,
                                                   version              INT          NOT NULL,
                                                   form_name            VARCHAR(255) NOT NULL,
                                                   form_description     LONGTEXT     NULL,
                                                   form_img             VARCHAR(1024) NULL,
                                                   json_schema          LONGTEXT     NULL,
                                                   ui_schema            LONGTEXT     NULL,
                                                   validations_json     LONGTEXT     NULL,
                                                   output_templates_json LONGTEXT    NULL,
                                                   PRIMARY KEY (form_definition_id),
                                                   UNIQUE KEY uq_form_key_version (form_key, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_notification_templates (
                                                         notification_template_id VARCHAR(36)  NOT NULL,
                                                         template_key             VARCHAR(255) NOT NULL,
                                                         channel                  VARCHAR(64)  NOT NULL,
                                                         version                  INT          NOT NULL,
                                                         subject_template         LONGTEXT     NULL,
                                                         body_template            LONGTEXT     NULL,
                                                         PRIMARY KEY (notification_template_id),
                                                         UNIQUE KEY uq_tpl_key_version (template_key, version),
                                                         KEY idx_tpl_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_automation_rules (
                                                   rule_id         VARCHAR(36)  NOT NULL,
                                                   name            VARCHAR(255) NOT NULL,
                                                   trigger_type    VARCHAR(64)  NOT NULL,
                                                   trigger_config  VARCHAR(512) NULL,
                                                   condition_json  LONGTEXT     NULL,
                                                   actions_json    LONGTEXT     NULL,
                                                   enabled         TINYINT(1)   NOT NULL,
                                                   PRIMARY KEY (rule_id),
                                                   KEY idx_rule_enabled (enabled),
                                                   KEY idx_rule_trigger (trigger_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Datenobjekte
-- =====================================================================

CREATE TABLE IF NOT EXISTS wf_artifacts (
                                            artifact_id  VARCHAR(36) NOT NULL,
                                            workflow_id  VARCHAR(36) NOT NULL,
                                            artifact_type VARCHAR(64) NOT NULL,
                                            created_at   BIGINT      NOT NULL,
                                            created_by   VARCHAR(36) NULL,
                                            PRIMARY KEY (artifact_id),
                                            KEY idx_art_wf (workflow_id),
                                            KEY idx_art_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_audit_trails (
                                               trail_id        VARCHAR(36)  NOT NULL,
                                               workflow_id     VARCHAR(36)  NOT NULL,
                                               actor_id        VARCHAR(36)  NULL,
                                               action          VARCHAR(255) NOT NULL,
                                               before_payload  LONGTEXT     NULL,
                                               after_payload   LONGTEXT     NULL,
                                               date            BIGINT       NOT NULL,
                                               PRIMARY KEY (trail_id),
                                               KEY idx_audit_wf (workflow_id),
                                               KEY idx_audit_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_documentation (
                                                documentation_id VARCHAR(36)  NOT NULL,
                                                workflow_id      VARCHAR(36)  NOT NULL,
                                                title            VARCHAR(255) NOT NULL,
                                                description      LONGTEXT     NULL,
                                                assignee_id      VARCHAR(36)  NULL,
                                                created_at       BIGINT       NOT NULL,
                                                PRIMARY KEY (documentation_id),
                                                KEY idx_doc_wf (workflow_id),
                                                KEY idx_doc_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_form_submissions (
                                                   submission_id  VARCHAR(36)  NOT NULL,
                                                   workflow_id    VARCHAR(36)  NOT NULL,
                                                   form_key       VARCHAR(255) NOT NULL,
                                                   form_version   INT          NOT NULL,
                                                   submitted_by   VARCHAR(36)  NULL,
                                                   submitted_at   BIGINT       NOT NULL,
                                                   `checksum`     VARCHAR(128) NULL,
                                                   payload_json   LONGTEXT     NULL,
                                                   PRIMARY KEY (submission_id),
                                                   KEY idx_sub_wf (workflow_id),
                                                   KEY idx_sub_form (form_key, form_version),
                                                   KEY idx_sub_time (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_notifications (
                                                notification_id VARCHAR(36) NOT NULL,
                                                template_id     VARCHAR(36) NOT NULL,
                                                state           VARCHAR(64) NOT NULL,
                                                payload_json    LONGTEXT    NULL,
                                                PRIMARY KEY (notification_id),
                                                KEY idx_notif_template (template_id),
                                                KEY idx_notif_state (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS wf_tasks (
                                        task_id           VARCHAR(36)  NOT NULL,
                                        workflow_id       VARCHAR(36)  NOT NULL,
                                        task_key          VARCHAR(255) NOT NULL,
                                        title             VARCHAR(255) NOT NULL,
                                        state             VARCHAR(64)  NOT NULL,
                                        candidate_roles_json LONGTEXT  NULL,
                                        assignee_id       VARCHAR(36)  NULL,
                                        due_at            BIGINT       NULL,
                                        created_at        BIGINT       NOT NULL,
                                        payload_json      LONGTEXT     NULL,
                                        PRIMARY KEY (task_id),
                                        KEY idx_task_wf (workflow_id),
                                        KEY idx_task_state (state),
                                        KEY idx_task_assignee (assignee_id),
                                        KEY idx_task_due (due_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
