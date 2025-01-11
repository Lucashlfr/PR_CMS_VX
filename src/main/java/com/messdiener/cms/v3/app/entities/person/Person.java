package com.messdiener.cms.v3.app.entities.person;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.entities.person.data.statistics.PersonStatistics;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.entities.user.config.UserConfigurations;
import com.messdiener.cms.v3.app.entities.workflows.Workflow;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.shared.enums.WorkflowAttributes;
import com.messdiener.cms.v3.utils.other.Pair;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Person {

    private UUID id;
    private UUID tenantId;

    private PersonAttributes.Type type;
    private PersonAttributes.Rank rank;

    private PersonAttributes.Salutation salutation;
    private String firstname;
    private String lastname;

    private PersonAttributes.Gender gender;
    private Optional<CMSDate> birthdate;

    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;

    private String email;
    private String phone;
    private String mobile;

    private Optional<CMSDate> accessionDate;
    private Optional<CMSDate> exitDate;
    private String activityNote;

    private String notes;

    private boolean active;

    private boolean canLogin;
    private String username;
    private String password;

    private String iban;
    private String bic;
    private String bank;
    private String accountHolder;

    private String privacy_policy;
    private String signature;

    public static Person empty(UUID tenantId) {
        return new Person(UUID.randomUUID(), tenantId, PersonAttributes.Type.NULL, PersonAttributes.Rank.NULL, PersonAttributes.Salutation.NULL, "", "", PersonAttributes.Gender.NOT_SPECIFIED, Optional.empty(), "", "", "", "", "", "", "", Optional.empty(), Optional.empty(), "", "", true, false, "", "", "", "", "", "", "", "");
    }

    public String getReadName() {
        return lastname + ", " + firstname;
    }

    public String getName() {
        return firstname + " " + lastname;
    }

    public String getImgAdress() {

        Optional<Pair<String, File>> profileImg = getData().stream().filter(pair -> pair.getFirst().contains("profileImg")).findFirst();
        return profileImg.isPresent() ? "/messdiener/person/download?uuid=" + id + "&name=profileImg.jpg" : "/dist/assets/img/illustrations/profiles/profile-1.png";
    }

    public Set<Pair<String, File>> getData(int i) {
        return Cache.getPersonService().listFilesUsingJavaIO(id, i);
    }

    public Set<Pair<String, File>> getData() {
        return Cache.getPersonService().listFilesUsingJavaIO(id);
    }

    public Tenant getTenant() throws SQLException {
        return Cache.getTenantService().findTenant(tenantId).orElse(new Tenant(tenantId, "TENANT NICHT GEFUNDEN!", "gray", "N"));
    }

    public void update() throws SQLException {
        Cache.getPersonService().updatePerson(this);
    }

    public PersonStatistics getStatistics() throws SQLException {
        return new PersonStatistics(this);
    }

    public boolean hasPermission(String name) throws SQLException {
        return Cache.getPermissionService().userHasePermission(this, name, false);
    }

    public boolean hasPermissionStrict(String name) throws SQLException {
        return Cache.getPermissionService().userHasePermission(this, name, true);
    }


    public UserConfigurations getConfigurations() throws SQLException {
        return Cache.getUserService().getUserConfigurations(id);
    }

    public void removeAllPermissions() {
        Cache.getPermissionService().removeAllPermissionFromUser(id);
    }

    public void addPermissions(List<String> selectedPermissions) throws SQLException {
        for (String p : selectedPermissions) {
            Cache.getPermissionService().mapPermissions(this, p, true);
        }
    }

    public void removePermission(String name) throws SQLException {
        Cache.getPermissionService().mapPermissions(this, name, false);
    }



    public List<PersonConnection> getPersonConnections() throws SQLException {
        return Cache.getPersonService().getConnectionsByHost(id);
    }

    public boolean isRegistered(UUID eventId) throws SQLException {
        return Cache.getOrganisationService().isRegistered(this.id, eventId);
    }

    public boolean isScheduled(UUID eventId) throws SQLException {
        return Cache.getOrganisationService().isScheduled(this.id, eventId);
    }

    public String getAddress(){
        String address = street + " " + houseNumber + ", " + postalCode + " " + city;
        if(address.length() == 4){
            return "";
        }
        return address;
    }

    public String getBankDetails(){
        String details = iban + " / " + bic + " / " + bank + " / " + accountHolder;
        if(details.length() == 9){
            return "";
        }
        return details;
    }

    public List<OrganisationEvent> getOrganisationEvents(int response, int schedule) throws SQLException {
        return Cache.getOrganisationService().getEventsByPerson(id, OrganisationType.WORSHIP, response, schedule);
    }

    public int getOpenWorkflows() throws SQLException {
        return Cache.getWorkflowService().getWorkflowsByUser(id, WorkflowAttributes.WorkflowState.PENDING).size();
    }

    public List<Workflow> getWorkflows() throws SQLException {
        return Cache.getWorkflowService().getWorkflowsByUser(id);
    }


}
