# ALTER TABLE module_user ADD tenant VARCHAR(255)
# ALTER TABLE module_persons ADD tenant VARCHAR(255);
# UPDATE module_persons SET tenant = 'DEFAULT' WHERE 1;
# ALTER TABLE module_persons ADD rank int;
# ALTER TABLE module_persons add dienplan long;
# UPDATE module_persons SET rank = 0, dienplan = 0 WHERE 1;
# ALTER TABLE module_persons add datenschutz longtext;
UPDATE module_persons SET datenschutz = true WHERE notes LIKE '%datenschutz:true;%'